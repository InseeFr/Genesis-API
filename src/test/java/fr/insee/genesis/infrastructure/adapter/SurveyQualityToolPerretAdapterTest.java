package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.infrastructure.utils.http.HttpUtils;
import fr.insee.genesis.infrastructure.utils.http.OidcService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SurveyQualityToolPerretAdapterTest {

    private static final String SURVEY_QUALITY_TOOL_URL = "https://perret.example.com";
    private static final String INTERROGATIONS_PATH = "/interrogations";

    @Mock
    private Config config;

    @Mock
    private OidcService oidcService;

    @InjectMocks
    private SurveyQualityToolPerretAdapter adapter;

    @Nested
    @DisplayName("sendProcessedIds() tests")
    class SendProcessedIdsTests {

        @Test
        @DisplayName("Should call HttpUtils.makeApiCall with correct url, path, method, body and oidcService")
        void sendProcessedIds_shouldCallHttpUtilsWithCorrectArguments() throws IOException {
            // GIVEN
            Map<String, Set<String>> processedIds = Map.of("questionnaire-1", Set.of("i1", "i2"));
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenReturn(ResponseEntity.ok().build());

                // WHEN
                adapter.sendProcessedIds(processedIds);

                // THEN
                httpUtilsMock.verify(() -> HttpUtils.makeApiCall(
                        eq(SURVEY_QUALITY_TOOL_URL),
                        eq(INTERROGATIONS_PATH),
                        eq(HttpMethod.POST),
                        eq(processedIds),
                        eq(Object.class),
                        eq(oidcService)
                ));
            }
        }

        @Test
        @DisplayName("Should use the url from config")
        void sendProcessedIds_shouldUseUrlFromConfig() throws IOException {
            // GIVEN
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenReturn(ResponseEntity.ok().build());

                // WHEN
                adapter.sendProcessedIds(Map.of());

                // THEN
                verify(config).getSurveyQualityToolUrl();
                httpUtilsMock.verify(() -> HttpUtils.makeApiCall(
                        eq(SURVEY_QUALITY_TOOL_URL), any(), any(), any(), any(), any()
                ));
            }
        }

        @Test
        @DisplayName("Should use POST as HTTP method")
        void sendProcessedIds_shouldUsePostMethod() throws IOException {
            // GIVEN
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenReturn(ResponseEntity.ok().build());

                // WHEN
                adapter.sendProcessedIds(Map.of());

                // THEN
                httpUtilsMock.verify(() -> HttpUtils.makeApiCall(
                        any(), any(), eq(HttpMethod.POST), any(), any(), any()
                ));
            }
        }

        @Test
        @DisplayName("Should return the ResponseEntity from HttpUtils")
        void sendProcessedIds_shouldReturnHttpUtilsResponse() throws IOException {
            // GIVEN
            ResponseEntity<Object> expected = ResponseEntity.status(HttpStatus.OK).build();
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenReturn(expected);

                // WHEN
                ResponseEntity<Object> result = adapter.sendProcessedIds(Map.of());

                // THEN
                assertThat(result).isEqualTo(expected);
            }
        }

        @Test
        @DisplayName("Should propagate IOException thrown by HttpUtils")
        void sendProcessedIds_shouldPropagateIOException() {
            // GIVEN
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenThrow(new IOException("Connection refused"));

                // WHEN / THEN
                org.assertj.core.api.Assertions.assertThatThrownBy(
                                () -> adapter.sendProcessedIds(Map.of()))
                        .isInstanceOf(IOException.class)
                        .hasMessageContaining("Connection refused");
            }
        }

        @Test
        @DisplayName("Should work with an empty processedIds map")
        void sendProcessedIds_emptyMap_shouldCallHttpUtils() throws IOException {
            // GIVEN
            when(config.getSurveyQualityToolUrl()).thenReturn(SURVEY_QUALITY_TOOL_URL);

            try (MockedStatic<HttpUtils> httpUtilsMock = mockStatic(HttpUtils.class)) {
                httpUtilsMock.when(() -> HttpUtils.makeApiCall(any(), any(), any(), any(), any(), any()))
                        .thenReturn(ResponseEntity.ok().build());

                // WHEN
                adapter.sendProcessedIds(Map.of());

                // THEN
                httpUtilsMock.verify(() -> HttpUtils.makeApiCall(
                        any(), any(), any(), eq(Map.of()), any(), any()
                ));
            }
        }
    }
}