package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(HealthCheckController.class)
@TestPropertySource(properties = "fr.insee.genesis.version=1.2.3-test")
class HealthCheckControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurveyUnitApiPort surveyUnitApiPort;

    @MockitoBean
    private DataProcessingContextApiPort dataProcessingContextApiPort;

    // -------------------------------------------------------------------------
    // GET /health-check
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /health-check tests")
    class HealthcheckTests {

        @Test
        @WithMockUser(username = "test-user")
        @DisplayName("Should return 200 OK")
        void healthcheck_shouldReturn200() throws Exception {
            // GIVEN

            // WHEN / THEN
            mockMvc.perform(get("/health-check"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser(username = "test-user")
        @DisplayName("Should include 'OK' in response body")
        void healthcheck_shouldContainOk() throws Exception {
            // GIVEN

            // WHEN / THEN
            mockMvc.perform(get("/health-check"))
                    .andExpect(content().string(containsString("OK")));
        }

        @Test
        @WithMockUser(username = "test-user")
        @DisplayName("Should include the project version in response body")
        void healthcheck_shouldContainVersion() throws Exception {
            // GIVEN

            // WHEN / THEN
            mockMvc.perform(get("/health-check"))
                    .andExpect(content().string(containsString("1.2.3-test")));
        }

        @Test
        @WithMockUser(username = "test-user")
        @DisplayName("Should include the authenticated username in response body")
        void healthcheck_shouldContainUsername() throws Exception {
            // GIVEN

            // WHEN / THEN
            mockMvc.perform(get("/health-check"))
                    .andExpect(content().string(containsString("test-user")));
        }
    }

    // -------------------------------------------------------------------------
    // GET /health-check/mongoDb
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("GET /health-check/mongoDb tests")
    class HealthcheckMongoTests {

        @Test
        @WithMockUser
        @DisplayName("Should return 200 OK")
        void healthcheckMongo_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(42L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(7L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(status().isOk());
        }

        @Test
        @WithMockUser
        @DisplayName("Should include 'MongoDB OK' in response body")
        void healthcheckMongo_shouldContainMongoDbOk() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(0L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(0L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(content().string(containsString("MongoDB OK")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should include the response count from surveyUnitApiPort")
        void healthcheckMongo_shouldContainResponseCount() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(42L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(0L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(content().string(containsString("42")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should include the context count from dataProcessingContextApiPort")
        void healthcheckMongo_shouldContainContextCount() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(0L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(7L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(content().string(containsString("7")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should include 'Responses' label in response body")
        void healthcheckMongo_shouldContainResponsesLabel() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(0L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(0L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(content().string(containsString("Responses")));
        }

        @Test
        @WithMockUser
        @DisplayName("Should include 'Contexts' label in response body")
        void healthcheckMongo_shouldContainContextsLabel() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.countResponses()).thenReturn(0L);
            when(dataProcessingContextApiPort.countContexts()).thenReturn(0L);

            // WHEN / THEN
            mockMvc.perform(get("/health-check/mongoDb"))
                    .andExpect(content().string(containsString("Contexts")));
        }
    }
}