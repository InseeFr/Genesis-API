package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
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
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.modelefiliere.ModeDto;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

import static fr.insee.genesis.TestConstants.DEFAULT_INTERROGATION_ID;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class RawResponseServiceUnitTest {

    static RawResponseService rawResponseService;
    static RawResponsePersistencePort rawResponsePersistencePort;
    static ControllerUtils controllerUtils;
    static QuestionnaireMetadataService metadataService;
    static SurveyUnitService surveyUnitService;
    static DataProcessingContextService dataProcessingContextService;

    private ArgumentCaptor<List<SurveyUnitModel>> surveyUnitModelsCaptor;

    private static final String TEST_VALIDATION_DATE = "2025-11-11T06:00:00Z";

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        rawResponsePersistencePort = mock(RawResponsePersistencePort.class);
        controllerUtils = mock(ControllerUtils.class);
        metadataService = mock(QuestionnaireMetadataService.class);
        surveyUnitService = mock(SurveyUnitService.class);
        dataProcessingContextService = mock(DataProcessingContextService.class);

        rawResponseService = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                dataProcessingContextService,
                new FileUtils(new ConfigStub()),
                new ConfigStub(),
                rawResponsePersistencePort
        );

        surveyUnitModelsCaptor = ArgumentCaptor.forClass(List.class);
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
                new ControllerUtils(new FileUtils(new ConfigStub())),
                metadataService,
                mock(SurveyUnitService.class),
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(new ConfigStub()),
                new ConfigStub(),
                rawResponsePersistencePort
        );


        //WHEN + THEN
        Assertions.assertThat(rawResponseService.getUnprocessedCollectionInstrumentIds())
                .containsExactly("TEST-TABLEAUX");
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
            rawResponseService.processRawResponsesByInterrogationIds(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
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

        List<RawResponseModel> mockedRawResponses = new ArrayList<>(List.of(
                new RawResponseModel(new ObjectId(), "interrogation-id-1", fooCollectionInstrumentId, fooMode, fooPayload, recordDate1, processDate),
                new RawResponseModel(new ObjectId(), "interrogation-id-1", fooCollectionInstrumentId, fooMode, fooPayload, recordDate2, null),
                new RawResponseModel(new ObjectId(), "interrogation-id-2", fooCollectionInstrumentId, fooMode, fooPayload, recordDate2, null)
        ));

        Mockito.when(rawResponsePersistencePort.findUnprocessedInterrogationIdsByCollectionInstrumentId(fooCollectionInstrumentId)).thenReturn(interrogationIds);
        Mockito.when(rawResponsePersistencePort.findRawResponses(fooCollectionInstrumentId, fooMode, interrogationIdList)).thenReturn(mockedRawResponses);
        Mockito.when(controllerUtils.getModesList(fooCollectionInstrumentId)).thenReturn(List.of(fooMode));
        Mockito.when(dataProcessingContextService.getContextByCollectionInstrumentId(fooCollectionInstrumentId)).thenReturn(fooProcessingContext);
        Mockito.when(metadataService.loadAndSaveIfNotExists(eq(fooCollectionInstrumentId), eq(fooCollectionInstrumentId), eq(fooMode), any(), any())).thenReturn(new MetadataModel());

        // When
        DataProcessResult dataProcessResult = rawResponseService.processRawResponses(fooCollectionInstrumentId);

        // Then
        assertEquals(2, dataProcessResult.dataCount());
    }

}
