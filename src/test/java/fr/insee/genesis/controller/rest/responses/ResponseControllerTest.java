package fr.insee.genesis.controller.rest.responses;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateInputDto;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.LunaticXmlRawDataApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.rawdata.LunaticXmlRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.LunaticJsonPersistanceStub;
import fr.insee.genesis.stubs.LunaticXmlPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

class ResponseControllerTest {
    //Given
    static ResponseController responseControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static LunaticXmlPersistanceStub lunaticXmlPersistanceStub;
    static LunaticJsonPersistanceStub lunaticJsonPersistanceStub;
    static List<InterrogationId> interrogationIdList;
    //Constants
    static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";
    static final String DEFAULT_QUESTIONNAIRE_ID = "TESTQUESTIONNAIREID";
    static final String CAMPAIGN_ID_WITH_DDI = "SAMPLETEST-PARADATA-v1";
    static final String QUESTIONNAIRE_ID_WITH_DDI = "SAMPLETEST-PARADATA-v1";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        lunaticXmlPersistanceStub = new LunaticXmlPersistanceStub();
        LunaticXmlRawDataApiPort lunaticXmlRawDataApiPort = new LunaticXmlRawDataService(lunaticXmlPersistanceStub);

        lunaticJsonPersistanceStub = new LunaticJsonPersistanceStub();
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonPersistanceStub);

        Config config = new ConfigStub();
        FileUtils fileUtils = new FileUtils(config);
        responseControllerStatic = new ResponseController(
                surveyUnitApiPort
                , new SurveyUnitQualityService()
                , lunaticXmlRawDataApiPort
                , lunaticJsonRawDataApiPort
                , fileUtils
                , new ControllerUtils(fileUtils)
                , new AuthUtils(config)
        );

        interrogationIdList = new ArrayList<>();
        interrogationIdList.add(new InterrogationId(DEFAULT_INTERROGATION_ID));
    }

    @BeforeEach
    void reset() throws IOException {
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
                , true
        );

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void saveResponsesFromXmlCampaignFolderTest_noData() throws Exception {
        surveyUnitPersistencePortStub.getMongoStub().clear();

        responseControllerStatic.saveResponsesFromXmlCampaignFolder(
                "TESTNODATA"
                , Mode.WEB
                , true
        );

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isEmpty();
    }

    //Raw data
    //xml
    @Test
    void saveXmlRawDataFromFileTest() throws Exception {
        lunaticXmlPersistanceStub.getMongoStub().clear();

        responseControllerStatic.saveRawResponsesFromXmlFile(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "IN/WEB/SAMPLETEST-PARADATA-v1/reponse-platine/data.complete.validated.STPDv1.20231122164209.xml").toString()
                , Mode.WEB
        );

        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub()).isNotEmpty();
        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub().getFirst().getProcessDate()).isNull();
        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub().getFirst().getLunaticXmlData()).isNotNull();
        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub().getFirst().getLunaticXmlData().getSurveyUnits()).isNotNull().isNotEmpty();


    }

    @Test
    void saveXmlRawDataFromFolderTest() throws Exception {
        lunaticXmlPersistanceStub.getMongoStub().clear();

        responseControllerStatic.saveRawResponsesFromXmlCampaignFolder(
                "SAMPLETEST-PARADATA-v1"
                , Mode.WEB
        );

        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub()).isNotEmpty();
    }

    //json
    @Test
    void saveJsonRawDataFromStringTest(){
        lunaticJsonPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";

        responseControllerStatic.saveRawResponsesFromJsonBody(
                campaignId
                , Mode.WEB
                , "{\"testdata\": \"test\"}"
        );

        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub()).isNotEmpty();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getCampaignId()).isNotNull().isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getData().get("testdata")).isNotNull();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getData().get("testdata")).isNotNull().isEqualTo("test");

        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getProcessDate()).isNull();

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
    void findAllResponsesByQuestionnaireTest() {
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "OUT", DEFAULT_QUESTIONNAIRE_ID);
        File dir = new File(String.valueOf(path));
        FileSystemUtils.deleteRecursively(dir);

        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(Files.exists(path)).isTrue();
        File[] dircontents = dir.listFiles();
        Assertions.assertThat(dircontents).hasSize(1);
        Assertions.assertThat(dircontents[0].length()).isPositive().isNotNull();
        FileSystemUtils.deleteRecursively(dir);
        dir.deleteOnExit();
    }

    @Test
    void getAllResponsesByQuestionnaireTestSequential() throws IOException {
        //Given
        surveyUnitPersistencePortStub.getMongoStub().clear();

        for (int i = 0; i < Constants.BATCH_SIZE + 2; i++) {
            Utils.addAdditionalSurveyUnitModelToMongoStub("TESTCAMPAIGNID", DEFAULT_INTERROGATION_ID + i,
                    LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                    surveyUnitPersistencePortStub);
        }

        //When
        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(DEFAULT_QUESTIONNAIRE_ID);

        //Then
        Assertions.assertThat(response).isNotNull();
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();

        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().toFile()).isNotNull().exists();

        Files.deleteIfExists(response.getBody());
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
    void getLatestByStatesSurveyDataTest(){
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


        //WHEN
        ResponseEntity<SurveyUnitQualityToolDto> response = responseControllerStatic.findResponsesByInterrogationAndQuestionnaireLatestStates(
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //THEN
        SurveyUnitQualityToolDto surveyUnitDto = response.getBody();
        Assertions.assertThat(surveyUnitDto).isNotNull();

        Assertions.assertThat(surveyUnitDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitDto.getCollectedVariables().stream().filter(
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


        variableQualityToolDtos = surveyUnitDto.getExternalVariables().stream().filter(
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


        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getFileDate()).isNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getExternalVariables()).isEmpty();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables()).hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().value()).isEqualTo(editedValue);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getModifiedBy()).isNull();
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

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        //EDITED document assertions
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getState()).isEqualTo(DataState.EDITED);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getFileDate()).isNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getExternalVariables()).isEmpty();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables()).hasSize(2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().varId()).isEqualTo(varId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().scope()).isEqualTo(loopId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().value()).isEqualTo(editedValue);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getModifiedBy()).isNull();
    }

    @Test
    void saveEditedTest_DocumentForced() {
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

        //WHEN
        responseControllerStatic.saveEditedVariables(surveyUnitInputDto);

        //THEN
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);

        //FORCED document assertions
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getQuestionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getLast().getState()).isEqualTo(DataState.FORCED);
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

        Assertions.assertThat(responseControllerStatic.saveEditedVariables(surveyUnitInputDto).getStatusCode()).isEqualTo(HttpStatusCode.valueOf(400));
    }
}
