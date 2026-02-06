package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ResponseControllerUnitTest {
    private static ResponseController responseController;

    private static SurveyUnitApiPort surveyUnitApiPort;
    private static SurveyUnitQualityService surveyUnitQualityService;
    private static QuestionnaireMetadataService questionnaireMetadataService;
    private static DataProcessingContextApiPort dataProcessingContextApiPort;
    private static AuthUtils authUtils;

    static FileUtils fileUtils = new FileUtils(TestConstants.getConfigStub());

    @BeforeEach
    void init(){
        //Mocks
        surveyUnitApiPort = mock(SurveyUnitApiPort.class);
        surveyUnitQualityService = mock(SurveyUnitQualityService.class);
        questionnaireMetadataService = mock(QuestionnaireMetadataService.class);
        dataProcessingContextApiPort = mock(DataProcessingContextApiPort.class);
        authUtils = mock(AuthUtils.class);

        responseController = new ResponseController(
                surveyUnitApiPort,
                surveyUnitQualityService,
                new FileUtils(TestConstants.getConfigStub()),
                new ControllerUtils(fileUtils),
                authUtils,
                questionnaireMetadataService,
                dataProcessingContextApiPort
        );
    }

    @Test
    void deleteAllResponsesByCollectionInstrument_test() {
        //GIVEN
        String collectionInstrumentId = "test";

        //WHEN
        responseController.deleteAllResponsesByCollectionInstrument(collectionInstrumentId);

        //THEN
        verify(surveyUnitApiPort, times(1)).deleteByCollectionInstrumentId(collectionInstrumentId);
    }

    @Test
    void findResponsesByInterrogationAndCollectionInstrument_test() {
        //GIVEN
        String interrogationId = "testInterrogation";
        String collectionInstrumentId = "testColIns";
        List<SurveyUnitModel> surveyUnitModelList = List.of(
                new SurveyUnitModel()
        );
        doReturn(surveyUnitModelList).when(surveyUnitApiPort)
                .findByIdsInterrogationAndCollectionInstrument(any(), any());

        //WHEN
        ResponseEntity<List<SurveyUnitModel>> response = responseController.findResponsesByInterrogationAndCollectionInstrument(
                interrogationId, collectionInstrumentId
        );

        //THEN
        verify(surveyUnitApiPort).findByIdsInterrogationAndCollectionInstrument(
                interrogationId,
                collectionInstrumentId
        );
        Assertions.assertThat(response.getBody()).isEqualTo(surveyUnitModelList);
    }

    @Test
    @SneakyThrows
    void findResponsesByInterrogationAndCollectionInstrumentLatestStates_test() {
        //GIVEN
        String interrogationId = "testInterrogation";
        String collectionInstrumentId = "testColIns";

        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .withReview(true)
                .build();
        doReturn(dataProcessingContextModel).when(dataProcessingContextApiPort).getContext(
                any()
        );

        SurveyUnitDto surveyUnitDto = SurveyUnitDto.builder()
                .interrogationId(interrogationId)
                .build();
        doReturn(surveyUnitDto).when(surveyUnitApiPort)
                .findLatestValuesByStateByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<Object> response = responseController
                .findResponsesByInterrogationAndCollectionInstrumentLatestStates(
                        interrogationId,
                        collectionInstrumentId
                );

        //THEN
        verify(dataProcessingContextApiPort, times(1)).getContext(interrogationId);
        verify(surveyUnitApiPort, times(1)).findLatestValuesByStateByIdAndByCollectionInstrumentId(
                interrogationId,
                collectionInstrumentId
        );
        Assertions.assertThat(response.getBody()).isNotNull().isInstanceOf(SurveyUnitQualityToolDto.class);
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) response.getBody();
        Assertions.assertThat(surveyUnitQualityToolDto.getInterrogationId()).isEqualTo(interrogationId);
    }

    @Test
    void getLatestByInterrogationAndCollectionInstrument_test() {
        //GIVEN
        String interrogationId = "testInterrogation";
        String collectionInstrumentId = "testColIns";
        List<SurveyUnitModel> surveyUnitModelList = List.of(
                SurveyUnitModel.builder().interrogationId(interrogationId).build()
        );
        doReturn(surveyUnitModelList).when(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<List<SurveyUnitModel>> response = responseController.getLatestByInterrogationAndCollectionInstrument(
                interrogationId,
                collectionInstrumentId
        );

        //THEN
        verify(surveyUnitApiPort, times(1))
                .findLatestByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);
        Assertions.assertThat(response.getBody()).isEqualTo(surveyUnitModelList);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getLatestByInterrogationOneObject_test(boolean isReversed) {
        //GIVEN
        String interrogationId = "testInterrogation";
        String collectionInstrumentId = "testColIns";
        LocalDateTime validationDate = LocalDateTime.now();
        RawResponseDto.QuestionnaireStateEnum questionnaireState = RawResponseDto.QuestionnaireStateEnum.FINISHED;
        Mode mode = Mode.WEB;

        List<SurveyUnitModel> surveyUnitModelList = List.of(
                SurveyUnitModel.builder()
                        .collectionInstrumentId(collectionInstrumentId)
                        .interrogationId(interrogationId)
                        .validationDate(validationDate)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build(),
                SurveyUnitModel.builder()
                        .collectionInstrumentId(collectionInstrumentId)
                        .interrogationId(interrogationId)
                        .questionnaireState(questionnaireState)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build()
        );
        doReturn(isReversed ? surveyUnitModelList.reversed() : surveyUnitModelList)
                .when(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<SurveyUnitSimplified> response = responseController.getLatestByInterrogationOneObject(
                interrogationId,
                collectionInstrumentId,
                mode
        );

        //THEN
        verify(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(response.getBody().getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(response.getBody().getMode()).isEqualTo(mode);
        Assertions.assertThat(response.getBody().getQuestionnaireState()).isEqualTo(questionnaireState);
        Assertions.assertThat(response.getBody().getValidationDate()).isEqualTo(validationDate);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void getLatestForInterrogationListAndCollectionInstrument_test(boolean isReversed){
        //GIVEN
        String interrogationId = "testInterrogation";
        String collectionInstrumentId = "testColIns";
        String usId = "IDUE";
        String varId = "varid";
        Mode mode = Mode.WEB;
        LocalDateTime validationDate = LocalDateTime.now();
        RawResponseDto.QuestionnaireStateEnum questionnaireState = RawResponseDto.QuestionnaireStateEnum.FINISHED;

        //Mock behaviour
        doReturn(Collections.singletonList(mode)).when(surveyUnitApiPort).findModesByCollectionInstrumentId(any());
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
                        .mode(mode)
                        .questionnaireState(questionnaireState)
                        .validationDate(validationDate)
                        .state(DataState.COLLECTED)
                        .usualSurveyUnitId(usId)
                        .collectionInstrumentId(collectionInstrumentId)
                        .interrogationId(interrogationId)
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
                        .mode(mode)
                        .state(DataState.EDITED)
                        .collectionInstrumentId(collectionInstrumentId)
                        .interrogationId(interrogationId)
                        .collectedVariables(collectedVariables)
                        .externalVariables(new ArrayList<>())
                        .recordDate(LocalDateTime.now().minusMinutes(1))
                        .build()
        );

        doReturn(isReversed ? surveyUnitModelList.reversed() : surveyUnitModelList).when(surveyUnitApiPort)
                .findLatestByIdAndByCollectionInstrumentId(any(), any());

        //WHEN
        ResponseEntity<List<SurveyUnitSimplified>> returnedResponse = responseController
                .getLatestForInterrogationListAndCollectionInstrument(
                    collectionInstrumentId,
                    Collections.singletonList(new InterrogationId(interrogationId))
        );

        //THEN
        verify(surveyUnitApiPort).findLatestByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);

        Assertions.assertThat(returnedResponse.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(returnedResponse.getBody()).isNotNull();
        Assertions.assertThat(returnedResponse.getBody()).hasSize(1);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getCollectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getMode()).isEqualTo(mode);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getUsualSurveyUnitId()).isEqualTo(usId);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getValidationDate()).isEqualTo(validationDate);
        Assertions.assertThat(returnedResponse.getBody().getFirst().getQuestionnaireState()).isEqualTo(questionnaireState);
    }

    @Test
    @SneakyThrows
    void saveEditedVariables_test() {
        //GIVEN
        String questionnaireId = "testQuest";
        String interrogationId = "testInterrogation";
        Mode mode = Mode.WEB;
        String idep = "AAAAA";

        doReturn(Set.of("test")).when(surveyUnitApiPort).findCampaignIdsFrom(any());
        MetadataModel metadataModel = new MetadataModel();
        doReturn(metadataModel).when(questionnaireMetadataService).loadAndSaveIfNotExists(
                any(),any(),any(),any(), anyList());
        doReturn(new ArrayList<>()).when(surveyUnitQualityService).checkVariablesPresentInMetadata(anyList(), any());
        doReturn(idep).when(authUtils).getIDEP();
        List<SurveyUnitModel> surveyUnitModelList = List.of(
                SurveyUnitModel.builder()
                        .collectionInstrumentId(questionnaireId)
                        .interrogationId(interrogationId)
                        .state(DataState.COLLECTED)
                        .questionnaireState(RawResponseDto.QuestionnaireStateEnum.STARTED)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build(),
                SurveyUnitModel.builder()
                        .collectionInstrumentId(questionnaireId)
                        .interrogationId(interrogationId)
                        .state(DataState.EDITED)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .modifiedBy(idep)
                        .build()
        );
        doReturn(surveyUnitModelList).when(surveyUnitApiPort).parseEditedVariables(any(),any(),any());

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .mode(mode)
                .collectedVariables(new ArrayList<>())
                .build();

        //WHEN
        responseController.saveEditedVariables(surveyUnitInputDto);

        //THEN
        verify(questionnaireMetadataService, times(1)).loadAndSaveIfNotExists(any(), eq(questionnaireId), eq(mode),
                any(),
                any());
        verify(surveyUnitQualityService, times(1)).verifySurveyUnits(
                any(), eq(metadataModel.getVariables())
        );
        verify(surveyUnitApiPort, times(1)).parseEditedVariables(
                surveyUnitInputDto,
                idep,
                metadataModel.getVariables()
        );
        verify(surveyUnitApiPort, times(1)).saveSurveyUnits(anyList());
    }

    @Test
    @SneakyThrows
    void saveEditedVariables_null_metadata_test() {
        //GIVEN
        doReturn(Set.of("test")).when(surveyUnitApiPort).findCampaignIdsFrom(any());
        doAnswer(invocation -> {
                    List<GenesisError> list = invocation.getArgument(4);
                    list.add(new GenesisError(""));
                    return null;
                }
        ).when(questionnaireMetadataService).loadAndSaveIfNotExists(any(), any(), any(), any(), anyList());

        //No metadataModel
        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .questionnaireId("testQuest")
                .interrogationId("testInterro")
                .mode(Mode.WEB)
                .collectedVariables(new ArrayList<>())
                .build();

        //WHEN
        ResponseEntity<Object> response = responseController.saveEditedVariables(surveyUnitInputDto);

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    @SneakyThrows
    void saveEditedVariables_absent_variables_test() {
        //GIVEN
        String questionnaireId = "testQuest";
        String interrogationId = "testInterrogation";
        Mode mode = Mode.WEB;
        String idep = "AAAAA";

        doReturn(Set.of("test")).when(surveyUnitApiPort).findCampaignIdsFrom(any());
        MetadataModel metadataModel = new MetadataModel();
        doReturn(metadataModel).when(questionnaireMetadataService).loadAndSaveIfNotExists(
                any(),any(),any(),any(), anyList());
        //1 absent variable
        doReturn(List.of("absentVariable")).when(surveyUnitQualityService)
                .checkVariablesPresentInMetadata(anyList(), any());
        doReturn(idep).when(authUtils).getIDEP();
        List<SurveyUnitModel> surveyUnitModelList = List.of(
                SurveyUnitModel.builder()
                        .collectionInstrumentId(questionnaireId)
                        .interrogationId(interrogationId)
                        .state(DataState.COLLECTED)
                        .questionnaireState(RawResponseDto.QuestionnaireStateEnum.STARTED)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build(),
                SurveyUnitModel.builder()
                        .collectionInstrumentId(questionnaireId)
                        .interrogationId(interrogationId)
                        .state(DataState.EDITED)
                        .mode(mode)
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .modifiedBy(idep)
                        .build()
        );
        doReturn(surveyUnitModelList).when(surveyUnitApiPort).parseEditedVariables(any(),any(),any());

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .mode(mode)
                .collectedVariables(new ArrayList<>())
                .build();

        //WHEN
        ResponseEntity<Object> response = responseController.saveEditedVariables(surveyUnitInputDto);

        //THEN
        Assertions.assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}
