package fr.insee.genesis.infrastructure.utils.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;


class PerretAuthWebClientFilterTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        port = server.getAddress().getPort();
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null) server.stop(0);
    }


    private WebClient clientWithFilter(OidcService oidc) {
        return WebClient.builder()
                .baseUrl("http://localhost:" + port)
                .filter(PerretAuthWebClientFilter.perretAuthFilter(oidc))
                .build();
    }

    @Test
    void addsBearerAndNoRetryOn200() {
        AtomicInteger hitCount = new AtomicInteger();
        server.createContext("/ok", ex -> {
            hitCount.incrementAndGet();
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            assertEquals("Bearer INIT", auth, "Le header Authorization doit contenir le token initial");
            byte[] body = "{\"msg\":\"ok\"}".getBytes(StandardCharsets.UTF_8);
            ex.getResponseHeaders().add("Content-Type", "application/json");
            ex.sendResponseHeaders(200, body.length);
            try (OutputStream os = ex.getResponseBody()) { os.write(body); }
            ex.close();
        });

        var oidc = new OidcServiceStub("INIT", "REFRESHED");
        ResponseEntity<String> resp = clientWithFilter(oidc)
                .get().uri("/ok")
                .retrieve().toEntity(String.class)
                .block();

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(1, hitCount.get(), "Pas de retry attendu sur 200");
        assertEquals(1, oidc.getCalls(), "Une seule lecture du token");
        assertEquals(0, oidc.refreshCalls(), "Aucun refresh attendu");
    }

    @Test
    void retriesOnceOn401WithRefreshedToken_thenSuccess() {
        AtomicInteger hitCount = new AtomicInteger();
        server.createContext("/needs-refresh", (HttpExchange ex) -> {
            int n = hitCount.incrementAndGet();
            String auth = ex.getRequestHeaders().getFirst("Authorization");
            if (n == 1) {
                // 1er appel : doit être fait avec le token initial return 401
                assertEquals("Bearer INIT", auth, "1er appel doit utiliser le token initial");
                ex.sendResponseHeaders(401, -1);
                ex.close();
            } else {
                // 2e appel (retry) : doit utiliser le token rafraîchi -> 200
                assertEquals("Bearer REFRESHED", auth, "Retry doit utiliser le token rafraîchi");
                byte[] body = "{\"msg\":\"after-refresh\"}".getBytes(StandardCharsets.UTF_8);
                ex.getResponseHeaders().add("Content-Type", "application/json");
                ex.sendResponseHeaders(200, body.length);
                try (OutputStream os = ex.getResponseBody()) { os.write(body); }
                ex.close();
            }
        });

        var oidc = new OidcServiceStub("INIT", "REFRESHED");
        ResponseEntity<String> resp = clientWithFilter(oidc)
                .get().uri("/needs-refresh")
                .retrieve().toEntity(String.class)
                .block();

        assertNotNull(resp);
        assertEquals(200, resp.getStatusCode().value());
        assertEquals(2, hitCount.get(), "Un seul retry attendu (total 2 appels)");
        assertTrue(oidc.getCalls() >= 2, "Le token est lu avant et après refresh");
        assertEquals(1, oidc.refreshCalls(), "Un seul refresh attendu");
    }

    @Test
    void failsWhenInitialTokenFetchThrows() {
        // Pas besoin de serveur : l'erreur survient dans le request processor
        var oidc = new OidcServiceStub("INIT", "REFRESHED", /*throwOnGet*/ true, /*throwOnRefresh*/ false);
        WebClient client = clientWithFilter(oidc);

        Executable call = () -> client.get().uri("/whatever")
                .retrieve().toEntity(String.class).block();

        assertThrows(RuntimeException.class, call);
        assertEquals(1, oidc.getCalls());
        assertEquals(0, oidc.refreshCalls());
    }

    @Test
    void failsWhenRefreshThrows_after401() {
        server.createContext("/boom", ex -> {
            ex.sendResponseHeaders(401, -1); // force chemin de retry
            ex.close();
        });

        var oidc = new OidcServiceStub("INIT", "REFRESHED", /*throwOnGet*/ false, /*throwOnRefresh*/ true);
        WebClient client = clientWithFilter(oidc);

        Executable call = () -> client.get().uri("/boom")
                .retrieve().toEntity(String.class).block();

        RuntimeException ex = assertThrows(RuntimeException.class, call);
        assertTrue(ex.getMessage().contains("Failed to refresh token"));
        // getServiceAccountToken est appelé au 1er passage (avant 401)
        assertTrue(oidc.getCalls() >= 1);
        assertEquals(1, oidc.refreshCalls(), "Un refresh a été tenté et a échoué");
    }
}
