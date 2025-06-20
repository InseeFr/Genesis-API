package fr.insee.genesis.domain.utils;

import lombok.experimental.UtilityClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@UtilityClass
public class HttpUtils {
    public static <T, R> ResponseEntity<R> makeApiCall(String baseUrl,
                                                       String path,
                                                       HttpMethod method,
                                                       T requestBody,
                                                       Class<R> responseType){
        WebClient webClient = WebClient.create(baseUrl);
        WebClient.ResponseSpec responseSpec = webClient
                .method(method)
                .uri(path)
                .bodyValue(requestBody)
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .retrieve();
        Mono<ResponseEntity<R>> response = responseSpec.toEntity(responseType);
        return response.block();
    }
}
