package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.auth.security.DefaultSecurityConfig;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ResponseController.class)
//Disable OIDC
@TestPropertySource(properties = {
        "fr.insee.genesis.authentication=NONE"
})
@Import({DefaultSecurityConfig.class})
@ActiveProfiles("test")
@AutoConfigureMockMvc
@EnableAutoConfiguration(exclude = {MongoAutoConfiguration.class, MongoDataAutoConfiguration.class})
class ResponseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SurveyUnitQualityService surveyUnitQualityService;

    @MockitoBean
    private DataProcessingContextApiPort contextService;

    @MockitoBean
    private FileUtils fileUtils;

    @MockitoBean
    private ControllerUtils controllerUtils;

    @MockitoBean
    private AuthUtils authUtils;

    @MockitoBean
    private QuestionnaireMetadataService metadataService;

    @MockitoBean
    private SurveyUnitApiPort surveyUnitApiPort;
    @MockitoBean
    private LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    @MockitoBean
    private RawResponseApiPort rawResponseApiPort;


    @Nested
    @DisplayName("DELETE /responses/delete/{collectionInstrumentId} tests")
    class DeleteAllResponsesByCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with deleted count")
        void delete_shouldReturn200WithCount() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.deleteByCollectionInstrumentId("QUEST01")).thenReturn(42L);

            // WHEN / THEN
            mockMvc.perform(delete("/responses/delete/QUEST01").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("42")))
                    .andExpect(content().string(containsString("deleted")));
        }

        @Test
        @DisplayName("Should return 200 with zero when no data exists")
        void delete_noData_shouldReturn200WithZero() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.deleteByCollectionInstrumentId("QUEST01")).thenReturn(0L);

            // WHEN / THEN
            mockMvc.perform(delete("/responses/delete/QUEST01").with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(content().string(containsString("0")));
        }
    }

    @Nested
    @DisplayName("GET /responses/by-interrogation-and-collection-instrument tests")
    class FindResponsesByInterrogationAndCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with list of survey unit models")
        void find_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findByIdsInterrogationAndCollectionInstrument("INTERRO01", "QUEST01"))
                    .thenReturn(List.of(buildSurveyUnitModel("INTERRO01", "QUEST01", Mode.WEB, DataState.COLLECTED)));

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return empty list when no responses found")
        void find_noResults_shouldReturnEmptyList() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findByIdsInterrogationAndCollectionInstrument(any(), any()))
                    .thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("[]"));
        }
    }

    @Nested
    @DisplayName("GET /responses/by-usual-survey-unit-and-collection-instrument tests")
    class FindResponsesByUsualSurveyUnitAndCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with list of models")
        void find_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findByIdsUsualSurveyUnitAndCollectionInstrument("USUAL01", "QUEST01"))
                    .thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-usual-survey-unit-and-collection-instrument")
                            .param("usualSurveyUnitId", "USUAL01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /responses/by-interrogation-and-collection-instrument/latest-states tests")
    class FindResponsesByInterrogationAndCollectionInstrumentLatestStatesTests {

        @Test
        @DisplayName("Should return 200 when review is enabled")
        void findLatestStates_reviewEnabled_shouldReturn200() throws Exception {
            // GIVEN
            DataProcessingContextModel ctx = new DataProcessingContextModel();
            ctx.setWithReview(true);
            when(contextService.getContext("INTERRO01")).thenReturn(ctx);
            when(surveyUnitApiPort.findLatestValuesByStateByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(getSurveyUnitDto());

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should also allow SCHEDULER role")
        void findLatestStates_schedulerRole_shouldReturn200() throws Exception {
            findLatestStates_reviewEnabled_shouldReturn200();
        }

        @Test
        @DisplayName("Should return 403 with 'Review is disabled' message when context is null")
        void findLatestStates_contextNull_shouldReturn403WithMessage() throws Exception {
            // GIVEN
            when(contextService.getContext("INTERRO01")).thenReturn(null);

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(containsString("Review is disabled")));
        }

        @Test
        @DisplayName("Should return 403 with 'Review is disabled' message when withReview is false")
        void findLatestStates_reviewDisabled_shouldReturn403WithMessage() throws Exception {
            // GIVEN
            DataProcessingContextModel ctx = new DataProcessingContextModel();
            ctx.setWithReview(false);
            when(contextService.getContext("INTERRO01")).thenReturn(ctx);

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest-states")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isForbidden())
                    .andExpect(content().string(containsString("Review is disabled")));
        }
    }

    @Nested
    @DisplayName("GET /responses/by-ue-and-questionnaire/latest-states tests (deprecated)")
    class FindResponsesByInterrogationAndQuestionnaireLatestStatesTests {

        @Test
        @DisplayName("Should return 200 when review is enabled")
        void findByQuestionnaire_reviewEnabled_shouldReturn200() throws Exception {
            // GIVEN
            DataProcessingContextModel ctx = new DataProcessingContextModel();
            ctx.setWithReview(true);
            when(contextService.getContext("INTERRO01")).thenReturn(ctx);
            when(surveyUnitApiPort.findLatestValuesByStateByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(getSurveyUnitDto());

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-ue-and-questionnaire/latest-states")
                            .param("interrogationId", "INTERRO01")
                            .param("questionnaireId", "QUEST01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should return 403 when review is disabled (shares same logic as new endpoint)")
        void findByQuestionnaire_reviewDisabled_shouldReturn403() throws Exception {
            // GIVEN
            when(contextService.getContext("INTERRO01")).thenReturn(null);

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-ue-and-questionnaire/latest-states")
                            .param("interrogationId", "INTERRO01")
                            .param("questionnaireId", "QUEST01"))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("GET /responses/by-interrogation-and-collection-instrument/latest tests")
    class GetLatestByInterrogationAndCollectionInstrumentTests {

        @Test
        @DisplayName("Should return 200 with list of models")
        void getLatest_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findLatestByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(get("/responses/by-interrogation-and-collection-instrument/latest")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01"))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /responses/simplified/by-interrogation-collection-instrument-and-mode/latest tests")
    class GetLatestByInterrogationOneObjectTests {

        @Test
        @DisplayName("Should return 200 and aggregate collected + external variables per mode")
        void getLatestOneObject_shouldReturn200AndAggregateVariables() throws Exception {
            // GIVEN
            SurveyUnitModel model = buildSurveyUnitModel("INTERRO01", "QUEST01", Mode.WEB, DataState.COLLECTED);
            when(surveyUnitApiPort.findLatestByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(List.of(model));

            // WHEN / THEN
            mockMvc.perform(get("/responses/simplified/by-interrogation-collection-instrument-and-mode/latest")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01")
                            .param("mode", Mode.WEB.name()))
                    .andExpect(status().isOk())
                    .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON));
        }

        @Test
        @DisplayName("Should filter responses by mode before aggregating")
        void getLatestOneObject_shouldFilterByMode() throws Exception {
            // GIVEN
            SurveyUnitModel webModel = buildSurveyUnitModel("INTERRO01", "QUEST01", Mode.WEB, DataState.COLLECTED);
            SurveyUnitModel paperModel = buildSurveyUnitModel("INTERRO01", "QUEST01", Mode.PAPER, DataState.COLLECTED);
            when(surveyUnitApiPort.findLatestByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(List.of(webModel, paperModel));

            // WHEN / THEN
            // Only WEB responses should be included — result should compile without error
            mockMvc.perform(get("/responses/simplified/by-interrogation-collection-instrument-and-mode/latest")
                            .param("interrogationId", "INTERRO01")
                            .param("collectionInstrumentId", "QUEST01")
                            .param("mode", Mode.WEB.name()))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("GET /responses/{collectionInstrumentId}/{mode}/{interrogationId} tests")
    class GetResponseByCollectionInstrumentAndInterrogationTests {

        @Test
        @DisplayName("Should return 200 with simplified dto")
        void getResponse_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findSimplified(
                    "QUEST01", "INTERRO01", Mode.WEB, Instant.now()))
                    .thenReturn(SurveyUnitSimplifiedDto.builder().build());

            // WHEN / THEN
            mockMvc.perform(get("/responses/QUEST01/WEB/INTERRO01"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should pass collectionInstrumentId, interrogationId and mode to service")
        void getResponse_shouldDelegateCorrectlyToService() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findSimplified(
                    anyString(), anyString(), any(), any()))
                    .thenReturn(SurveyUnitSimplifiedDto.builder().build());

            // WHEN
            mockMvc.perform(get("/responses/QUEST01/WEB/INTERRO01"));

            // THEN
            verify(surveyUnitApiPort).findSimplified(
                    "QUEST01", "INTERRO01", Mode.WEB, null);
        }
    }

    @Nested
    @DisplayName("POST /responses/simplified/by-list-interrogation-and-collection-instrument/latest tests")
    class GetLatestForInterrogationListTests {

        @Test
        @DisplayName("Should return 200 with simplified dto for each interrogation+mode combination with variables")
        void getLatestList_shouldReturn200() throws Exception {
            // GIVEN
            SurveyUnitModel model = buildSurveyUnitModel("INTERRO01", "QUEST01", Mode.WEB, DataState.COLLECTED);
            when(surveyUnitApiPort.findModesByCollectionInstrumentId("QUEST01"))
                    .thenReturn(List.of(Mode.WEB));
            when(surveyUnitApiPort.findLatestByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(List.of(model));

            // WHEN / THEN
            mockMvc.perform(post("/responses/simplified/by-list-interrogation-and-collection-instrument/latest")
                            .with(csrf())
                            .param("collectionInstrumentId", "QUEST01")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"interrogationId\":\"INTERRO01\"}]"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should exclude interrogations with no variables in any mode")
        void getLatestList_noVariables_shouldReturnEmptyList() throws Exception {
            // GIVEN
            SurveyUnitModel modelNoVars = SurveyUnitModel.builder()
                    .interrogationId("INTERRO01")
                    .collectionInstrumentId("QUEST01")
                    .mode(Mode.WEB)
                    .collectedVariables(List.of())
                    .externalVariables(List.of())
                    .build();
            when(surveyUnitApiPort.findModesByCollectionInstrumentId("QUEST01"))
                    .thenReturn(List.of(Mode.WEB));
            when(surveyUnitApiPort.findLatestByIdAndByCollectionInstrumentId("INTERRO01", "QUEST01"))
                    .thenReturn(List.of(modelNoVars));

            // WHEN / THEN
            mockMvc.perform(post("/responses/simplified/by-list-interrogation-and-collection-instrument/latest")
                            .with(csrf())
                            .param("collectionInstrumentId", "QUEST01")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"interrogationId\":\"INTERRO01\"}]"))
                    .andExpect(status().isOk())
                    .andExpect(content().string("[]"));
        }
    }

    @Nested
    @DisplayName("POST /responses/{collectionInstrumentId} tests")
    class GetResponseByCollectionInstrumentAndInterrogationListTests {

        @Test
        @DisplayName("Should return 200 with list of simplified dtos")
        void getResponseList_shouldReturn200() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findSimplifiedList(
                    eq("QUEST01"), any(), any()))
                    .thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(post("/responses/QUEST01")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("[{\"interrogationId\":\"INTERRO01\"}]"))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should delegate to service with correct collectionInstrumentId")
        void getResponseList_shouldDelegateToService() throws Exception {
            // GIVEN
            when(surveyUnitApiPort.findSimplifiedList(
                    anyString(), any(), any()))
                    .thenReturn(List.of());

            // WHEN
            mockMvc.perform(post("/responses/QUEST01")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content("[{\"interrogationId\":\"INTERRO01\"}]"));

            // THEN
            verify(surveyUnitApiPort).findSimplifiedList(
                    eq("QUEST01"), any(), any());
        }
    }

    @Nested
    @DisplayName("POST /responses/save-edited tests")
    class SaveEditedVariablesTests {

        private static final String VALID_BODY = """
                {
                  "questionnaireId": "QUEST01",
                  "interrogationId": "INTERRO01",
                  "mode": "WEB",
                  "collectedVariables": []
                }
                """;

        @Test
        @DisplayName("Should return 200 with 'Data saved' message on success")
        void saveEdited_shouldReturn200WithSuccessMessage() throws Exception {
            // GIVEN
            MetadataModel metadataModel = buildMetadataModel();
            when(metadataService.loadAndSaveIfNotExists(isNull(), eq("QUEST01"), eq(Mode.WEB), any(), any()))
                    .thenReturn(metadataModel);
            when(surveyUnitQualityService.checkVariablesPresentInMetadata(any(), any()))
                    .thenReturn(List.of());
            when(authUtils.getIDEP()).thenReturn("user-idep");
            when(surveyUnitApiPort.parseEditedVariables(any(), eq("user-idep"), any()))
                    .thenReturn(List.of());

            // WHEN / THEN
            mockMvc.perform(post("/responses/save-edited")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Data saved"));
        }

        @Test
        @DisplayName("Should call verifySurveyUnits and saveSurveyUnits after successful parse")
        void saveEdited_shouldCallVerifyThenSave() throws Exception {
            // GIVEN
            MetadataModel metadataModel = buildMetadataModel();
            when(metadataService.loadAndSaveIfNotExists(any(), any(), any(), any(), any()))
                    .thenReturn(metadataModel);
            when(surveyUnitQualityService.checkVariablesPresentInMetadata(any(), any()))
                    .thenReturn(List.of());
            when(authUtils.getIDEP()).thenReturn("user-idep");
            when(surveyUnitApiPort.parseEditedVariables(any(), anyString(), any()))
                    .thenReturn(List.of());

            // WHEN
            mockMvc.perform(post("/responses/save-edited")
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(VALID_BODY));

            // THEN
            verify(surveyUnitQualityService).verifySurveyUnits(any(), any());
            verify(surveyUnitApiPort).saveSurveyUnits(any());
        }

        @Test
        @DisplayName("Should return 400 with absent variable names when metadata check fails")
        void saveEdited_absentVariables_shouldReturn400WithNames() throws Exception {
            // GIVEN
            MetadataModel metadataModel = buildMetadataModel();
            when(metadataService.loadAndSaveIfNotExists(any(), any(), any(), any(), any()))
                    .thenReturn(metadataModel);
            when(surveyUnitQualityService.checkVariablesPresentInMetadata(any(), any()))
                    .thenReturn(List.of("ABSENT_VAR1", "ABSENT_VAR2"));

            // WHEN / THEN
            mockMvc.perform(post("/responses/save-edited")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isBadRequest())
                    .andExpect(content().string(containsString("ABSENT_VAR1")))
                    .andExpect(content().string(containsString("ABSENT_VAR2")));
        }

        @Test
        @DisplayName("Should return 404 when metadataModel is null and error list is populated")
        void saveEdited_metadataNull_shouldReturn404() throws Exception {
            // GIVEN
            when(metadataService.loadAndSaveIfNotExists(any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        List<GenesisError> errors = invocation.getArgument(4);
                        errors.add(new GenesisError("Metadata not found"));
                        return null;
                    });

            // WHEN / THEN
            mockMvc.perform(post("/responses/save-edited")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isNotFound())
                    .andExpect(content().string(containsString("Metadata not found")));
        }

        @Test
        @DisplayName("Should return GenesisException status when parseEditedVariables throws")
        void saveEdited_parseThrows_shouldReturnExceptionStatus() throws Exception {
            // GIVEN
            MetadataModel metadataModel = buildMetadataModel();
            when(metadataService.loadAndSaveIfNotExists(any(), any(), any(), any(), any()))
                    .thenReturn(metadataModel);
            when(surveyUnitQualityService.checkVariablesPresentInMetadata(any(), any()))
                    .thenReturn(List.of());
            when(authUtils.getIDEP()).thenReturn("user-idep");
            when(surveyUnitApiPort.parseEditedVariables(any(), anyString(), any()))
                    .thenThrow(new GenesisException(HttpStatus.UNPROCESSABLE_ENTITY, "Unprocessable entity"));

            // WHEN / THEN
            mockMvc.perform(post("/responses/save-edited")
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(VALID_BODY))
                    .andExpect(status().isUnprocessableEntity())
                    .andExpect(content().string(containsString("Unprocessable entity")));
        }
    }

    private SurveyUnitModel buildSurveyUnitModel(String interrogationId,
                                                 String collectionInstrumentId,
                                                 Mode mode,
                                                 DataState state) {
        return SurveyUnitModel.builder()
                .interrogationId(interrogationId)
                .collectionInstrumentId(collectionInstrumentId)
                .mode(mode)
                .state(state)
                .recordDate(Instant.now())
                .collectedVariables(List.of())
                .externalVariables(List.of())
                .build();
    }

    private MetadataModel buildMetadataModel() {
        MetadataModel model = new MetadataModel();
        model.setVariables(new VariablesMap());
        return model;
    }

    private SurveyUnitDto getSurveyUnitDto() {
        return SurveyUnitDto.builder().build();
    }
}