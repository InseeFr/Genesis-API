package fr.insee.genesis.infrastructure.utils.http;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Tests « boite noire » de HttpUtils sans lib externe :
 * - serveur HTTP embarqué JDK comme double de l'API
 * - vérification des en-têtes, du body et de la désérialisation JSON
 */
class HttpUtilsTest {

    private HttpServer server;
    private int port;

    @BeforeEach
    void startServer() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0); // port auto
        port = server.getAddress().getPort();
        server.setExecutor(Executors.newSingleThreadExecutor());
        server.start();
    }

    @AfterEach
    void stopServer() {
        if (server != null){ server.stop(0);}
    }

    // --- DTO simples pour sérialiser/désérialiser via Jackson déjà présent avec Spring WebFlux
    public static class RequestDto {
        public String name;
        public int age;
        public RequestDto() {}
        public RequestDto(String name, int age) { this.name = name; this.age = age; }
    }
    public static class ResponseDto {
        public String status;
        public String echoedName;
        public ResponseDto() {}
        public ResponseDto(String status, String echoedName) { this.status = status; this.echoedName = echoedName; }
    }


    @Test
    void makeApiCall_ok_post_json_and_auth_header() throws Exception {
        // Arrange: route /test qui vérifie méthode, headers, body et renvoie un JSON
        server.createContext("/test", new HttpHandler() {
            @Override public void handle(HttpExchange ex) throws IOException {
                try {
                    assertEquals("POST", ex.getRequestMethod(), "Méthode HTTP inattendue");

                    // Vérifie header Authorization et Content-Type
                    var auth = ex.getRequestHeaders().getFirst("Authorization");
                    assertEquals("Bearer TEST_TOKEN", auth, "Header Authorization manquant/incorrect");
                    var ct = ex.getRequestHeaders().getFirst("Content-Type");
                    assertTrue(ct != null && ct.contains("application/json"), "Content-Type JSON attendu");

                    // Lis le body
                    var reqBody = new String(ex.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                    assertTrue(reqBody.contains("\"name\":\"Alice\""), "Body JSON ne contient pas name=Alice");
                    assertTrue(reqBody.contains("\"age\":30"), "Body JSON ne contient pas age=30");

                    // Réponse JSON
                    var resp = """
                               {"status":"ok","echoedName":"Alice"}
                               """;
                    ex.getResponseHeaders().add("Content-Type", "application/json");
                    byte[] bytes = resp.getBytes(StandardCharsets.UTF_8);
                    ex.sendResponseHeaders(200, bytes.length);
                    try (OutputStream os = ex.getResponseBody()) { os.write(bytes); }
                } finally {
                    ex.close();
                }
            }
        });

        var baseUrl = "http://localhost:" + port;
        var oidc = new OidcServiceStub("TEST_TOKEN");
        var request = new RequestDto("Alice", 30);

        // Act
        ResponseEntity<ResponseDto> response = HttpUtils.makeApiCall(
                baseUrl,
                "/test",
                HttpMethod.POST,
                request,
                ResponseDto.class,
                oidc
        );

        // Assert
        assertNotNull(response);
        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals("ok", response.getBody().status);
        assertEquals("Alice", response.getBody().echoedName);
    }

    @Test
    void makeApiCall_propagates_5xx_error()  {
        server.createContext("/boom", ex -> {
            ex.sendResponseHeaders(500, -1); // pas de corps
            ex.close();
        });

        var baseUrl = "http://localhost:" + port;
        var oidc = new OidcServiceStub("TEST_TOKEN");

        final RequestDto req = new RequestDto("Bob", 42);

        Executable call = () -> HttpUtils.makeApiCall(
                baseUrl, "/boom", HttpMethod.POST, req, ResponseDto.class, oidc
        );

        // Check the classpath
        RuntimeException exception = assertThrows(RuntimeException.class, call);
        assertTrue(exception.getClass().getName().contains("WebClientResponseException"));
    }
}
