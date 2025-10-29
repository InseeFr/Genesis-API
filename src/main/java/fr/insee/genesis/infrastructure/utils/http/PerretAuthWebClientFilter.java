package fr.insee.genesis.infrastructure.utils.http;

import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.ClientRequest;
import org.springframework.web.reactive.function.client.ExchangeFilterFunction;
import reactor.core.publisher.Mono;

import java.io.IOException;

@Slf4j
@UtilityClass
public class PerretAuthWebClientFilter {
    public static ExchangeFilterFunction perretAuthFilter(OidcService oidcService) {
        return ExchangeFilterFunction.ofRequestProcessor(request -> {
            try {
                String token = oidcService.getServiceAccountToken();
                ClientRequest newRequest = ClientRequest.from(request)
                        .headers(headers -> headers.setBearerAuth(token))
                        .build();
                return Mono.just(newRequest);
            } catch (IOException e) {
                return Mono.error(new RuntimeException("Failed to get token", e));
            }
        }).andThen((request, next) -> next.exchange(request)
                .flatMap(response -> {
                    if (response.statusCode().value() == 401) {
                        log.warn("Received 401, refreshing token and retrying once");
                        try {
                            oidcService.retrieveServiceAccountToken(); // Force refresh
                            String refreshedToken = oidcService.getServiceAccountToken();
                            ClientRequest retryRequest = ClientRequest.from(request)
                                    .headers(headers -> headers.setBearerAuth(refreshedToken))
                                    .build();
                            return next.exchange(retryRequest);
                        } catch (IOException e) {
                            return Mono.error(new RuntimeException("Failed to refresh token", e));
                        }
                    }
                    return Mono.just(response);
                }));
    }
}
