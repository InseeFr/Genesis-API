package fr.insee.genesis.controller.rest;


import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.rawdata.RawResponseService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.service.volumetry.VolumetryLogService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistencePortStub;
import fr.insee.genesis.stubs.RawResponseDataPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitQualityToolPerretAdapterStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
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
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static fr.insee.genesis.TestConstants.DEFAULT_INTERROGATION_ID;

class UtilsControllerTest {
    //Given
    static UtilsController utilsControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistencePort;
    static RawResponseDataPersistanceStub rawResponseDataPersistanceStub;
    static DataProcessingContextPersistancePort contextStub = new DataProcessingContextPersistancePortStub();
    static SurveyUnitQualityToolPerretAdapterStub surveyUnitQualityToolPerretAdapterStub;
    static List<InterrogationId> interrogationIdList;
    static FileUtils fileUtils = new FileUtils(new ConfigStub());
    static ControllerUtils controllerUtils = new ControllerUtils(fileUtils);
    static QuestionnaireMetadataService metadataService = new QuestionnaireMetadataService(new QuestionnaireMetadataPersistencePortStub());
    static SurveyUnitService surveyUnitService = new SurveyUnitService(surveyUnitPersistencePortStub, metadataService, fileUtils);
    static SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();

    static Path logFileFolder = Path.of(new ConfigStub().getLogFolder()).resolve(Constants.VOLUMETRY_FOLDER_NAME);

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        lunaticJsonRawDataPersistencePort = new LunaticJsonRawDataPersistanceStub();
        rawResponseDataPersistanceStub = new RawResponseDataPersistanceStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub, metadataService, fileUtils);
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(
                        lunaticJsonRawDataPersistencePort,
                        controllerUtils,
                        metadataService,
                        surveyUnitService,
                        surveyUnitQualityService,
                        fileUtils,
                        new DataProcessingContextService(
                                new DataProcessingContextPersistancePortStub(),
                                surveyUnitPersistencePortStub),
                        surveyUnitQualityToolPerretAdapterStub,
                        new ConfigStub(),
                        contextStub);

        RawResponseService rawResponseApiPort = new RawResponseService(
                controllerUtils,
                metadataService,
                surveyUnitService,
                surveyUnitQualityService,
                surveyUnitQualityToolPerretAdapterStub,
                new DataProcessingContextService(
                        new DataProcessingContextPersistancePortStub(),
                        surveyUnitPersistencePortStub),
                fileUtils,
                new ConfigStub(),
                rawResponseDataPersistanceStub
        );


        utilsControllerStatic = new UtilsController(
                new VolumetryLogService(new ConfigStub())
                , surveyUnitApiPort
                , lunaticJsonRawDataApiPort
                , rawResponseApiPort
        );

        interrogationIdList = new ArrayList<>();
        interrogationIdList.add(new InterrogationId(DEFAULT_INTERROGATION_ID));
    }

    @BeforeEach
    void reset() throws IOException {
        //MongoDB stub management
        surveyUnitPersistencePortStub.getMongoStub().clear();

        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(variable);
        variable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTPARENTID")
                .build();
        collectedVariableList.add(collectedVariable);
        collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .scope("TESTSCOPE")
                .iteration(2)
                .parentId("TESTPARENTID")
                .build();
        collectedVariableList.add(collectedVariable);

        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TEST-TABLEAUX")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectionInstrumentId("TEST-TABLEAUX")
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
        //SAMPLETEST-PARADATA-V1
        //Root
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-V1")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //Differential data
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-V1")
                .resolve("differential")
                .resolve("data")
                .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                .toFile().exists()
        ) {
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.partial.STPDv1.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V1")
                            .resolve("differential")
                            .resolve("data")
                            .resolve("data.complete.validated.STPDv1.20231122164209.xml")
            );
        }
        //SAMPLETEST-PARADATA-V2
        if (!testResourcesPath
                .resolve("IN")
                .resolve("WEB")
                .resolve("SAMPLETEST-PARADATA-V2")
                .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                .toFile().exists()
        ){
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V2")
                            .resolve("data.complete.partial.STPDv2.20231122164209.xml")
            );
            Files.copy(
                    testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V2")
                            .resolve("reponse-platine")
                            .resolve("data.complete.validated.STPDv2.20231122164209.xml")
                    , testResourcesPath
                            .resolve("IN")
                            .resolve("WEB")
                            .resolve("SAMPLETEST-PARADATA-V2")
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
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        try(Stream<Path> pathStream = Files.list(logFileFolder).filter(logFilePath ->
                logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_FILE_SUFFIX)
                        && !logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_RAW_FILE_SUFFIX)
        )) {
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst())
                    .exists().content().isNotEmpty()
                    .contains("TEST-TABLEAUX;1");
        }
    }

    @Test
    void saveVolumetryTest_overwrite() throws IOException {
        //WHEN
        utilsControllerStatic.saveVolumetry();
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        try(Stream<Path> pathStream = Files.list(logFileFolder).filter(logFilePath ->
                logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_FILE_SUFFIX)
                        && !logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_RAW_FILE_SUFFIX)
        )) {
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst())
                    .exists().content().isNotEmpty()
                    .contains("TEST-TABLEAUX;1")
                    .doesNotContain("TEST-TABLEAUX;1\nTEST-TABLEAUX;1");
        }
    }

    @Test
    void saveVolumetryTest_additionnal_campaign() throws IOException {
        //Given
        addAdditionalSurveyUnitModelToMongoStub("TEST-TABLEAUX2","TEST-TABLEAUX2");

        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        try(Stream<Path> pathStream = Files.list(logFileFolder).filter(logFilePath ->
                logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_FILE_SUFFIX)
                && !logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_RAW_FILE_SUFFIX)
        )){
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst())
                    .isNotNull().exists().content().isNotEmpty().contains("TEST-TABLEAUX;1").contains("TEST-TABLEAUX2;1");
        }
    }
    @Test
    void saveVolumetryTest_additionnal_campaign_and_document() throws IOException {
        //Given
        addAdditionalSurveyUnitModelToMongoStub("TEST-TABLEAUX");
        addAdditionalSurveyUnitModelToMongoStub("TEST-TABLEAUX2","TEST-TABLEAUX2");

        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        try(Stream<Path> pathStream = Files.list(logFileFolder).filter(logFilePath ->
                logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_FILE_SUFFIX)
                        && !logFilePath.getFileName().toString().contains(Constants.VOLUMETRY_RAW_FILE_SUFFIX)
        )){
            List<Path> pathList = pathStream.toList();
            Assertions.assertThat(pathList).hasSize(1);
            Assertions.assertThat(pathList.getFirst())
                    .isNotNull().exists().content().isNotEmpty().contains("TEST-TABLEAUX;2").contains("TEST-TABLEAUX2;1");
        }
    }

    @Test
    void cleanOldVolumetryLogFiles() throws IOException {
        //GIVEN
        Path oldLogFilePath = logFileFolder
                .resolve(LocalDateTime.of(2000,1,1,0,0,0).format(DateTimeFormatter.ofPattern(Constants.VOLUMETRY_FILE_DATE_FORMAT))
                        + Constants.VOLUMETRY_FILE_SUFFIX + ".csv");

        Files.createDirectories(oldLogFilePath.getParent());
        Files.write(oldLogFilePath, "test".getBytes());

        //WHEN
        ResponseEntity<Object> response = utilsControllerStatic.saveVolumetry();

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(oldLogFilePath).doesNotExist();
    }




    // Utilities

    private void addAdditionalSurveyUnitModelToMongoStub(String collectionInstrumentId) {
        addAdditionalSurveyUnitModelToMongoStub("TEST-TABLEAUX",collectionInstrumentId);
    }

    private void addAdditionalSurveyUnitModelToMongoStub(String campaignId, String collectionInstrumentId) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(variable);
        variable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTPARENTID")
                .build();
        collectedVariableList.add(collectedVariable);
        collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .scope("TESTSCOPE")
                .iteration(2)
                .parentId("TESTPARENTID")
                .build();
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentSurveyUnitModel = SurveyUnitModel.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .collectionInstrumentId(collectionInstrumentId)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentSurveyUnitModel);
    }

    @AfterEach
    @SneakyThrows
    void tearDown() {
        FileSystemUtils.deleteRecursively(logFileFolder);
    }
}
