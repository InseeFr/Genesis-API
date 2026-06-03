package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.converter.rawdata.RawResponseRawDataConverter;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.parser.rawdata.RawResponsePayloadParser;
import fr.insee.genesis.domain.ports.spi.QuestionnaireMetadataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityToolService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.modelefiliere.ModeDto;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static fr.insee.genesis.TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;
import static fr.insee.genesis.TestConstants.DEFAULT_INTERROGATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RawResponseServiceUnitTest {

    private RawResponseService rawResponseService;

    @Mock
    private RawResponsePersistencePort rawResponsePersistencePort;
    @Mock
    private ControllerUtils controllerUtils;
    @Mock
    private QuestionnaireMetadataService metadataService;
    @Mock
    private SurveyUnitService surveyUnitService;
    @Mock
    private SurveyUnitQualityService surveyUnitQualityService;
    @Mock
    private SurveyUnitQualityToolService surveyUnitQualityToolService;

    private RawResponseRawDataConverter rawResponseRawDataConverter;
    @Mock
    static DataProcessingContextService dataProcessingContextService;

    @Captor
    private ArgumentCaptor<List<SurveyUnitModel>> surveyUnitModelsCaptor;

    private static final String TEST_VALIDATION_DATE = "2025-11-11T06:00:00Z";

    @BeforeEach
    void init() {
        rawResponseRawDataConverter = new RawResponseRawDataConverter(surveyUnitService, new RawResponsePayloadParser());

        rawResponseService = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                surveyUnitQualityService,
                surveyUnitQualityToolService,
                new FileUtils(TestConstants.getConfigStub()),
                TestConstants.getConfigStub(),
                rawResponseRawDataConverter,
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
        questionnaireIds.add(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        doReturn(questionnaireIds).when(rawResponsePersistencePort).getUnprocessedCollectionIds();
        doReturn(List.of(ModeDto.CAWI)).when(rawResponsePersistencePort).findModesByCollectionInstrument(any());
        //No mock for metadataservice this time
        metadataService = new QuestionnaireMetadataService(
                mock(QuestionnaireMetadataPersistencePort.class)
        );
        rawResponseService = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                surveyUnitQualityService,
                surveyUnitQualityToolService,
                new FileUtils(TestConstants.getConfigStub()),
                TestConstants.getConfigStub(),
                rawResponseRawDataConverter,
                rawResponsePersistencePort
        );


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactly(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
    }

    @Test
    void existsByInterrogationId_shouldReturnTrue_whenExists() {
        // When
        Mockito.when(rawResponsePersistencePort.existsByInterrogationId(DEFAULT_INTERROGATION_ID))
                .thenReturn(true);
        boolean exists = rawResponseService.existsByInterrogationId(DEFAULT_INTERROGATION_ID);

        // Then
        Assertions.assertThat(exists).isTrue();
    }

    @Test
    void existsByInterrogationId_shouldReturnFalse_whenNotExists() {
        // Given
        String unknownId = "UNKNOWN_INTERROGATION_ID";

        // When
        boolean exists = rawResponseService.existsByInterrogationId(unknownId);

        // Then
        Assertions.assertThat(exists).isFalse();
    }

    @Nested
    @DisplayName("Non regression tests of InseeFr/Genesis-API#365: validation date and questionnaire state in processed responses")
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
            rawResponseService.processRawResponsesByCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            verify(surveyUnitService).saveSurveyUnits(surveyUnitModelsCaptor.capture());
            return surveyUnitModelsCaptor.getValue();
        }
        private List<SurveyUnitModel> whenProcessRawResponsesCollectionInstrumentId() throws GenesisException {
            rawResponseService.processRawResponsesByInterrogationIds(
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

    @Test
    void processWithDuplicateInterrogationId() throws GenesisException {
        // Given
        String fooCollectionInstrumentId = "FOOX00";
        Mode fooMode = Mode.WEB;

        DataProcessingContextModel fooProcessingContext = DataProcessingContextModel.builder()
                .collectionInstrumentId(fooCollectionInstrumentId).withReview(false)
                .build();

        Set<String> interrogationIds = Set.of("interrogation-id-1", "interrogation-id-2");
        List<String> interrogationIdList = interrogationIds.stream().toList();
        Map<String, Object> fooVariable = Map.of("COLLECTED", "some value");
        Map<String, Object> fooCollectedContent = Map.of("SOME_VARIABLE", fooVariable);
        Map<String, Object> fooData = Map.of("COLLECTED", fooCollectedContent);
        Map<String, Object> fooPayload = Map.of(
                "questionnaireState", "FOO_QUESTIONNAIRE_STATE",
                "data", fooData);

        LocalDateTime recordDate1 = LocalDateTime.of(2026, 1, 1, 8, 0);
        LocalDateTime processDate = LocalDateTime.of(2026, 1, 1, 9, 0);
        LocalDateTime recordDate2 = LocalDateTime.of(2026, 1, 1, 10, 0);

        //2 responses for interrogation-id-1
        List<RawResponseModel> mockedRawResponses = new ArrayList<>(List.of(
                new RawResponseModel(new ObjectId(), "interrogation-id-1", fooCollectionInstrumentId, fooMode, fooPayload, recordDate1, processDate),
                new RawResponseModel(new ObjectId(), "interrogation-id-1", fooCollectionInstrumentId, fooMode, fooPayload, recordDate2, null),
                new RawResponseModel(new ObjectId(), "interrogation-id-2", fooCollectionInstrumentId, fooMode, fooPayload, recordDate2, null)
        ));

        Mockito.when(rawResponsePersistencePort.findUnprocessedInterrogationIdsByCollectionInstrumentId(fooCollectionInstrumentId)).thenReturn(interrogationIds);
        Mockito.when(rawResponsePersistencePort.findRawResponses(fooCollectionInstrumentId, fooMode, interrogationIdList)).thenReturn(mockedRawResponses);
        Mockito.when(controllerUtils.getModesList(eq(fooCollectionInstrumentId), any())).thenReturn(List.of(fooMode));
        Mockito.when(dataProcessingContextService.getContextByCollectionInstrumentId(fooCollectionInstrumentId)).thenReturn(fooProcessingContext);
        Mockito.when(metadataService.loadAndSaveIfNotExists(eq(fooCollectionInstrumentId), eq(fooCollectionInstrumentId), eq(fooMode), any(), any())).thenReturn(new MetadataModel());

        // When
        DataProcessResult dataProcessResult = rawResponseService.processRawResponsesByCollectionInstrumentId(
                fooCollectionInstrumentId
        );

        // Then
        assertEquals(2, dataProcessResult.dataCount());
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
            RawResponseModel rawResponse = buildRawResponseWithVar("COLLECTED", "value1");
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,rawResponses, variablesMap
            );

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
            RawResponseModel rawResponse = buildRawResponseWithVar("EDITED", "editedValue");
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponses, variablesMap
            );

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
            RawResponseModel rawResponse = buildRawResponseWithCollectedAndExternal();
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponses, variablesMap
            );

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
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponses, variablesMap);

            // THEN
            Assertions.assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("COLLECTED list management")
        void convertRawResponse_shouldHandleListValues() {
            // GIVEN
            RawResponseModel rawResponse = buildRawResponseWithListVar(List.of("v1", "v2", "v3"));
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponses, variablesMap
            );

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
            RawResponseModel rawResponse = buildRawResponseWithListVar(List.of("v1", "", "v3"));
            List<RawResponseModel> rawResponses = List.of(rawResponse);

            // WHEN
            List<SurveyUnitModel> result = rawResponseRawDataConverter.convertRawResponse(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, rawResponses, variablesMap
            );

            // THEN
            List<SurveyUnitModel> collectedModels = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .toList();
            Assertions.assertThat(collectedModels).hasSize(1);
            Assertions.assertThat(collectedModels.getFirst().getCollectedVariables()).hasSize(2);
        }

        //UTILS

        private RawResponseModel buildRawResponseWithVar(String stateKey, String value) {
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
            collectedMap.put("VAR1", varStates);
            dataMap.put("COLLECTED", collectedMap);
            dataMap.put("EXTERNAL", new HashMap<>());
            model.payload().put("data", dataMap);
            return model;
        }

        private RawResponseModel buildRawResponseWithListVar(List<String> values) {
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
            varStates.put("COLLECTED", values);
            collectedMap.put("VAR_LIST", varStates);
            dataMap.put("COLLECTED", collectedMap);
            dataMap.put("EXTERNAL", new HashMap<>());
            model.payload().put("data", dataMap);
            return model;
        }

        private RawResponseModel buildRawResponseWithCollectedAndExternal() {
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
            varStates.put("COLLECTED", "val1");
            collectedMap.put("VAR1", varStates);
            dataMap.put("COLLECTED", collectedMap);

            Map<String, Object> externalMap = new HashMap<>();
            externalMap.put("EXT1", "extVal");
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
    @DisplayName("getRawResponseByCollectionInstrumentIdAndInterrogationId must call persistence port")
    void getRawResponseByCollectionInstrumentIdAndInterrogationId_shouldDelegateToPersistencePort() throws NoDataException {
        // GIVEN
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        String collectionInstrumentId = DEFAULT_COLLECTION_INSTRUMENT_ID;
        List<RawResponseModel> expected = List.of(mock(RawResponseModel.class));
        doReturn(expected).when(rawResponsePersistencePort).findRawResponseByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);

        // WHEN
        List<RawResponseModel> result = rawResponseService.getRawResponseByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);

        // THEN
        Assertions.assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("getRawResponseByCollectionInstrumentIdAndInterrogationId must throw NoDataException when no raw response found")
    void getRawResponseByCollectionInstrumentIdAndInterrogationId_noData_shouldThrowNoDataException() {
        // GIVEN
        String interrogationId = TestConstants.DEFAULT_INTERROGATION_ID;
        String collectionInstrumentId = DEFAULT_COLLECTION_INSTRUMENT_ID;

        doReturn(List.of())
                .when(rawResponsePersistencePort)
                .findRawResponseByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);

        // WHEN + THEN
        assertThatThrownBy(() ->
                rawResponseService.getRawResponseByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                )
        ).isInstanceOf(NoDataException.class);
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
