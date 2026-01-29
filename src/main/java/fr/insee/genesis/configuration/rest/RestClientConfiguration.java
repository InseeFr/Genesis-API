package fr.insee.genesis.configuration.rest;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.client.InterceptingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.util.List;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class RestClientConfiguration {
    @Value("${fr.insee.genesis.platine.url}")
    private final String platineManagementUrl;


    @Bean("platineRestClientBuilder")
    public RestClient.Builder platineRestClientBuilder() {
        return RestClient.builder()
                .defaultHeader(HttpHeaders.ACCEPT, MediaType.APPLICATION_JSON_VALUE)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .baseUrl(platineManagementUrl)
                .requestFactory(new InterceptingClientHttpRequestFactory(
                        new SimpleClientHttpRequestFactory(),
                        List.of(
                                new UserJwtBearerInterceptor(),
                                new LoggingInterceptor()
                        )
                ));
    }
}