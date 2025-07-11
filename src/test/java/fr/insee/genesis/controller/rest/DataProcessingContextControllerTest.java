package fr.insee.genesis.controller.rest;


import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

class DataProcessingContextControllerTest {
    //Given
    private static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    private static DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;
    private static DataProcessingContextController dataProcessingContextController;

    @BeforeEach
    void clean() throws IOException {
        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        dataProcessingContextController = new DataProcessingContextController(
                new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub),
                new FileUtils(new ConfigStub())
        );
        //Clean genesis_deleted_schedules log folder
        if(Files.exists(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME))) {
            try(Stream<Path> files = Files.list(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                    .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME))){
                for (Path filePath : files.toList()) {
                    Files.deleteIfExists(filePath);
                }
            }
        }
    }

    @Test
    void getAllSchedulesTest() {
        //When
        ResponseEntity<Object> response = dataProcessingContextController.getAllSchedules();

        //Then
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void addScheduleWithoutEncryptionTest() {
        //When
        String partitionId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithoutEncryptionTest_nullServiceToCall() {
        //When
        String partitionId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = null;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.MAIN);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithEncryptionTest() {
        //When
        String partitionId = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, true,
                "testvault/testkey",
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("OUT_ENCRYPTED").resolve(partitionId).toString(),
                false
        );

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).isNotEmpty();

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNotNull();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getInputPath()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getOutputFolder()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getVaultPath()).isEqualTo(
                "testvault/testkey");
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().isUseSignature()).isFalse();
    }

    @Test
    void addAdditionnalScheduleTest() {
        //When
        String partitionId = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleDedupTest()  {
        //Given
        addNewDocumentToStub();

        //When
        String partitionId = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).isNotEmpty().hasSize(1);

        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals(partitionId)).toList();

        DataProcessingContextDocument dataProcessingContextDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getLastExecution()).isNull();

        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void updateLastExecutionTest(){
        //Given
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecution("TESTSURVEY", LocalDateTime.now());

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void setLastExecutionTestToNull(){
        //Given
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecution("TESTSURVEY", null);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNull();
    }

    @Test
    void setLastExecutionTest(){
        //Given
        LocalDateTime date = LocalDateTime.now();
        addNewDocumentToStub();

        //When
        dataProcessingContextController.setSurveyLastExecution("TESTSURVEY", date);

        //Then
        List<DataProcessingContextDocument> mongoStubFiltered = dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isEqualTo(date);
    }

    @Test
    void wrongFrequencyTest(){
        //When+Then
        String partitionId = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "ERROR";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        ResponseEntity<Object> response = dataProcessingContextController.saveSchedule(partitionId, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void notFoundTest(){
        //When+Then
        ResponseEntity<Object> response = dataProcessingContextController.setSurveyLastExecution("ERROR", LocalDateTime.now());
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void deleteScheduleTest(){
        //When
        dataProcessingContextController.deleteSchedules("TESTSURVEY");

        //Then
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEY")
        ).isEmpty();
    }

    @Test
    void deleteExpiredScheduleTest_execution() throws IOException, GenesisException {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED",
                null,
                new ArrayList<>(),
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2000").doesNotContain("5023");
    }

    @Test
    void deleteExpiredScheduleTest_wholeSurvey() throws IOException, GenesisException {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED",
                null,
                new ArrayList<>(),
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2002, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2002, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")
        ).hasSize(1);
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isEmpty();


        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2001","2002");
    }

    @Test
    void deleteExpiredScheduleTest_appendLog() throws IOException, GenesisException {
        //Given
        DataProcessingContextModel dataProcessingContextModel = new DataProcessingContextModel(
                null,
                "TESTSURVEYADDED2",
                null,
                new ArrayList<>(),
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));

        //When
        dataProcessingContextController.deleteExpiredSchedules();
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(DataProcessingContextMapper.INSTANCE.modelToDocument(dataProcessingContextModel));
        dataProcessingContextController.deleteExpiredSchedules();

        //Then
        //Expired schedules deleted
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub()).filteredOn(dataProcessingContextDocument ->
                dataProcessingContextDocument.getPartitionId().equals("TESTSURVEYADDED2")
        ).isNotEmpty();
        Assertions.assertThat(dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(dataProcessingContextDocument ->
                dataProcessingContextDocument
                        .getPartitionId().equals("TESTSURVEYADDED2")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedules to only one log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED2.json")
                .toFile()).exists().content().isNotEmpty().contains("2000","2001");
    }

    private void addNewDocumentToStub() {
        DataProcessingContextDocument dataProcessingContextDocumentTest = new DataProcessingContextDocument(
                "TESTSURVEY",
                new ArrayList<>(),
                false
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        dataProcessingContextDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        dataProcessingContextPersistancePortStub.getMongoStub().add(dataProcessingContextDocumentTest);
    }
}
