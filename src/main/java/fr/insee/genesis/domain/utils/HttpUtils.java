package fr.insee.genesis.domain.utils;

import lombok.experimental.UtilityClass;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

@UtilityClass
public class HttpUtils {
    public static <T, R> ResponseEntity<R> makeApiCall(String url, HttpMethod method, T requestBody, Class<R> responseType){
        RestTemplate restTemplate = new RestTemplateBuilder().build();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<T> requestEntity = new HttpEntity<>(requestBody, headers);

        return restTemplate.exchange(url, method, requestEntity, responseType);
    }
}
