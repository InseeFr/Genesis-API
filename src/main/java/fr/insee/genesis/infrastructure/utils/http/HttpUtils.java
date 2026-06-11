package fr.insee.genesis.infrastructure.utils.http;

import lombok.experimental.UtilityClass;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.io.IOException;

@UtilityClass
public class HttpUtils {
    public static <T, R> ResponseEntity<R> makeApiCall(
            String baseUrl,
            String path,
            HttpMethod method,
            T requestBody,
            Class<R> responseType,
            OidcService oidcService
    ) throws IOException {

        WebClient webClient = WebClient.builder()
                .baseUrl(baseUrl)
                .filter(PerretAuthWebClientFilter.perretAuthFilter(oidcService))
                .build();

        WebClient.RequestBodySpec request = webClient
                .method(method)
                .uri(path)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + oidcService.getServiceAccountToken());

        WebClient.ResponseSpec responseSpec;

        if (requestBody != null) {
            responseSpec = request
                    .bodyValue(requestBody)
                    .retrieve();
        } else {
            responseSpec = request
                    .retrieve();
        }

        if (responseType == null || responseType == Void.class) {
            return (ResponseEntity<R>) responseSpec.toBodilessEntity().block();
        }

        return responseSpec.toEntity(responseType).block();
    }
}
