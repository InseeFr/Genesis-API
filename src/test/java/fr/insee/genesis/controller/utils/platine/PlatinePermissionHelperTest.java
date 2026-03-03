package fr.insee.genesis.controller.utils.platine;

import com.github.tomakehurst.wiremock.WireMockServer;
import fr.insee.genesis.configuration.rest.RestClientConfiguration;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.wiremock.spring.EnableWireMock;
import org.wiremock.spring.InjectWireMock;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@EnableWireMock
@SpringBootTest(classes = {
        RestClientConfiguration.class,
        PlatinePermissionHelper.class
    },
    properties = {
            "fr.insee.genesis.platine.url=http://localhost:${wiremock.server.port}"
    }
)
class PlatinePermissionHelperTest {

    @InjectWireMock
    private WireMockServer wireMock;

    @AfterEach
    void cleanup() {
        SecurityContextHolder.clearContext();
    }

    @Autowired
    PlatinePermissionHelper helper;

    @Test
    void shouldReturnTrue_andSendBearerToken_whenPermissionGranted() {
        // given
        Jwt jwt = Jwt.withTokenValue("token-123")
                .header("alg", "none")
                .claim("sub", "user")
                .build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt));

        wireMock.stubFor(get(urlPathEqualTo("/api/permissions/check"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-1"))
                .willReturn(aResponse().withStatus(200)));

        // when
        boolean result = helper.hasExportDataPermission("INT-1");

        // then
        assertThat(result).isTrue();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/api/permissions/check"))
                .withHeader("Authorization", equalTo("Bearer token-123"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-1")));
    }

    @Test
    void shouldReturnFalse_whenPlatineReturns403() {
        wireMock.stubFor(get(urlPathEqualTo("/api/permissions/check"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-2"))
                .willReturn(aResponse().withStatus(403)));

        boolean result = helper.hasExportDataPermission("INT-2");

        assertThat(result).isFalse();

        wireMock.verify(getRequestedFor(urlPathEqualTo("/api/permissions/check"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-2")));
    }

    @Test
    void shouldReturnFalse_whenPlatineReturns401() {
        wireMock.stubFor(get(urlPathEqualTo("/api/permissions/check"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-3"))
                .willReturn(aResponse().withStatus(401)));

        boolean result = helper.hasExportDataPermission("INT-3");

        assertThat(result).isFalse();
    }

    @Test
    void shouldThrow_whenPlatineReturns500() {
        wireMock.stubFor(get(urlPathEqualTo("/api/permissions/check"))
                .withQueryParam("permission", equalTo("INTERROGATION_DATA_EXPORT"))
                .withQueryParam("id", equalTo("INT-4"))
                .willReturn(aResponse().withStatus(500)));

        assertThatThrownBy(() -> helper.hasExportDataPermission("INT-4"))
                .isInstanceOf(org.springframework.web.client.RestClientException.class);
    }
}
