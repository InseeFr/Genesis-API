package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.mockito.ArgumentCaptor;

import java.time.LocalDateTime;
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

class RawResponseServiceUnitTest {

    static RawResponseService rawResponseService;
    static RawResponsePersistencePort rawResponsePersistencePort;
    static ControllerUtils controllerUtils;
    static QuestionnaireMetadataService metadataService;
    static SurveyUnitService surveyUnitService;

    private ArgumentCaptor<List<SurveyUnitModel>> surveyUnitModelsCaptor;

    private LocalDateTime validationDate;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void init() {
        rawResponsePersistencePort = mock(RawResponsePersistencePort.class);
        controllerUtils = mock(ControllerUtils.class);
        metadataService = mock(QuestionnaireMetadataService.class);
        surveyUnitService = mock(SurveyUnitService.class);

        rawResponseService = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                mock(SurveyUnitQualityService.class),
                mock(SurveyUnitQualityToolPort.class),
                mock(DataProcessingContextService.class),
                new FileUtils(new ConfigStub()),
                new ConfigStub(),
                rawResponsePersistencePort
        );

        surveyUnitModelsCaptor = ArgumentCaptor.forClass(List.class);
    }

    //Non regression tests of #22875
    @ParameterizedTest
    @EnumSource(RawResponseDto.QuestionnaireStateEnum.class)
    @SneakyThrows
    void processRawResponses_byCollectionInstrumentId_validation_date_questionnaire_state_test(
            RawResponseDto.QuestionnaireStateEnum questionnaireState
    ) {
        //GIVEN
        processRawResponses_given(questionnaireState);

        //WHEN
        rawResponseService.processRawResponses(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        verify(surveyUnitService).saveSurveyUnits(surveyUnitModelsCaptor.capture());
        List<SurveyUnitModel> createdModels = surveyUnitModelsCaptor.getValue();

        //THEN
        processRawResponses_then(questionnaireState, createdModels);
    }

    @ParameterizedTest
    @EnumSource(RawResponseDto.QuestionnaireStateEnum.class)
    @SneakyThrows
    void processRawResponses_byCollectionInstrumentIdAndInterrogationList_validation_date_questionnaire_state_test(
            RawResponseDto.QuestionnaireStateEnum questionnaireState
    ) {
        //GIVEN
        processRawResponses_given(questionnaireState);

        //WHEN
        rawResponseService.processRawResponses(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Collections.singletonList(TestConstants.DEFAULT_INTERROGATION_ID),
                new ArrayList<>()
        );
        verify(surveyUnitService).saveSurveyUnits(surveyUnitModelsCaptor.capture());
        List<SurveyUnitModel> createdModels = surveyUnitModelsCaptor.getValue();

        //THEN
        processRawResponses_then(questionnaireState, createdModels);
    }
    @SneakyThrows
    private void processRawResponses_given(RawResponseDto.QuestionnaireStateEnum questionnaireState){
        VariablesMap variablesMap = new VariablesMap();
        //TODO if the bug is caused by BPM, put variables into it to emulate fix
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.setVariables(variablesMap);
        validationDate = questionnaireState.equals(RawResponseDto.QuestionnaireStateEnum.FINISHED) ?
                LocalDateTime.now() : null;


        List<RawResponse> rawResponses = new ArrayList<>();
        RawResponse rawResponse = new RawResponse(
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
        Map<String, Map<String, Map<String, String>>> dataMap = new HashMap<>();
        dataMap.put("COLLECTED", new HashMap<>());
        dataMap.get("COLLECTED").put("VAR1", new HashMap<>());
        dataMap.get("COLLECTED").get("VAR1").put("COLLECTED", "value");
        rawResponse.payload().put("data", dataMap);
        rawResponses.add(rawResponse);

        //Mocks behaviour
        doReturn(Collections.singletonList(Mode.WEB)).when(controllerUtils).getModesList(any(),any());
        doReturn(Set.of(TestConstants.DEFAULT_INTERROGATION_ID)).when(rawResponsePersistencePort).findUnprocessedInterrogationIdsByCollectionInstrumentId(any());
        doReturn(metadataModel).when(metadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), any());
        doReturn(rawResponses).when(rawResponsePersistencePort).findRawResponses(any(), any(), any());
    }
    private void processRawResponses_then(RawResponseDto.QuestionnaireStateEnum questionnaireState,
                                          List<SurveyUnitModel> createdModels) {
        Assertions.assertThat(createdModels).hasSize(1);
        Assertions.assertThat(createdModels.getFirst().getValidationDate()).isEqualTo(validationDate);
        Assertions.assertThat(createdModels.getFirst().getQuestionnaireState()).isEqualTo(questionnaireState);
    }
}