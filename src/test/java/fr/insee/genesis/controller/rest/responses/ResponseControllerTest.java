package fr.insee.genesis.controller.rest.responses;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitId;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
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

    static List<SurveyUnitId> surveyUnitIdList;
    //Constants
    static final String DEFAULT_ID_UE = "TESTIDUE";
    static final String DEFAULT_ID_QUEST = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        lunaticXmlPersistanceStub = new LunaticXmlPersistanceStub();
        LunaticXmlRawDataApiPort lunaticXmlRawDataApiPort = new LunaticXmlRawDataService(lunaticXmlPersistanceStub);

        lunaticJsonPersistanceStub = new LunaticJsonPersistanceStub();
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonPersistanceStub);

        FileUtils fileUtils = new FileUtils(new ConfigStub());
        responseControllerStatic = new ResponseController(
                surveyUnitApiPort
                , new SurveyUnitQualityService()
                , lunaticXmlRawDataApiPort
                , lunaticJsonRawDataApiPort
                , fileUtils
                , new ControllerUtils(fileUtils)
        );

        surveyUnitIdList = new ArrayList<>();
        surveyUnitIdList.add(new SurveyUnitId(DEFAULT_ID_UE));
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
        Assertions.assertThat(lunaticXmlPersistanceStub.getMongoStub().getFirst().getLunaticXmlData()).isNotNull();
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

    @Test
    void saveResponsesFromAllCampaignFoldersTests(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        responseControllerStatic.saveResponsesFromAllCampaignFolders();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }
    //json
    @Test
    void saveJsonRawDataFromStringTest() throws Exception {
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
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getData().get("testdata").asText()).isNotNull().isEqualTo("test");

        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getProcessDate()).isNull();

    }


    //Gets
    @Test
    void findResponsesByUEAndQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.findResponsesByUEAndQuestionnaire(DEFAULT_ID_UE, DEFAULT_ID_QUEST);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(DEFAULT_ID_UE);
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo(DEFAULT_ID_QUEST);
    }

    @Test
    void findAllResponsesByQuestionnaireTest() {
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "OUT", DEFAULT_ID_QUEST);
        File dir = new File(String.valueOf(path));
        FileSystemUtils.deleteRecursively(dir);

        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(DEFAULT_ID_QUEST);

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
            Utils.addAdditionalDtoToMongoStub("TESTIDCAMPAIGN", DEFAULT_ID_UE + i,
                    LocalDateTime.of(2023, 1, 1, 0, 0, 0),
                    LocalDateTime.of(2024, 1, 1, 0, 0, 0),
                    surveyUnitPersistencePortStub);
        }

        //When
        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(DEFAULT_ID_QUEST);

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
        Utils.addAdditionalDtoToMongoStub(surveyUnitPersistencePortStub);

        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.getLatestByUE(DEFAULT_ID_UE, DEFAULT_ID_QUEST);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(DEFAULT_ID_UE);
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo(DEFAULT_ID_QUEST);
        Assertions.assertThat(response.getBody().getFirst().getFileDate()).hasMonth(Month.FEBRUARY);
    }

    @Test
    void getLatestByUEOneObjectTest() {
        ResponseEntity<SurveyUnitSimplified> response = responseControllerStatic.getLatestByUEOneObject(DEFAULT_ID_UE, DEFAULT_ID_QUEST, Mode.WEB);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getIdUE()).isEqualTo(DEFAULT_ID_UE);
        Assertions.assertThat(response.getBody().getIdQuest()).isEqualTo(DEFAULT_ID_QUEST);
    }

    @Test
    void getLatestForUEListTest() {
        ResponseEntity<List<SurveyUnitSimplified>> response = responseControllerStatic.getLatestForUEList(DEFAULT_ID_QUEST, surveyUnitIdList);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(DEFAULT_ID_UE);
    }

    // Perret tests
    @Test
    void getLatestByStatesSurveyDataTest(){
        //GIVEN
        //Recent Collected already in stub
        //Old Collected
        Utils.addAdditionalDtoToMongoStub(DataState.COLLECTED,
                "C OLD C", //<Collected/External> <NEW or OLD> <Collected/Edited>
                "E OLD C",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        //Recent Edited
        Utils.addAdditionalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );

        //Old Edited
        Utils.addAdditionalDtoToMongoStub(DataState.EDITED,
                "C OLD E",
                "E OLD E",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0),
                surveyUnitPersistencePortStub
        );


        //WHEN
        ResponseEntity<SurveyUnitDto> response = responseControllerStatic.findResponsesByUEAndQuestionnaireLatestStates(
                DEFAULT_ID_UE,
                DEFAULT_ID_QUEST
        );


        //THEN
        SurveyUnitDto surveyUnitDto = response.getBody();
        Assertions.assertThat(surveyUnitDto).isNotNull();

        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(DEFAULT_ID_UE);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }



}
