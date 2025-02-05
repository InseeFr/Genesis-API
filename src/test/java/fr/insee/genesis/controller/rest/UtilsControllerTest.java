package fr.insee.genesis.controller.rest;

import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

class UtilsControllerTest {
    //Given
    static UtilsController utilsControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    static List<InterrogationId> interrogationIdList;
    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        utilsControllerStatic = new UtilsController(
                surveyUnitApiPort
                , new VolumetryLogService(new ConfigStub())
        );

        interrogationIdList = new ArrayList<>();
        interrogationIdList.add(new InterrogationId(defaultIdUE));
    }

    @BeforeEach
    void reset() throws IOException {
        //MongoDB stub management
        surveyUnitPersistencePortStub.getMongoStub().clear();

        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(variable);
        variable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V1")
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);
        collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V2")
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .iteration(2)
                .build();
        collectedVariableList.add(collectedVariable);

        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(defaultIdUE)
                .questionnaireId(defaultIdQuest)
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
        //SAMPLETEST-NO-COLLECTED
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-NO-COLLECTED")
                .resolve("differential")
                .resolve("data")
                .resolve("data_diff_no_collected.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-NO-COLLECTED")
                            .resolve("data_diff_no_collected.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-NO-COLLECTED")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data_diff_no_collected.xml")
            );
        }
    }




    @Test
    void saveVolumetryTest() throws IOException {
        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

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
        utilsControllerStatic.saveVolumetry();
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

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
        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

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
        addAdditionalDtoToMongoStub("TESTQUEST");
        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN2","TESTQUEST2");

        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

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
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

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




    // Utilities

    private void addAdditionalDtoToMongoStub(String idQuestionnaire) {
        addAdditionalDtoToMongoStub("TESTIDCAMPAIGN",idQuestionnaire);
    }

    private void addAdditionalDtoToMongoStub(String idCampaign, String idQuestionnaire) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(variable);
        variable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V1")
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);
        collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V2")
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .iteration(2)
                .build();
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId(idCampaign)
                .mode(Mode.WEB)
                .interrogationId(defaultIdUE)
                .questionnaireId(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

}
