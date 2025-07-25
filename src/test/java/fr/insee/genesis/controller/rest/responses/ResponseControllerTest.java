package fr.insee.genesis.controller.rest.responses;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateInputDto;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

class ResponseControllerTest {
    //Given
    static ResponseController responseControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;

    static List<InterrogationId> interrogationIdList;
    //Constants
    static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";
    static final String DEFAULT_QUESTIONNAIRE_ID = "TESTQUESTIONNAIREID";
    static final String CAMPAIGN_ID_WITH_DDI = "SAMPLETEST-PARADATA-v1";
    static final String QUESTIONNAIRE_ID_WITH_DDI = "SAMPLETEST-PARADATA-v1";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();

        Config config = new ConfigStub();
        FileUtils fileUtils = new FileUtils(config);
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new MetadataService(),
                fileUtils
                );

        responseControllerStatic = new ResponseController(
                surveyUnitApiPort
                , new SurveyUnitQualityService()
                , fileUtils
                , new ControllerUtils(fileUtils)
                , new AuthUtils(config)
                , new MetadataService()
                , new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub)
        );

        interrogationIdList = new ArrayList<>();
        interrogationIdList.add(new InterrogationId(DEFAULT_INTERROGATION_ID));
    }

    @BeforeEach
    void reset() throws IOException {
        dataProcessingContextPersistancePortStub.getMongoStub().clear();

        Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    //Survey units
    @Test
    void saveResponseFromXMLFileTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlFile(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "IN/WEB/SAMPLETEST-PARADATA-v1/reponse-platine/data.complete.validated.STPDv1.20231122164209.xml").toString()
                , Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "specs/SAMPLETEST-PARADATA-v1/ddi-SAMPLETEST-PARADATA-v1.xml").toString()
                , Mode.WEB
                , true
        );

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveOneFileNoCollected_NoNullPointerException(){
        Assertions.assertThatCode(() -> responseControllerStatic.saveResponsesFromXmlFile(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "IN/WEB/SAMPLETEST-NO-COLLECTED/differential/data/data_diff_no_collected.xml").toString()
                , Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "specs/SAMPLETEST-NO-COLLECTED/WEB/ddi_response_simple.xml").toString()
                , Mode.WEB
                , true
        )).doesNotThrowAnyException();

    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest() throws Exception {
        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "SAMPLETEST-PARADATA-v1"
                , Mode.WEB
        );

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest_noData() throws Exception {
        surveyUnitPersistencePortStub.getMongoStub().clear();

        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "TESTNODATA"
                , Mode.WEB
        );

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isEmpty();
    }
    //All data
    @Test
    void saveResponsesFromAllCampaignFoldersTests(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        responseControllerStatic.saveResponsesFromAllCampaignFolders();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }


    //Gets
    @Test
    void findResponsesByUEAndQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.findResponsesByInterrogationAndQuestionnaire(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);
        Assertions.assertThat(response.getBody().getFirst().getQuestionnaireId()).isEqualTo(DEFAULT_QUESTIONNAIRE_ID);
    }

    @Test
    void getLatestByUETest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub(surveyUnitPersistencePortStub);

        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.getLatestByInterrogation(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);
        Assertions.assertThat(response.getBody().getFirst().getQuestionnaireId()).isEqualTo(DEFAULT_QUESTIONNAIRE_ID);
        Assertions.assertThat(response.getBody().getFirst().getFileDate()).hasMonth(Month.FEBRUARY);
    }

    @Test
    void getLatestByUEOneObjectTest() {
        ResponseEntity<SurveyUnitSimplified> response = responseControllerStatic.getLatestByInterrogationOneObject(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID, Mode.WEB);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);
        Assertions.assertThat(response.getBody().getQuestionnaireId()).isEqualTo(DEFAULT_QUESTIONNAIRE_ID);
    }

    @Test
    void getLatestForUEListTest() {
        ResponseEntity<List<SurveyUnitSimplified>> response = responseControllerStatic.getLatestForInterrogationList(DEFAULT_QUESTIONNAIRE_ID, interrogationIdList);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);
    }

    // Perret tests
    @Test
    void getLatestByStatesSurveyDataTest() throws GenesisException {
        //GIVEN
        //Recent Collected already in stub
        //Old Collected
        Utils.addAdditionalSurveyUnitModelToMongoStub(DataState.COLLECTED,
                "C OLD C", //<Collected/External> <NEW or OLD> <Collected/Edited>
                "E OLD C",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        //Recent Edited
        Utils.addAdditionalSurveyUnitModelToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        //Old Edited
        Utils.addAdditionalSurveyUnitModelToMongoStub(DataState.EDITED,
                "C OLD E",
                "E OLD E",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        dataProcessingContextPersistancePortStub.getMongoStub().add(new DataProcessingContextDocument(
                "TEST-TABLEAUX", new ArrayList<>(), true

        ));


        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.findResponsesByInterrogationAndQuestionnaireLatestStates(
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) response.getBody();

        Assertions.assertThat(surveyUnitQualityToolDto).isNotNull();

        Assertions.assertThat(surveyUnitQualityToolDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getCollectedVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals("TESTVARID")
                && variableQualityToolDto.getIteration().equals(1)
        ).toList();
        Assertions.assertThat(variableQualityToolDtos).hasSize(1);
        VariableQualityToolDto variableQualityToolDto = variableQualityToolDtos.getFirst();

        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();


        variableQualityToolDtos = surveyUnitQualityToolDto.getExternalVariables().stream().filter(
                variableQualityToolDto1 -> variableQualityToolDto1.getVariableName().equals("TESTVARID")
                        && variableQualityToolDto1.getIteration().equals(1)
        ).toList();
        Assertions.assertThat(variableQualityToolDtos).hasSize(1);
        variableQualityToolDto = variableQualityToolDtos.getFirst();

        Assertions.assertThat(variableQualityToolDto.getVariableName())
                .isEqualTo("TESTVARID");
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }

    @Test
    void getLatestByStatesSurveyDataTest_invalid_collected() throws GenesisException {
        //GIVEN
        String variableName = "TABLEAUTIC21"; //Number variable
        Utils.addAdditionalSurveyUnitModelToMongoStub(DataState.COLLECTED,
                variableName,
                "?",
                "?",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        dataProcessingContextPersistancePortStub.getMongoStub().add(new DataProcessingContextDocument(
                "TEST-TABLEAUX", new ArrayList<>(), true

        ));


        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.findResponsesByInterrogationAndQuestionnaireLatestStates(
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(200);
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = (SurveyUnitQualityToolDto) response.getBody();

        Assertions.assertThat(surveyUnitQualityToolDto).isNotNull();

        Assertions.assertThat(surveyUnitQualityToolDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getCollectedVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(variableName)
                        && variableQualityToolDto.getIteration().equals(1)
        ).toList();
        Assertions.assertThat(variableQualityToolDtos).hasSize(1);
        VariableQualityToolDto variableQualityToolDto = variableQualityToolDtos.getFirst();

        Assertions.assertThat(variableQualityToolDto.getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("?");
    }

    @Test
    void saveEditedTest() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = CAMPAIGN_ID_WITH_DDI;
        String questionnaireId = QUESTIONNAIRE_ID_WITH_DDI;
        String varId = "PRENOM_C";
        String loopId = "B_PRENOMREP";
        String editedValue = "TESTPRENOMEDITED";

        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .build();

        variableInputDto.setVariableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                .build());

        newVariables.add(variableInputDto);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        SurveyUnitModel docSaved = surveyUnitPersistencePortStub.getMongoStub().get(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(docSaved.getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(docSaved.getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(docSaved.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(docSaved.getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(docSaved.getFileDate()).isNull();
        Assertions.assertThat(docSaved.getRecordDate()).isNotNull();
        Assertions.assertThat(docSaved.getExternalVariables()).isEmpty();

        Assertions.assertThat(docSaved.getCollectedVariables()).hasSize(1);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().value()).isEqualTo(editedValue);

        Assertions.assertThat(docSaved.getModifiedBy()).isNull();
    }

    @Test
    void saveEditedTest_DocumentEdited() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = CAMPAIGN_ID_WITH_DDI;
        String questionnaireId = QUESTIONNAIRE_ID_WITH_DDI;
        String varId = "PRENOM_C";
        String varId2 = "NB_SOEURS";
        String loopId = "B_PRENOMREP";
        String editedValue = "NOT A INT";

        //Variable 1
        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                        .build())
                .build();
        newVariables.add(variableInputDto);

        //Variable 2
        VariableInputDto variableInputDto2 = VariableInputDto.builder()
                .variableName(varId2)
                .iteration(1)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                        .build())
                .build();
        newVariables.add(variableInputDto2);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        //EDITED document assertions
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(3);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getFileDate()).isNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getExternalVariables()).isEmpty();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCollectedVariables()).hasSize(2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getCollectedVariables().getFirst().value()).isEqualTo(editedValue);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().get(1).getModifiedBy()).isNull();
    }

    @Test
    void saveEditedTest_DocumentFormatted() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = CAMPAIGN_ID_WITH_DDI;
        String questionnaireId = QUESTIONNAIRE_ID_WITH_DDI;
        String varId = "PRENOM_C";
        String varId2 = "NB_SOEURS";
        String loopId = "B_PRENOMREP";
        String editedValue = "NOT A INT";

        //Variable 1
        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                        .build())
                .build();
        newVariables.add(variableInputDto);

        //Variable 2
        VariableInputDto variableInputDto2 = VariableInputDto.builder()
                .variableName(varId2)
                .iteration(1)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                        .build())
                .build();
        newVariables.add(variableInputDto2);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(3);

        //FORMATTED document assertions
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getState()).isEqualTo(DataState.FORMATTED);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getFileDate()).isNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getExternalVariables()).isEmpty();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCollectedVariables()).hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCollectedVariables().getFirst().varId()).isEqualTo(varId2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCollectedVariables().getFirst().value()).isNotNull().isEmpty();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getModifiedBy()).isNull();
    }
    @Test
    void saveEditedTest_No_Metadata_Error() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = "TEST";
        String varId = "PRENOM_C";
        String editedValue = "TESTVALUE";

        //Variable 1
        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.EDITED)
                        .value(editedValue)
                        .build())
                .build();
        newVariables.add(variableInputDto);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);


        Assertions.assertThat(
            responseControllerStatic.saveEditedVariables(
                    surveyUnitInputDto
            ).getStatusCode()
        ).isEqualTo(HttpStatusCode.valueOf(404));
    }

    @Test
    void saveTest_With_Collected_State_Error(){
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String varId = "PRENOM_C";
        String editedValue = "TESTVALUE";

        //Variable 1
        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .variableStateInputDto(VariableStateInputDto.builder()
                        .state(DataState.COLLECTED) //Collected instead of EDITED
                        .value(editedValue)
                        .build())
                .iteration(1)
                .build();
        newVariables.add(variableInputDto);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(CAMPAIGN_ID_WITH_DDI)
                .mode(Mode.WEB)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(CAMPAIGN_ID_WITH_DDI)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        Assertions.assertThat(responseControllerStatic.saveEditedVariables(surveyUnitInputDto).getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }

    @Test
    void saveEditedTest_int() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = CAMPAIGN_ID_WITH_DDI;
        String questionnaireId = QUESTIONNAIRE_ID_WITH_DDI;
        String varId = "AGE";
        String loopId = "B_PRENOMREP";
        Integer editedValue = 5;

        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .build();

        variableInputDto.setVariableStateInputDto(VariableStateInputDto.builder()
                .state(DataState.EDITED)
                .value(editedValue)
                .build());

        newVariables.add(variableInputDto);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        SurveyUnitModel docSaved = surveyUnitPersistencePortStub.getMongoStub().get(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(docSaved.getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(docSaved.getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(docSaved.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(docSaved.getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(docSaved.getFileDate()).isNull();
        Assertions.assertThat(docSaved.getRecordDate()).isNotNull();
        Assertions.assertThat(docSaved.getExternalVariables()).isEmpty();

        Assertions.assertThat(docSaved.getCollectedVariables()).hasSize(1);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().value()).isEqualTo(editedValue.toString());

        Assertions.assertThat(docSaved.getModifiedBy()).isNull();
    }

    @Test
    void saveEditedTest_null() {
        //GIVEN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = CAMPAIGN_ID_WITH_DDI;
        String questionnaireId = QUESTIONNAIRE_ID_WITH_DDI;
        String varId = "AGE";
        String loopId = "B_PRENOMREP";
        Integer editedValue = null;

        List<VariableInputDto> newVariables = new ArrayList<>();
        VariableInputDto variableInputDto = VariableInputDto.builder()
                .variableName(varId)
                .iteration(1)
                .build();

        variableInputDto.setVariableStateInputDto(VariableStateInputDto.builder()
                .state(DataState.EDITED)
                .value(editedValue)
                .build());

        newVariables.add(variableInputDto);

        SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(newVariables)
                .build();

        // We need a response in database to retrieve campaignId from interrogationId and questionnaireId
        SurveyUnitModel suModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .questionnaireId(questionnaireId)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectedVariables(List.of())
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(suModel);

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        SurveyUnitModel docSaved = surveyUnitPersistencePortStub.getMongoStub().get(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(docSaved.getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(docSaved.getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(docSaved.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(docSaved.getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(docSaved.getFileDate()).isNull();
        Assertions.assertThat(docSaved.getRecordDate()).isNotNull();
        Assertions.assertThat(docSaved.getExternalVariables()).isEmpty();

        Assertions.assertThat(docSaved.getCollectedVariables()).hasSize(1);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(docSaved.getCollectedVariables().getFirst().value()).isNull();

        Assertions.assertThat(docSaved.getModifiedBy()).isNull();
    }


    @Test
    void getLatestForInterrogationListWithModes_performance() throws GenesisException {
        //GIVEN
        Utils.fillMongoStubForPerformances(DataState.COLLECTED,
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );


        //WHEN
        List<String> modes = new ArrayList<>();
        modes.add("WEB");
        List<InterrogationId> interrogationIds = Utils.generateInterrogationIds();
        long start = System.currentTimeMillis();
        ResponseEntity<List<SurveyUnitSimplified>> response = responseControllerStatic.getLatestForInterrogationListWithModes(
                DEFAULT_QUESTIONNAIRE_ID,
                modes,
                interrogationIds
        );
        long end = System.currentTimeMillis();
        long delta = end - start;

        //THEN
        Assertions.assertThat(delta < 3000).isTrue();
    }


}
