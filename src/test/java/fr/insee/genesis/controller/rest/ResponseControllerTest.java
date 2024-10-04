package fr.insee.genesis.controller.rest;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.dto.perret.SurveyUnitPerret;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitId;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

class ResponseControllerTest {
    //Given
    static ResponseController responseControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    static List<SurveyUnitId> surveyUnitIdList;
    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        FileUtils fileUtils = new FileUtils(new ConfigStub());
        responseControllerStatic = new ResponseController(
                surveyUnitApiPort
                , new SurveyUnitQualityService()
                , new VolumetryLogService(new ConfigStub())
                , fileUtils
                , new ControllerUtils(fileUtils)
        );

        surveyUnitIdList = new ArrayList<>();
        surveyUnitIdList.add(new SurveyUnitId(defaultIdUE));
    }

    @BeforeEach
    void reset() throws IOException {
        //MongoDB stub management
        surveyUnitPersistencePortStub.getMongoStub().clear();

        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build());

        //Test file management
        //Clean DONE folder
        Path testResourcesPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        if (testResourcesPath.resolve("DONE").toFile().exists())
            Files.walk(testResourcesPath.resolve("DONE"))
                    .sorted(Comparator.reverseOrder())
                    .map(Path::toFile)
                    .forEach(File::delete);

        //Recreate data files
        //SAMPLETEST-PARADATA-v1
        //Root
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //Differential data
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v1")
                .resolve("differential")
                .resolve("data")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ) {
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //SAMPLETEST-PARADATA-v2
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-v2")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-v2")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
            );
        }
    }


    //When + Then
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

    @Test
    void saveResponsesFromAllCampaignFoldersTests(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        responseControllerStatic.saveResponsesFromAllCampaignFolders();

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotEmpty();
    }

    @Test
    void findResponsesByUEAndQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.findResponsesByUEAndQuestionnaire(defaultIdUE, defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(defaultIdUE);
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo(defaultIdQuest);
    }

    @Test
    void findAllResponsesByQuestionnaireTest() {
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "OUT", defaultIdQuest);
        File dir = new File(String.valueOf(path));
        FileSystemUtils.deleteRecursively(dir);

        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(Files.exists(path)).isTrue();
        File[] dir_contents = dir.listFiles();
        Assertions.assertThat(dir_contents).hasSize(1);
        Assertions.assertThat(dir_contents[0].length()).isPositive().isNotNull();
        FileSystemUtils.deleteRecursively(dir);
        dir.deleteOnExit();
    }

    @Test
    void getAllResponsesByQuestionnaireTestSequential() throws IOException {
        //Given
        surveyUnitPersistencePortStub.getMongoStub().clear();

        for (int i = 0; i < Constants.BATCH_SIZE + 2; i++) {
            List<Variable> externalVariableList = new ArrayList<>();
            Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
            externalVariableList.add(variable);

            List<CollectedVariable> collectedVariableList = new ArrayList<>();
            CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
            collectedVariableList.add(collectedVariable);

            surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                    .idCampaign("TESTIDCAMPAIGN")
                    .mode(Mode.WEB)
                    .idUE(defaultIdUE + i)
                    .idQuest(defaultIdQuest)
                    .state(DataState.COLLECTED)
                    .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                    .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                    .externalVariables(externalVariableList)
                    .collectedVariables(collectedVariableList)
                    .build());
        }

        //When
        ResponseEntity<Path> response = responseControllerStatic.findAllResponsesByQuestionnaire(defaultIdQuest);

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
        addAdditionnalDtoToMongoStub();

        ResponseEntity<List<SurveyUnitModel>> response = responseControllerStatic.getLatestByUE(defaultIdUE, defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(defaultIdUE);
        Assertions.assertThat(response.getBody().getFirst().getIdQuest()).isEqualTo(defaultIdQuest);
        Assertions.assertThat(response.getBody().getFirst().getFileDate()).hasMonth(Month.FEBRUARY);
    }

    @Test
    void getLatestByUEOneObjectTest() {
        ResponseEntity<SurveyUnitSimplified> response = responseControllerStatic.getLatestByUEOneObject(defaultIdUE, defaultIdQuest, Mode.WEB);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getIdUE()).isEqualTo(defaultIdUE);
        Assertions.assertThat(response.getBody().getIdQuest()).isEqualTo(defaultIdQuest);
    }

    @Test
    void getLatestForUEListTest() {
        ResponseEntity<List<SurveyUnitSimplified>> response = responseControllerStatic.getLatestForUEList(defaultIdQuest, surveyUnitIdList);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(defaultIdUE);
    }

    @Test
    void getAllIdUEsByQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitId>> response = responseControllerStatic.getAllIdUEsByQuestionnaire(defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(defaultIdUE);
    }

    @Test
    void getModesByQuestionnaireTest() {
        ResponseEntity<List<Mode>> response = responseControllerStatic.getModesByQuestionnaire(defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignTest() {
        ResponseEntity<List<Mode>> response = responseControllerStatic.getModesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getCampaignsTest() {
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<Set<String>> response = responseControllerStatic.getCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                "TESTIDCAMPAIGN","TESTCAMPAIGN2");
    }

    @Test
    void getQuestionnairesTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        ResponseEntity<Set<String>> response = responseControllerStatic.getQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                defaultIdQuest,"TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        ResponseEntity<Set<String>> response = responseControllerStatic.getQuestionnairesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                defaultIdQuest,"TESTQUESTIONNAIRE2");
    }

    @Test
    void getCampaignsWithQuestionnairesTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<List<CampaignWithQuestionnaire>> response = responseControllerStatic.getCampaignsWithQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(campaignWithQuestionnaire ->
                        campaignWithQuestionnaire.getIdCampaign().equals("TESTIDCAMPAIGN")
                                || campaignWithQuestionnaire.getIdCampaign().equals("TESTCAMPAIGN2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getIdCampaign().equals("TESTIDCAMPAIGN")
        ).findFirst().get().getQuestionnaires()).containsExactly(defaultIdQuest, "TESTQUESTIONNAIRE2");

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getIdCampaign().equals("TESTCAMPAIGN2")
        ).findFirst().get().getQuestionnaires()).containsExactly("TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesWithCampaignsTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");
        addAdditionnalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2");

        ResponseEntity<List<QuestionnaireWithCampaign>> response = responseControllerStatic.getQuestionnairesWithCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(questionnaireWithCampaign ->
                questionnaireWithCampaign.getIdQuestionnaire().equals(defaultIdQuest)
                        || questionnaireWithCampaign.getIdQuestionnaire().equals("TESTQUESTIONNAIRE2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getIdQuestionnaire().equals(defaultIdQuest)
        ).findFirst().get().getCampaigns()).containsExactly("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getIdQuestionnaire().equals("TESTQUESTIONNAIRE2")
        ).findFirst().get().getCampaigns()).containsExactly("TESTIDCAMPAIGN", "TESTCAMPAIGN2");
    }

    @Test
    void saveVolumetryTest() throws IOException {
        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                        .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                        .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                                + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void saveVolumetryTest_overwrite() throws IOException {
        //WHEN
        responseControllerStatic.saveVolumetry();
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1").doesNotContain("TESTIDCAMPAIGN;1\nTESTIDCAMPAIGN;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void saveVolumetryTest_additionnal_campaign() throws IOException {
        //Given
        addAdditionnalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;1").contains("TESTIDCAMPAIGN2;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }
    @Test
    void saveVolumetryTest_additionnal_campaign_and_document() throws IOException {
        //Given
        addAdditionnalDtoToMongoStub("TESTQUEST");
        addAdditionnalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Path logFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.now().format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(logFilePath).exists().content().isNotEmpty().contains("TESTIDCAMPAIGN;2").contains("TESTIDCAMPAIGN2;1");

        //CLEAN
        Files.deleteIfExists(logFilePath);
    }

    @Test
    void cleanOldVolumetryLogFiles() throws IOException {
        //GIVEN
        Path oldLogFilePath = Path.of(
                        new ConfigStub().getLogFolder())
                .resolve(Constants.VOLUMETRY_FOLDER_NAME)
                .resolve(LocalDate.of(2000,1,1).format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");

        Files.createDirectories(oldLogFilePath.getParent());
        Files.write(oldLogFilePath, "test".getBytes());

        //WHEN
        ResponseEntity<Object> response = responseControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(oldLogFilePath).doesNotExist();

        //CLEAN
        try(Stream<Path> stream = Files.walk(oldLogFilePath.getParent())){
            for(Path filePath : stream.filter(path -> path.getFileName().toString().endsWith(".csv")).toList()){
                Files.deleteIfExists(filePath);
            }
        }
    }

    // Perret tests
    @Test
    void getPerretSurveyDataTest(){
        //GIVEN
        //Recent Collected already in stub
        //Old Collected
        addAdditionnalDtoToMongoStub(DataState.COLLECTED,
                "C OLD C", //<Collected/External> <NEW or OLD> <Collected/Edited>
                "E OLD C",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0)
        );

        //Recent Edited
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );

        //Old Edited
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C OLD E",
                "E OLD E",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0)
        );


        //WHEN
        ResponseEntity<SurveyUnitPerret> response = responseControllerStatic.findResponsesByUEAndQuestionnairePerret(
                defaultIdUE,
                defaultIdQuest
        );


        //THEN
        SurveyUnitPerret surveyUnitPerret = response.getBody();
        Assertions.assertThat(surveyUnitPerret).isNotNull();

        Assertions.assertThat(surveyUnitPerret.getSurveyUnitId()).isEqualTo(defaultIdUE);

        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");

        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }



    // Utilities
    private void addAdditionnalDtoToMongoStub() {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idCampaign, String idQuestionnaire) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign(idCampaign)
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(DataState state,
                                              String collectedVariableValue,
                                              String externalVariableValue,
                                              LocalDateTime fileDate,
                                              LocalDateTime recordDate) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{collectedVariableValue}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }
}
