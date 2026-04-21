package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.configuration.auth.security.DefaultSecurityConfig;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.data.mongo.MongoDataAutoConfiguration;
import org.springframework.boot.autoconfigure.mongo.MongoAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpMethod.HEAD;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.request;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RawResponseController.class)
//Disable OIDC
@TestPropertySource(properties = {
        "fr.insee.genesis.authentication=NONE"
})
@Import({DefaultSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class RawResponseControllerTest {

    @Autowired
    private MockMvc mockMvc; // Simulates HTTP requests to the REST endpoints

    @MockitoBean
    private MongoTemplate mongoTemplate;
    @MockitoBean
    private SurveyUnitApiPort surveyUnitApiPort;
    @MockitoBean
    private LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    @MockitoBean
    private RawResponseApiPort rawResponseApiPort;
    @MockitoBean
    private RawResponseInputRepository rawRepository;

    @Nested
    @DisplayName("PUT /responses/raw/lunatic-json/save tests")
    class SaveRawResponsesFromJsonBodyTests {

        @Test
        @DisplayName("Should return 201 on success")
        void save_shouldReturn201() throws Exception {
            // WHEN / THEN
            mockMvc.perform(put("/responses/raw/lunatic-json/save")
                            .with(csrf())
                            .param("campaignName", "CAMPAIGN")
                            .param("questionnaireId", "QUEST01")
                            .param("interrogationId", "INTERRO01")
                            .param("mode", Mode.WEB.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isCreated());
        }

        @Test
        @DisplayName("Should return success message with interrogationId")
        void save_shouldReturnSuccessMessage() throws Exception {
            // GIVEN

            // WHEN / THEN
            mockMvc.perform(put("/responses/raw/lunatic-json/save")
                            .with(csrf())
                            .param("campaignName", "CAMPAIGN")
                            .param("questionnaireId", "QUEST01")
                            .param("interrogationId", "INTERRO01")
                            .param("mode", Mode.WEB.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(content().string(containsString("INTERRO01")));
        }

        @Test
        @DisplayName("Should return 500 when port throws an exception")
        void save_portThrows_shouldReturn500() throws Exception {
            // GIVEN
            doThrow(new RuntimeException("DB error")).when(lunaticJsonRawDataApiPort).save(any());

            // WHEN / THEN
            mockMvc.perform(put("/responses/raw/lunatic-json/save")
                            .with(csrf())
                            .param("campaignName", "CAMPAIGN")
                            .param("questionnaireId", "QUEST01")
                            .param("interrogationId", "INTERRO01")
                            .param("mode", Mode.WEB.name())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isInternalServerError())
                    .andExpect(content().string(containsString("Unexpected error")));
        }
    }

    @Nested
    @DisplayName("POST /raw-responses tests")
    class SaveRawResponsesFromDtoTests {

        @Test
        @DisplayName("Should return 201 and call repository")
        void saveDto_shouldReturn201AndCallRepository() throws Exception {
            // GIVEN
            String body = getFiliereModelRawResponseBody();

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isCreated())
                    .andExpect(content().string(containsString("INTERRO01")));

            verify(rawRepository).saveAsRawJson(any());
        }

        private String getFiliereModelRawResponseBody() {
            return """
                    {
                          "partitionId": "RAWDATATESTCAMPAIGN",
                          "collectionInstrumentId": "TESTQUEST",
                          "usualSurveyUnitId": "TESTUE00001",
                          "interrogationId": "INTERRO01",
                          "mode": "CAWI",
                          "isCapturedIndirectly": true,
                          "questionnaireState": "FINISHED",
                          "data": {}
                    }
                    """;
        }
    }

    @Nested
    @DisplayName("POST /raw-responses/process tests")
    class ProcessRawResponsesTests {

        @Test
        @DisplayName("Should return 200 with count message when no formatted data")
        void process_noFormatted_shouldReturnCountMessage() throws Exception {
            // GIVEN
            when(rawResponseApiPort.processRawResponsesByInterrogationIds(eq("QUEST01"), anyList(), anyList()))
                    .thenReturn(new DataProcessResult(10, 0, new ArrayList<>()));

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses/process")
                            .with(csrf())
                            .param("collectionInstrumentId", "QUEST01")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"i1\",\"i2\"]"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("10")))
                    .andExpect(content().string(containsString("QUEST01")));
        }

        @Test
        @DisplayName("Should return 200 with formatted count message when formatted data present")
        void process_withFormatted_shouldReturnFormattedCountMessage() throws Exception {
            // GIVEN
            when(rawResponseApiPort.processRawResponsesByInterrogationIds(eq("QUEST01"), anyList(), anyList()))
                    .thenReturn(new DataProcessResult(10, 3, new ArrayList<>()));

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses/process")
                            .with(csrf())
                            .param("collectionInstrumentId", "QUEST01")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"i1\"]"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("10")))
                    .andExpect(content().string(containsString("3")))
                    .andExpect(content().string(containsString("FORMATTED")));
        }

        @Test
        @DisplayName("Should return GenesisException status when port throws")
        void process_genesisException_shouldReturnExceptionStatus() throws Exception {
            // GIVEN
            when(rawResponseApiPort.processRawResponsesByInterrogationIds(anyString(), anyList(), anyList()))
                    .thenThrow(new GenesisException(404, "Not found"));

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses/process")
                            .with(csrf())
                            .param("collectionInstrumentId", "QUEST01")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[\"i1\"]"))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Not found")));
        }
    }

    @Nested
    @DisplayName("POST /raw-responses/{collectionInstrumentId}/process tests")
    class ProcessRawResponsesByCollectionInstrumentIdTests {

        @Test
        @DisplayName("Should return 200 with count message")
        void processByCollectionInstrumentId_shouldReturn200() throws Exception {
            // GIVEN
            when(rawResponseApiPort.processRawResponsesByInterrogationIds("QUEST01"))
                    .thenReturn(new DataProcessResult(5, 0, new ArrayList<>()));

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses/QUEST01/process")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("5")));
        }

        @Test
        @DisplayName("Should return GenesisException status when port throws")
        void processByCollectionInstrumentId_genesisException_shouldReturnExceptionStatus() throws Exception {
            // GIVEN
            when(rawResponseApiPort.processRawResponsesByInterrogationIds(anyString()))
                    .thenThrow(new GenesisException(422, "Unprocessable"));

            // WHEN / THEN
            mockMvc.perform(post("/raw-responses/QUEST01/process")
                            .with(csrf()))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().string(containsString("Unprocessable")));
        }
    }

    @Nested
    @DisplayName("GET /raw-responses/unprocessed/collection-instrument-ids tests")
    class GetUnprocessedCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with list of collection instrument ids")
        void getUnprocessed_shouldReturn200WithIds() throws Exception {
            // GIVEN
            when(rawResponseApiPort.getUnprocessedCollectionInstrumentIds())
                    .thenReturn(List.of("QUEST01", "QUEST02"));

            // WHEN / THEN
            mockMvc.perform(get("/raw-responses/unprocessed/collection-instrument-ids"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("QUEST01")));
        }
    }

    @Nested
    @DisplayName("GET /responses/raw/lunatic-json/get/unprocessed tests")
    class GetUnprocessedJsonRawDataTests {

        @Test
        @DisplayName("Should return 200 with unprocessed data ids")
        void getUnprocessedJson_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.getUnprocessedDataIds()).thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/get/unprocessed"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /responses/raw/lunatic-json/get/unprocessed/questionnaireIds tests")
    class GetUnprocessedJsonRawDataQuestionnairesIdsTests {

        @Test
        @DisplayName("Should return 200 with questionnaire ids set")
        void getUnprocessedQuestionnaireIds_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.getUnprocessedDataQuestionnaireIds())
                    .thenReturn(Set.of("QUEST01", "QUEST02"));

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/get/unprocessed/questionnaireIds"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("QUEST01")));
        }
    }

    @Nested
    @DisplayName("POST /responses/raw/lunatic-json/{questionnaireId}/process tests")
    class ProcessJsonRawDataByQuestionnaireIdTests {

        @Test
        @DisplayName("Should return 200 with count message")
        void processByQuestionnaireId_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.processRawData("QUEST01"))
                    .thenReturn(new DataProcessResult(8, 0, new ArrayList<>()));

            // WHEN / THEN
            mockMvc.perform(post("/responses/raw/lunatic-json/QUEST01/process")
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("8")));
        }

        @Test
        @DisplayName("Should return GenesisException status when port throws")
        void processByQuestionnaireId_genesisException_shouldReturnExceptionStatus() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.processRawData(anyString()))
                    .thenThrow(new GenesisException(404, "Questionnaire not found"));

            // WHEN / THEN
            mockMvc.perform(post("/responses/raw/lunatic-json/QUEST01/process")
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Questionnaire not found")));
        }
    }

    @Nested
    @DisplayName("GET /responses/raw/lunatic-json/{campaignId} tests")
    class GetLunaticJsonRawDataTests {

        @Test
        @DisplayName("Should return 200 with paged model")
        void getLunaticJson_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.findRawDataByCampaignIdAndDate(
                    eq("CAMPAIGN"), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1000), 0));

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/CAMPAIGN"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should accept optional startDate and endDate query params")
        void getLunaticJson_withDates_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.findRawDataByCampaignIdAndDate(
                    anyString(), any(Instant.class), any(Instant.class), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/CAMPAIGN")
                            .param("startDate", "2024-01-01T00:00:00Z")
                            .param("endDate", "2024-12-31T23:59:59Z"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /responses/raw/lunatic-json/by-questionnaire/{questionnaireId} tests")
    class GetLunaticJsonByQuestionnaireTests {

        @Test
        @DisplayName("Should return 200 with paged model")
        void getByQuestionnaire_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.findRawDataByQuestionnaireId(eq("QUEST01"), any()))
                    .thenReturn(new PageImpl<>(List.of()));

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/by-questionnaire/QUEST01"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("HEAD /responses/raw/lunatic-json/{interrogationId} tests")
    class ExistsLunaticJsonByInterrogationIdTests {

        @Test
        @DisplayName("Should return 200 when interrogation exists")
        void existsLunaticJson_exists_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.existsByInterrogationId("INTERRO01")).thenReturn(true);

            // WHEN / THEN
            mockMvc.perform(request(HEAD, "/responses/raw/lunatic-json/INTERRO01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 404 when interrogation does not exist")
        void existsLunaticJson_notFound_shouldReturn404() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.existsByInterrogationId("INTERRO01")).thenReturn(false);

            // WHEN / THEN
            mockMvc.perform(request(HEAD, "/responses/raw/lunatic-json/INTERRO01"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /raw-responses/{campaignId} tests")
    class GetRawResponsesByCampaignTests {

        @Test
        @DisplayName("Should return 200 with paged model")
        void getRawResponses_shouldReturn200() throws Exception {
            // GIVEN
            when(rawResponseApiPort.findRawResponseDataByCampaignIdAndDate(
                    eq("CAMPAIGN"), any(), any(), any()))
                    .thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 1000), 0));

            // WHEN / THEN
            mockMvc.perform(get("/raw-responses/CAMPAIGN"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /raw-responses/by-collection-instrument/{collectionInstrumentId} tests")
    class GetRawResponsesByCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with paged model")
        void getByCollectionInstrument_shouldReturn200() throws Exception {
            // GIVEN
            // All nulls as we only test status
            RawResponseModel rawResponseModel = new RawResponseModel(
                    null, null, null, null, null, null, null
            );

            when(rawResponseApiPort.findRawResponseDataByCollectionInstrumentId(eq("QUEST01"), any()))
                    .thenReturn(new PageImpl<>(List.of(rawResponseModel), PageRequest.of(0, 1000), 1));

            // WHEN / THEN
            mockMvc.perform(get("/raw-responses/by-collection-instrument/QUEST01"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("HEAD /raw-responses/{interrogationId} tests")
    class ExistsByInterrogationIdTests {

        @Test
        @DisplayName("Should return 200 when interrogation exists")
        void exists_found_shouldReturn200() throws Exception {
            // GIVEN
            when(rawResponseApiPort.existsByInterrogationId("INTERRO01")).thenReturn(true);

            // WHEN / THEN
            mockMvc.perform(request(HEAD, "/raw-responses/INTERRO01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 404 when interrogation does not exist")
        void exists_notFound_shouldReturn404() throws Exception {
            // GIVEN
            when(rawResponseApiPort.existsByInterrogationId("INTERRO01")).thenReturn(false);

            // WHEN / THEN
            mockMvc.perform(request(HEAD, "/raw-responses/INTERRO01"))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /responses/raw/lunatic-json/processed/ids tests")
    class GetProcessedDataIdsSinceHoursTests {

        @Test
        @DisplayName("Should return 200 with processed ids map")
        void getProcessedIds_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.findProcessedIdsgroupedByQuestionnaireSince(any()))
                    .thenReturn(Map.of("QUEST01", List.of("i1", "i2")));

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/processed/ids")
                            .param("questionnaireId", "QUEST01"))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("QUEST01")));
        }

        @Test
        @DisplayName("Should use default sinceHours=24 when not specified")
        void getProcessedIds_defaultHours_shouldReturn200() throws Exception {
            // GIVEN
            when(lunaticJsonRawDataApiPort.findProcessedIdsgroupedByQuestionnaireSince(any()))
                    .thenReturn(Map.of());

            // WHEN / THEN
            mockMvc.perform(get("/responses/raw/lunatic-json/processed/ids")
                            .param("questionnaireId", "QUEST01"))
                    .andExpect(status().isOk());
        }
    }
}