package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

public class ResponseControllerUnitTest {
    static ResponseController responseController;

    static SurveyUnitApiPort surveyUnitApiPort;
    static SurveyUnitQualityService surveyUnitQualityService;
    static QuestionnaireMetadataService questionnaireMetadataService;
    static DataProcessingContextApiPort dataProcessingContextApiPort;

    static FileUtils fileUtils = new FileUtils(new ConfigStub());

    @BeforeEach
    void init(){
        //Mocks
        surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        surveyUnitQualityService = mock(SurveyUnitQualityService.class);
        questionnaireMetadataService = mock(QuestionnaireMetadataService.class);
        dataProcessingContextApiPort = mock(DataProcessingContextApiPort.class);

        responseController = new ResponseController(
                surveyUnitApiPort,
                surveyUnitQualityService,
                new FileUtils(new ConfigStub()),
                new ControllerUtils(fileUtils),
                new AuthUtils(new ConfigStub()),
                questionnaireMetadataService,
                dataProcessingContextApiPort
        );
    }

    //Non regression test of #22876
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnUsualSurveyUnitIdInSimplifiedResponsesWhereInAnyOrder(boolean isReversed){
        //GIVEN
        String usId = "IDUE";
        String varId = "varid";
        //Mock behaviour
        doReturn(Collections.singletonList(Mode.WEB)).when(surveyUnitApiPort).findModesByCollectionInstrumentId(any());
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        //Collected
        List<VariableModel> collectedVariables = new ArrayList<>();
        collectedVariables.add(VariableModel.builder()
                        .varId(varId)
                        .value("value")
                        .iteration(1)
                        .scope(Constants.ROOT_GROUP_NAME)
                .build()
        );
        surveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .mode(Mode.WEB)
                        .state(DataState.COLLECTED)
                        .usualSurveyUnitId(usId)
                        .collectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                        .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                        .collectedVariables(collectedVariables)
                        .externalVariables(new ArrayList<>())
                        .recordDate(LocalDateTime.now().minusMinutes(5))
                        .build()
        );

        //Edited model
        collectedVariables = new ArrayList<>();
        collectedVariables.add(VariableModel.builder()
                .varId(varId)
                .value("valueedited")
                .iteration(1)
                .scope(Constants.ROOT_GROUP_NAME)
                .build()
        );
        surveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .mode(Mode.WEB)
                        .state(DataState.EDITED)
                        .collectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                        .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                        .collectedVariables(collectedVariables)
                        .externalVariables(new ArrayList<>())
                        .recordDate(LocalDateTime.now().minusMinutes(1))
                        .build()
        );

        doReturn(isReversed ? surveyUnitModelList.reversed() : surveyUnitModelList).when(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<List<SurveyUnitSimplified>> returnedResponse = responseController.getLatestForInterrogationListAndCollectionInstrument(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Collections.singletonList(new InterrogationId(TestConstants.DEFAULT_INTERROGATION_ID))
        );

        //THEN
        Assertions.assertThat(returnedResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(returnedResponse.getBody()).hasSize(1);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getUsualSurveyUnitId()).isEqualTo(usId);
    }
}
