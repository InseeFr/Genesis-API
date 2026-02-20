package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.modelefiliere.ModeDto;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RawResponseServiceUnitTest {

    static RawResponseService rawResponseService;

    @Mock
    static RawResponsePersistencePort rawResponsePersistencePort;
    @Mock
    static ControllerUtils controllerUtils;
    @Mock
    static QuestionnaireMetadataService metadataService;
    @Mock
    static SurveyUnitService surveyUnitService;

    @Captor
    private ArgumentCaptor<List<SurveyUnitModel>> surveyUnitModelsCaptor;

    private static final String TEST_VALIDATION_DATE = "2025-11-11T06:00:00Z";

    @BeforeEach
    void init() {
        rawResponseService = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(TestConstants.getConfigStub()),
                TestConstants.getConfigStub(),
                rawResponsePersistencePort
        );
    }

    @Test
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_test() {
        //GIVEN
        List<String> collectionInstrumentIds = new ArrayList<>();
        collectionInstrumentIds.add("QUEST1");
        collectionInstrumentIds.add("QUEST2");
        doReturn(collectionInstrumentIds).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(List.of(ModeDto.CAWI)).when(rawResponsePersistencePort).findModesByCollectionInstrument(any());
        doReturn(new MetadataModel()).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactlyInAnyOrder("QUEST1","QUEST2");
    }

    @Test
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_shouldnt_return_if_no_spec() {
        //GIVEN
        List<String> questionnaireIds = new ArrayList<>();
        questionnaireIds.add("QUEST1"); //No spec
        questionnaireIds.add("TEST-TABLEAUX");
        doReturn(questionnaireIds).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(List.of(ModeDto.CAWI)).when(rawResponsePersistencePort).findModesByCollectionInstrument(any());
        //No mock for metadataservice this time
        metadataService = new QuestionnaireMetadataService(
                mock(QuestionnaireMetadataPersistencePort.class)
        );
        rawResponseService = new RawResponseService(
                new ControllerUtils(new FileUtils(TestConstants.getConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(TestConstants.getConfigStub()),
                TestConstants.getConfigStub(),
                rawResponsePersistencePort
        );


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactly("TEST-TABLEAUX");
    }

    @Nested
    @DisplayName("Non regression tests of #22875 : validation date and questionnaire state in processed responses")
    class ValidationDateAndQuestionnaireStateTests{
        //OK cases
        @ParameterizedTest
        @DisplayName("Process by collection instrument id OK test")
        @EnumSource(RawResponseDto.QuestionnaireStateEnum.class)
        @SneakyThrows
        void processRawResponses_byCollectionInstrumentId_validation_date_questionnaire_state_test(
                RawResponseDto.QuestionnaireStateEnum questionnaireState
        ) {
            //GIVEN
            givenOkCase(questionnaireState);

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessByCollectionInstrumentIdAndInterrogationIdList();

            //THEN
            processRawResponsesThen(questionnaireState, createdModels);
        }

        @ParameterizedTest
        @EnumSource(RawResponseDto.QuestionnaireStateEnum.class)
        @DisplayName("Process by collection instrument id and interrogation id OK test")
        @SneakyThrows
        void processRawResponses_byCollectionInstrumentIdAndInterrogationList_validation_date_questionnaire_state_test(
                RawResponseDto.QuestionnaireStateEnum questionnaireState
        ) {
            //GIVEN
            givenOkCase(questionnaireState);

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessRawResponsesCollectionInstrumentId();

            //THEN
            processRawResponsesThen(questionnaireState, createdModels);
        }

        //Non-blocking exception tests
        //Invalid questionnaire state
        @Test
        @DisplayName("Invalid questionnaireState test (process by collection id)")
        @SneakyThrows
        void processRawResponses_byCollectionInstrumentId_invalid_questionnaire_state_test() {
            //GIVEN
            givenInvalidQuestionnaireState();

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessRawResponsesCollectionInstrumentId();

            //THEN
            processRawResponsesThenQuestionnaireStateNull(createdModels);
        }
        @Test
        @DisplayName("Invalid questionnaireState test (process by collection id and interrogation id list)")
        @SneakyThrows
        void processRawResponses_byCollectionInstrumentIdAndInterrogationList_invalid_questionnaire_state_test() {
            //GIVEN
            givenInvalidQuestionnaireState();

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessByCollectionInstrumentIdAndInterrogationIdList();

            //THEN
            processRawResponsesThenQuestionnaireStateNull(createdModels);
        }

        //Invalid validationDate
        @Test
        @DisplayName("Invalid validationDate test (process by collection id)")
        @SneakyThrows
        void processRawResponses_byCollectionId_invalid_validation_date_test(){
            //GIVEN
            givenInvalidValidationDate();

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessByCollectionInstrumentIdAndInterrogationIdList();

            //THEN
            processRawResponsesThenValidationDateNull(createdModels);
        }
        @Test
        @DisplayName("Invalid validationDate test (process by collection id and interrogation id list)")
        @SneakyThrows
        void processRawResponses_byCollectionIdAndInterrogationIds_invalid_validation_date_test(){
            //GIVEN
            givenInvalidValidationDate();

            //WHEN
            List<SurveyUnitModel> createdModels = whenProcessRawResponsesCollectionInstrumentId();

            //THEN
            processRawResponsesThenValidationDateNull(createdModels);
        }

        //GIVENS
        @SneakyThrows
        private void givenOkCase(RawResponseDto.QuestionnaireStateEnum questionnaireState){
            VariablesMap variablesMap = new VariablesMap();
            MetadataModel metadataModel = new MetadataModel();
            metadataModel.setVariables(variablesMap);
            String validationDate = questionnaireState.equals(RawResponseDto.QuestionnaireStateEnum.FINISHED) ?
                    TEST_VALIDATION_DATE : null;

            List<RawResponseModel> rawResponses = new ArrayList<>();
            RawResponseModel rawResponse = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            rawResponse.payload().put("validationDate", validationDate);
            rawResponse.payload().put("questionnaireState", questionnaireState);
            rawResponse.payload().put("usualSurveyUnitId", TestConstants.DEFAULT_SURVEY_UNIT_ID);
            rawResponse.payload().put("majorModelVersion", 2);
            Map<String, Map<String, Map<String, String>>> dataMap = new HashMap<>();
            dataMap.put("COLLECTED", new HashMap<>());
            dataMap.get("COLLECTED").put("VAR1", new HashMap<>());
            dataMap.get("COLLECTED").get("VAR1").put("COLLECTED", "value");
            rawResponse.payload().put("data", dataMap);
            rawResponses.add(rawResponse);

            //Mocks behaviour
            doReturn(Collections.singletonList(Mode.WEB)).when(controllerUtils).getModesList(any(),any());
            doReturn(Set.of(TestConstants.DEFAULT_INTERROGATION_ID))
                    .when(rawResponsePersistencePort).findUnprocessedInterrogationIdsByCollectionInstrumentId(any());
            doReturn(metadataModel).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());
            doReturn(rawResponses).when(rawResponsePersistencePort).findRawResponses(any(), any(), any());
        }
        @SneakyThrows
        private void givenInvalidQuestionnaireState(){
            VariablesMap variablesMap = new VariablesMap();
            MetadataModel metadataModel = new MetadataModel();
            metadataModel.setVariables(variablesMap);

            List<RawResponseModel> rawResponses = new ArrayList<>();
            RawResponseModel rawResponse = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            rawResponse.payload().put("validationDate", TEST_VALIDATION_DATE);
            rawResponse.payload().put("questionnaireState", "not a questionnaire state");
            rawResponse.payload().put("usualSurveyUnitId", TestConstants.DEFAULT_SURVEY_UNIT_ID);
            rawResponse.payload().put("majorModelVersion", 2);
            Map<String, Map<String, Map<String, String>>> dataMap = new HashMap<>();
            dataMap.put("COLLECTED", new HashMap<>());
            dataMap.get("COLLECTED").put("VAR1", new HashMap<>());
            dataMap.get("COLLECTED").get("VAR1").put("COLLECTED", "value");
            rawResponse.payload().put("data", dataMap);
            rawResponses.add(rawResponse);

            //Mocks behaviour
            doReturn(Collections.singletonList(Mode.WEB)).when(controllerUtils).getModesList(any(),any());
            doReturn(Set.of(TestConstants.DEFAULT_INTERROGATION_ID))
                    .when(rawResponsePersistencePort).findUnprocessedInterrogationIdsByCollectionInstrumentId(any());
            doReturn(metadataModel).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());
            doReturn(rawResponses).when(rawResponsePersistencePort).findRawResponses(any(), any(), any());
        }
        @SneakyThrows
        private void givenInvalidValidationDate(){
            VariablesMap variablesMap = new VariablesMap();
            MetadataModel metadataModel = new MetadataModel();
            metadataModel.setVariables(variablesMap);

            List<RawResponseModel> rawResponses = new ArrayList<>();
            RawResponseModel rawResponse = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            rawResponse.payload().put("validationDate", "not a validation date");
            rawResponse.payload().put("questionnaireState", RawResponseDto.QuestionnaireStateEnum.FINISHED);
            rawResponse.payload().put("usualSurveyUnitId", TestConstants.DEFAULT_SURVEY_UNIT_ID);
            rawResponse.payload().put("majorModelVersion", 2);
            Map<String, Map<String, Map<String, String>>> dataMap = new HashMap<>();
            dataMap.put("COLLECTED", new HashMap<>());
            dataMap.get("COLLECTED").put("VAR1", new HashMap<>());
            dataMap.get("COLLECTED").get("VAR1").put("COLLECTED", "value");
            rawResponse.payload().put("data", dataMap);
            rawResponses.add(rawResponse);

            //Mocks behaviour
            doReturn(Collections.singletonList(Mode.WEB)).when(controllerUtils).getModesList(any(),any());
            doReturn(Set.of(TestConstants.DEFAULT_INTERROGATION_ID))
                    .when(rawResponsePersistencePort).findUnprocessedInterrogationIdsByCollectionInstrumentId(any());
            doReturn(metadataModel).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());
            doReturn(rawResponses).when(rawResponsePersistencePort).findRawResponses(any(), any(), any());
        }

        //WHENS
        private List<SurveyUnitModel> whenProcessByCollectionInstrumentIdAndInterrogationIdList() throws GenesisException {
            rawResponseService.processRawResponses(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            verify(surveyUnitService).saveSurveyUnits(surveyUnitModelsCaptor.capture());
            return surveyUnitModelsCaptor.getValue();
        }
        private List<SurveyUnitModel> whenProcessRawResponsesCollectionInstrumentId() throws GenesisException {
            rawResponseService.processRawResponses(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Collections.singletonList(TestConstants.DEFAULT_INTERROGATION_ID),
                    new ArrayList<>()
            );
            verify(surveyUnitService).saveSurveyUnits(surveyUnitModelsCaptor.capture());
            return surveyUnitModelsCaptor.getValue();
        }

        //THENS
        private void processRawResponsesThen(RawResponseDto.QuestionnaireStateEnum questionnaireState,
                                             List<SurveyUnitModel> createdModels) {
            Assertions.assertThat(createdModels).hasSize(1);
            if(questionnaireState.equals(RawResponseDto.QuestionnaireStateEnum.FINISHED)){
                Assertions.assertThat(createdModels.getFirst().getValidationDate()).isEqualTo(
                        LocalDateTime.parse(TEST_VALIDATION_DATE, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                );
            }
            Assertions.assertThat(createdModels.getFirst().getQuestionnaireState()).isEqualTo(questionnaireState);
        }
        private void processRawResponsesThenValidationDateNull(
                List<SurveyUnitModel> createdModels
        ){
            Assertions.assertThat(createdModels).hasSize(1);
            Assertions.assertThat(createdModels.getFirst().getValidationDate()).isNull();
        }
        private void processRawResponsesThenQuestionnaireStateNull(List<SurveyUnitModel> createdModels){
            Assertions.assertThat(createdModels).hasSize(1);
            Assertions.assertThat(createdModels.getFirst().getQuestionnaireState()).isNull();
        }
    }

    @Test
    void countByCollectionInstrumentId_test(){
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        long exampleCount = 100;
        doReturn(exampleCount).when(rawResponsePersistencePort).countByCollectionInstrumentId(any());

        //WHEN + THEN
        Assertions.assertThat(rawResponseService.countByCollectionInstrumentId(collectionInstrumentId))
                .isEqualTo(exampleCount);
    }

    @Test
    void getDistinctCollectionInstrumentIds_test(){
        //GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        doReturn(Set.of(collectionInstrumentId)).when(rawResponsePersistencePort).findDistinctCollectionInstrumentIds();

        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getDistinctCollectionInstrumentIds()).containsExactly(collectionInstrumentId);
    }

    @Nested
    @DisplayName("convertRawResponse tests")
    class ConvertRawResponseTests {

        private VariablesMap variablesMap;

        @BeforeEach
        void setup() {
            variablesMap = new VariablesMap();
        }

        @Test
        @DisplayName("Simple COLLECTED raw response conversion")
        void convertRawResponse_shouldConvertCollectedVariable() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithVar("VAR1", "COLLECTED", "value1");
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            Assertions.assertThat(result).hasSize(1); // only COLLECTED state (EDITED has no data)
            Assertions.assertThat(result.getFirst().getCollectedVariables()).hasSize(1);
            Assertions.assertThat(result.getFirst().getCollectedVariables().getFirst().varId()).isEqualTo("VAR1");
            Assertions.assertThat(result.getFirst().getCollectedVariables().getFirst().value()).isEqualTo("value1");
        }

        @Test
        @DisplayName("Simple EDITED conversion")
        void convertRawResponse_shouldConvertEditedVariable() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithVar("VAR1", "EDITED", "editedValue");
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            // On attend 1 modèle EDITED avec des variables
            List<SurveyUnitModel> editedModels = result.stream()
                    .filter(m -> m.getState() == DataState.EDITED)
                    .toList();
            Assertions.assertThat(editedModels).hasSize(1);
            Assertions.assertThat(editedModels.getFirst().getCollectedVariables()).hasSize(1);
            Assertions.assertThat(editedModels.getFirst().getCollectedVariables().getFirst().value()).isEqualTo("editedValue");
        }

        @Test
        @DisplayName("Must convert external variables in COLLECTED")
        void convertRawResponse_shouldConvertExternalVariables() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithCollectedAndExternal("VAR1", "val1", "EXT1", "extVal");
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            List<SurveyUnitModel> collectedModels = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .toList();
            Assertions.assertThat(collectedModels).hasSize(1);
            Assertions.assertThat(collectedModels.getFirst().getExternalVariables()).hasSize(1);
            Assertions.assertThat(collectedModels.getFirst().getExternalVariables().getFirst().varId()).isEqualTo("EXT1");
        }

        @Test
        @DisplayName("Ignore empty models")
        void convertRawResponse_shouldIgnoreEmptyResponse() {
            // GIVEN
            RawResponseModel rawResponse = buildEmptyRawResponse();
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("COLLECTED list management")
        void convertRawResponse_shouldHandleListValues() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithListVar("VAR_LIST", "COLLECTED", List.of("v1", "v2", "v3"));
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            List<SurveyUnitModel> collectedModels = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .toList();
            Assertions.assertThat(collectedModels).hasSize(1);
            // 3 valeurs non nulles => 3 VariableModel avec iterations 1,2,3
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables()).hasSize(3);
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables().get(0).iteration()).isEqualTo(1);
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables().get(1).iteration()).isEqualTo(2);
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables().get(2).iteration()).isEqualTo(3);
        }

        @Test
        @DisplayName("Ignore null and empty values")
        void convertRawResponse_shouldSkipNullOrEmptyListValues() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithListVar("VAR_LIST", "COLLECTED", List.of("v1", "", "v3"));
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseService.convertRawResponse(rawResponses, variablesMap);

            // THEN
            List<SurveyUnitModel> collectedModels = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .toList();
            Assertions.assertThat(collectedModels).hasSize(1);
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables()).hasSize(2);
        }

        //UTILS

        private RawResponseModel buildRawResponseWithVar(String varName, String stateKey, String value) {
            RawResponseModel model = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> collectedMap = new HashMap<>();
            Map<String, Object> varStates = new HashMap<>();
            varStates.put(stateKey, value);
            collectedMap.put(varName, varStates);
            dataMap.put("COLLECTED", collectedMap);
            dataMap.put("EXTERNAL", new HashMap<>());
            model.payload().put("data", dataMap);
            return model;
        }

        private RawResponseModel buildRawResponseWithListVar(String varName, String stateKey, List<String> values) {
            RawResponseModel model = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            Map<String, Object> dataMap = new HashMap<>();
            Map<String, Object> collectedMap = new HashMap<>();
            Map<String, Object> varStates = new HashMap<>();
            varStates.put(stateKey, values);
            collectedMap.put(varName, varStates);
            dataMap.put("COLLECTED", collectedMap);
            dataMap.put("EXTERNAL", new HashMap<>());
            model.payload().put("data", dataMap);
            return model;
        }

        private RawResponseModel buildRawResponseWithCollectedAndExternal(
                String collectedVarName, String collectedValue,
                String externalVarName, String externalValue) {
            RawResponseModel model = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            Map<String, Object> dataMap = new HashMap<>();

            Map<String, Object> collectedMap = new HashMap<>();
            Map<String, Object> varStates = new HashMap<>();
            varStates.put("COLLECTED", collectedValue);
            collectedMap.put(collectedVarName, varStates);
            dataMap.put("COLLECTED", collectedMap);

            Map<String, Object> externalMap = new HashMap<>();
            externalMap.put(externalVarName, externalValue);
            dataMap.put("EXTERNAL", externalMap);

            model.payload().put("data", dataMap);
            return model;
        }

        private RawResponseModel buildEmptyRawResponse() {
            RawResponseModel model = new RawResponseModel(
                    null,
                    TestConstants.DEFAULT_INTERROGATION_ID,
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                    Mode.WEB,
                    new HashMap<>(),
                    LocalDateTime.now(),
                    null
            );
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("COLLECTED", new HashMap<>());
            dataMap.put("EXTERNAL", new HashMap<>());
            model.payload().put("data", dataMap);
            return model;
        }
    }

    @Test
    @DisplayName("getRawResponses must call persistence port")
    void getRawResponses_shouldDelegateToPersistencePort() {
        // GIVEN
        String collectionInstrumentId = TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
        Mode mode = Mode.WEB;
        List<String> interrogationIds = List.of(TestConstants.DEFAULT_INTERROGATION_ID);
        List<RawResponseModel> expected = List.of(mock(RawResponseModel.class));
        doReturn(expected).when(rawResponsePersistencePort).findRawResponses(collectionInstrumentId, mode, interrogationIds);

        // WHEN
        List<RawResponseModel> result = rawResponseService.getRawResponses(collectionInstrumentId, mode, interrogationIds);

        // THEN
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getRawResponsesByInterrogationID must call persistence port")
    void getRawResponsesByInterrogationID_shouldDelegateToPersistencePort() {
        // GIVEN
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        List<RawResponseModel> expected = List.of(mock(RawResponseModel.class));
        doReturn(expected).when(rawResponsePersistencePort).findRawResponsesByInterrogationID(interrogationId);

        // WHEN
        List<RawResponseModel> result = rawResponseService.getRawResponsesByInterrogationID(interrogationId);

        // THEN
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("updateProcessDates must call persistence port for each collectionInstrumentId")
    void updateProcessDates_shouldCallPersistencePortForEachCollectionInstrument() {
        // GIVEN
        SurveyUnitModel su1 = SurveyUnitModel.builder()
                .collectionInstrumentId("QUEST1")
                .interrogationId("INTERRO1")
                .build();
        SurveyUnitModel su2 = SurveyUnitModel.builder()
                .collectionInstrumentId("QUEST1")
                .interrogationId("INTERRO2")
                .build();
        SurveyUnitModel su3 = SurveyUnitModel.builder()
                .collectionInstrumentId("QUEST2")
                .interrogationId("INTERRO3")
                .build();

        // WHEN
        rawResponseService.updateProcessDates(List.of(su1, su2, su3));

        // THEN
        ArgumentCaptor<String> collectionIdCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Set> interrogationIdsCaptor = ArgumentCaptor.forClass(Set.class);
        verify(rawResponsePersistencePort, org.mockito.Mockito.times(2))
                .updateProcessDates(collectionIdCaptor.capture(), interrogationIdsCaptor.capture());

        Assertions.assertThat(collectionIdCaptor.getAllValues()).containsExactlyInAnyOrder("QUEST1", "QUEST2");
    }

    @Test
    @DisplayName("getUnprocessedCollectionInstrumentIds should exclude no mode raw datas")
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_shouldExclude_whenNoMode() {
        // GIVEN
        doReturn(List.of("QUEST_NO_MODE")).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(Collections.emptyList()).when(rawResponsePersistencePort).findModesByCollectionInstrument("QUEST_NO_MODE");

        // WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds()).isEmpty();
    }

    @Test
    @DisplayName("getUnprocessedCollectionInstrumentIds should exclude raw datas with only null")
    @SneakyThrows
    void getUnprocessedCollectionInstrumentIds_shouldExclude_whenOnlyNullMode() {
        // GIVEN
        doReturn(List.of("QUEST_NULL_MODE")).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        List<ModeDto> modesWithNull = new ArrayList<>();
        modesWithNull.add(null);
        doReturn(modesWithNull).when(rawResponsePersistencePort).findModesByCollectionInstrument("QUEST_NULL_MODE");

        // WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds()).isEmpty();
    }
}