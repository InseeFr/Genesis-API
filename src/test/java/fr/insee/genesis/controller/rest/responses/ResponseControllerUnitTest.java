package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
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
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ResponseControllerUnitTest {
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

    @Test
    @SneakyThrows
    void getResponseByCollectionInstrumentAndInterrogation_test(){
        //GIVEN
        String collectionInstrumentId = "collectionInstrumentId";
        String interrogationId = "interrogationTest";
        Mode mode = Mode.WEB;
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto = SurveyUnitSimplifiedDto.builder().build();
        doReturn(surveyUnitSimplifiedDto).when(surveyUnitApiPort).findSimplified(
                collectionInstrumentId,
                interrogationId,
                mode,
                null
        );

        //WHEN
        ResponseEntity<SurveyUnitSimplifiedDto> response = responseController.getResponseByCollectionInstrumentAndInterrogation(
                collectionInstrumentId,
                interrogationId,
                mode
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(surveyUnitApiPort, times(1)).findSimplified(
                collectionInstrumentId,
                interrogationId,
                mode,
                null
        );
        Assertions.assertThat(response.getBody()).isEqualTo(surveyUnitSimplifiedDto);
    }

    @Test
    @SneakyThrows
    void getResponseByCollectionInstrumentAndInterrogationList_test(){
        //GIVEN
        String collectionInstrumentId = "collectionInstrumentId";
        List<InterrogationId> interrogationIds = List.of(
                new InterrogationId("interrogationTest1")
                , new InterrogationId("interrogationTest2")
        );
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto1 = SurveyUnitSimplifiedDto.builder()
                .interrogationId(interrogationIds.getFirst().getInterrogationId())
                .build();
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto2 = SurveyUnitSimplifiedDto.builder()
                .interrogationId(interrogationIds.getLast().getInterrogationId())
                .build();
        List<SurveyUnitSimplifiedDto> surveyUnitSimplifiedDtos = List.of(
                surveyUnitSimplifiedDto1,
                surveyUnitSimplifiedDto2
        );
        doReturn(surveyUnitSimplifiedDtos).when(surveyUnitApiPort).findSimplifiedList(
                collectionInstrumentId,
                interrogationIds,
                null
        );

        //WHEN
        ResponseEntity<List<SurveyUnitSimplifiedDto>> response = responseController.searchResponses(
                collectionInstrumentId,
                null,
                interrogationIds
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        verify(surveyUnitApiPort, times(1)).findSimplifiedList(
                collectionInstrumentId,
                interrogationIds,
                null
        );
        Assertions.assertThat(response.getBody()).hasSize(2);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(surveyUnitSimplifiedDto1);
        Assertions.assertThat(response.getBody().getLast()).isEqualTo(surveyUnitSimplifiedDto2);
    }

    //Non regression test of #22876 and #22875
    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void shouldReturnNewVariablesInSimplifiedResponsesWhereInAnyOrder(boolean isReversed){
        //GIVEN
        String usId = "IDUE";
        String varId = "varid";
        LocalDateTime validationDate = LocalDateTime.now();
        RawResponseDto.QuestionnaireStateEnum questionnaireState = RawResponseDto.QuestionnaireStateEnum.FINISHED;

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
                        .questionnaireState(questionnaireState)
                        .validationDate(validationDate)
                        .state(DataState.COLLECTED)
                        .usualSurveyUnitId(usId)
                        .collectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                        .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                        .collectedVariables(collectedVariables)
                        .externalVariables(new ArrayList<>())
                        .recordDate(LocalDateTime.now().minusMinutes(5).toInstant(ZoneOffset.UTC))
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
                        .recordDate(LocalDateTime.now().minusMinutes(1).toInstant(ZoneOffset.UTC))
                        .build()
        );

        doReturn(isReversed ? surveyUnitModelList.reversed() : surveyUnitModelList).when(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<List<SurveyUnitSimplifiedDto>> returnedResponse = responseController.getLatestForInterrogationListAndCollectionInstrument(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Collections.singletonList(new InterrogationId(TestConstants.DEFAULT_INTERROGATION_ID))
        );

        //THEN
        Assertions.assertThat(returnedResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(returnedResponse.getBody()).hasSize(1);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getUsualSurveyUnitId()).isEqualTo(usId);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getValidationDate()).isEqualTo(validationDate);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getQuestionnaireState()).isEqualTo(questionnaireState);
    }
}
