package fr.insee.genesis.configuration.rest;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.http.client.MockClientHttpRequest;
import org.springframework.mock.http.client.MockClientHttpResponse;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.net.URI;

import static org.assertj.core.api.Assertions.assertThat;

class UserJwtBearerInterceptorTest {

    private final UserJwtBearerInterceptor interceptor = new UserJwtBearerInterceptor();

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void shouldAddBearerAuthHeader_whenJwtAuthenticationPresent() throws Exception {
        // given
        Jwt jwt = Jwt.withTokenValue("token-123")
                .header("alg", "none")
                .claim("sub", "user")
                .build();

        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/test"));
        byte[] body = new byte[0];

        ClientHttpRequestExecution execution = (req, b) -> {
            assertThat(req.getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                    .isEqualTo("Bearer token-123");
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        // when
        ClientHttpResponse response = interceptor.intercept(request, body, execution);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }

    @Test
    void shouldNotAddBearerAuthHeader_whenNoJwtAuthentication() throws Exception {
        // given
        SecurityContextHolder.clearContext();

        MockClientHttpRequest request = new MockClientHttpRequest(HttpMethod.GET, URI.create("http://localhost/test"));
        byte[] body = new byte[0];

        ClientHttpRequestExecution execution = (req, b) -> {
            assertThat(req.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)).isFalse();
            return new MockClientHttpResponse(new byte[0], HttpStatus.OK);
        };

        // when
        ClientHttpResponse response = interceptor.intercept(request, body, execution);

        // then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
    }
}
