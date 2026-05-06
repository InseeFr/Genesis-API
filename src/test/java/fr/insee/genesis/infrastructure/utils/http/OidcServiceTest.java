package fr.insee.genesis.infrastructure.utils.http;

import fr.insee.genesis.configuration.Config;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OidcServiceTest {

    private static final String AUTH_SERVER_URL = "https://auth.example.com";
    private static final String REALM = "my-realm";
    private static final String CLIENT_ID = "my-client";
    private static final String CLIENT_SECRET = "my-secret";
    private static final String EXPECTED_TOKEN_URL =
            "https://auth.example.com/realms/my-realm/protocol/openid-connect/token";
    private static final String VALID_TOKEN = "valid-access-token";
    private static final String VALID_RESPONSE_BODY =
            "{\"access_token\":\"valid-access-token\",\"expires_in\":300}";

    @Mock
    private Config config;

    private OidcService oidcService;
    private MockRestServiceServer mockServer;

    @BeforeEach
    void setUp() {
        when(config.getAuthServerUrl()).thenReturn(AUTH_SERVER_URL);
        when(config.getRealm()).thenReturn(REALM);
        when(config.getServiceClientId()).thenReturn(CLIENT_ID);
        when(config.getServiceClientSecret()).thenReturn(CLIENT_SECRET);

        oidcService = new OidcService(config);

        // Reading a final field via reflection works fine in Java 17+
        // (only writing is blocked). We bind MockRestServiceServer to the
        // internal RestTemplate to intercept HTTP calls without any code change.
        RestTemplate internalRestTemplate =
                (RestTemplate) ReflectionTestUtils.getField(oidcService, "restTemplate");
        Assertions.assertThat(internalRestTemplate).isNotNull();
        mockServer = MockRestServiceServer.createServer(internalRestTemplate);
    }

    @Nested
    @DisplayName("retrieveServiceAccountToken() tests")
    class RetrieveServiceAccountTokenTests {

        @Test
        @DisplayName("Should POST to the correct token URL")
        void retrieveToken_shouldPostToCorrectUrl() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andExpect(method(HttpMethod.POST))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            oidcService.retrieveServiceAccountToken();

            // THEN
            mockServer.verify();
        }

        @Test
        @DisplayName("Should set serviceAccountToken from response body")
        void retrieveToken_shouldStoreToken() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            oidcService.retrieveServiceAccountToken();

            // THEN
            assertThat(ReflectionTestUtils.getField(oidcService, "serviceAccountToken"))
                    .isEqualTo(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should set tokenExpirationTime in the future")
        void retrieveToken_shouldSetExpirationTimeInFuture() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            long before = System.currentTimeMillis();

            // WHEN
            oidcService.retrieveServiceAccountToken();

            // THEN
            Long expirationTime = (Long) ReflectionTestUtils.getField(oidcService, "tokenExpirationTime");
            assertThat(expirationTime).isGreaterThan(before);
        }

        @Test
        @DisplayName("Should throw IOException when response is not 2xx")
        void retrieveToken_non2xxResponse_shouldThrowIOException() {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withUnauthorizedRequest());

            // WHEN / THEN
            assertThatThrownBy(() -> oidcService.retrieveServiceAccountToken())
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should throw IOException when access_token is missing from response body")
        void retrieveToken_missingAccessToken_shouldThrowIOException() {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess("{\"expires_in\":300}", MediaType.APPLICATION_JSON));

            // WHEN / THEN
            assertThatThrownBy(() -> oidcService.retrieveServiceAccountToken())
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("access_token");
        }

        @Test
        @DisplayName("Should throw IOException when access_token is not a String")
        void retrieveToken_accessTokenNotString_shouldThrowIOException() {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(
                            "{\"access_token\":12345,\"expires_in\":300}",
                            MediaType.APPLICATION_JSON));

            // WHEN / THEN
            assertThatThrownBy(() -> oidcService.retrieveServiceAccountToken())
                    .isInstanceOf(IOException.class)
                    .hasMessageContaining("access_token");
        }

        @Test
        @DisplayName("Should throw IOException on HTTP 4xx error")
        void retrieveToken_httpClientError_shouldThrowIOException() {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withStatus(org.springframework.http.HttpStatus.FORBIDDEN));

            // WHEN / THEN
            assertThatThrownBy(() -> oidcService.retrieveServiceAccountToken())
                    .isInstanceOf(IOException.class);
        }

        @Test
        @DisplayName("Should send Content-Type: application/x-www-form-urlencoded")
        void retrieveToken_shouldSendCorrectContentType() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andExpect(header("Content-Type", "application/x-www-form-urlencoded"))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            oidcService.retrieveServiceAccountToken();

            // THEN
            mockServer.verify();
        }

        @Test
        @DisplayName("Should send client_id and client_secret in request body")
        void retrieveToken_shouldSendCredentialsInBody() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("client_id=" + CLIENT_ID)))
                    .andExpect(content().string(org.hamcrest.Matchers.containsString("client_secret=" + CLIENT_SECRET)))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            oidcService.retrieveServiceAccountToken();

            // THEN
            mockServer.verify();
        }
    }

    @Nested
    @DisplayName("getServiceAccountToken() tests")
    class GetServiceAccountTokenTests {

        @Test
        @DisplayName("Should fetch token when no token is cached")
        void getToken_noTokenCached_shouldFetchAndReturnToken() throws IOException {
            // GIVEN
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            String token = oidcService.getServiceAccountToken();

            // THEN
            mockServer.verify();
            assertThat(token).isEqualTo(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should NOT fetch token when a valid token is cached")
        void getToken_validTokenCached_shouldReturnCachedToken() throws IOException {
            // GIVEN — inject a valid non-expired token directly
            ReflectionTestUtils.setField(oidcService, "serviceAccountToken", VALID_TOKEN);
            ReflectionTestUtils.setField(oidcService, "tokenExpirationTime",
                    System.currentTimeMillis() + 60_000L);

            // WHEN
            String token = oidcService.getServiceAccountToken();

            // THEN — no HTTP call should have been made
            mockServer.verify();
            assertThat(token).isEqualTo(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should re-fetch token when it is expired")
        void getToken_expiredToken_shouldRefetch() throws IOException {
            // GIVEN
            ReflectionTestUtils.setField(oidcService, "serviceAccountToken", "old-token");
            ReflectionTestUtils.setField(oidcService, "tokenExpirationTime",
                    System.currentTimeMillis() - 1_000L);
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            String token = oidcService.getServiceAccountToken();

            // THEN
            mockServer.verify();
            assertThat(token).isEqualTo(VALID_TOKEN);
        }

        @Test
        @DisplayName("Should re-fetch token when within the 5-second expiry margin")
        void getToken_tokenAboutToExpire_shouldRefetch() throws IOException {
            // GIVEN — expires in 3 seconds, within the 5-second safety margin
            ReflectionTestUtils.setField(oidcService, "serviceAccountToken", "soon-expired");
            ReflectionTestUtils.setField(oidcService, "tokenExpirationTime",
                    System.currentTimeMillis() + 3_000L);
            mockServer.expect(requestTo(EXPECTED_TOKEN_URL))
                    .andRespond(withSuccess(VALID_RESPONSE_BODY, MediaType.APPLICATION_JSON));

            // WHEN
            String token = oidcService.getServiceAccountToken();

            // THEN
            mockServer.verify();
            assertThat(token).isEqualTo(VALID_TOKEN);
        }
    }

    @Nested
    @DisplayName("getConfig() tests")
    class GetConfigTests {

        @Test
        @DisplayName("getConfig() should return the injected Config")
        void getConfig_shouldReturnConfig() {
            // GIVEN

            // WHEN
            Config result = oidcService.getConfig();

            // THEN
            assertThat(result).isEqualTo(config);
        }
    }
}