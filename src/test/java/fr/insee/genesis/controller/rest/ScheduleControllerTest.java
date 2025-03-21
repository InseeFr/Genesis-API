package fr.insee.genesis.controller.rest;


import cucumber.TestConstants;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.mappers.ScheduleDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.ScheduleApiPortStub;
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

class ScheduleControllerTest {
    //Given
    private static ScheduleController scheduleController;

    private static ScheduleApiPortStub scheduleApiPortStub;

    @BeforeEach
    void clean() throws IOException {
        scheduleApiPortStub = new ScheduleApiPortStub();
        scheduleController = new ScheduleController(scheduleApiPortStub, new FileUtils(new ConfigStub()));
        //Clean genesis_deleted_schedules log folder
        if(Files.exists(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME))) {
            for (Path filePath : Files.list(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                    .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)).toList()) {
                Files.deleteIfExists(filePath);
            }
        }
        scheduleController = new ScheduleController(scheduleApiPortStub, new FileUtils(new ConfigStub()));
    }

    @Test
    void getAllSchedulesTest() {
        //When
        ResponseEntity<Object> response = scheduleController.getAllSchedules();

        //Then
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void addScheduleWithoutEncryptionTest() {
        //When
        String surveyName = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty();

        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithoutEncryptionTest_nullServiceToCall() {
        //When
        String surveyName = "TESTADDSURVEY";
        ServiceToCall serviceToCall = null;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "TEST", "TEST", false);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty();

        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.MAIN);
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addScheduleWithEncryptionTest() {
        //When
        String surveyName = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, true,
                "testvault/testkey",
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("OUT_ENCRYPTED").resolve(surveyName).toString(),
                false
        );

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty();

        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();

        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNotNull();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getInputPath()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getOutputFolder()).contains(
                "TESTADDSURVEY");
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().getVaultPath()).isEqualTo(
                "testvault/testkey");
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getTrustParameters().isUseSignature()).isFalse();
    }

    @Test
    void addAdditionnalScheduleTest() {
        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(scheduleDocument.getLastExecution()).isNull();

        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleDedupTest()  {
        //Given 2
        ScheduleDocument scheduleDocumentTest = new ScheduleDocument(
                "TESTSURVEY",
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleDocumentTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        scheduleApiPortStub.mongoStub.add(scheduleDocumentTest);

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        ScheduleDocument scheduleDocument = mongoStubFiltered.getFirst();
        Assertions.assertThat(scheduleDocument.getLastExecution()).isNull();

        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(scheduleDocument.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }
    @Test
    void updateLastExecutionTest(){
        //When
        scheduleController.setSurveyLastExecution("TESTSURVEY", LocalDateTime.now());

        //Then
        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void setLastExecutionTestToNull(){
        //When
        scheduleController.setSurveyLastExecution("TESTSURVEY", null);

        //Then
        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNull();
    }

    @Test
    void setLastExecutionTest(){
        LocalDateTime date = LocalDateTime.now();
        //When
        scheduleController.setSurveyLastExecution("TESTSURVEY", date);

        //Then
        List<ScheduleDocument> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isEqualTo(date);
    }

    @Test
    void wrongFrequencyTest(){
        //When+Then
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "ERROR";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate,
                false, "", "", false);
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void notFoundTest(){
        //When+Then
        ResponseEntity<Object> response = scheduleController.setSurveyLastExecution("ERROR", LocalDateTime.now());
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void deleteScheduleTest(){
        //When
        scheduleController.deleteSchedule("TESTSURVEY");

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")
        ).isEmpty();
    }

    @Test
    void deleteExpiredScheduleTest_execution() throws NotFoundException, IOException {
        //Given
        ScheduleModel scheduleModel = new ScheduleModel(
                null,
                "TESTSURVEYADDED",
                null,
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        scheduleApiPortStub.mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(scheduleModel));

        //When
        scheduleController.deleteExpiredSchedules();

        //Then
        //Expired schedule deleted
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEYADDED")
        ).isNotEmpty();
        Assertions.assertThat(scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEYADDED")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2000").doesNotContain("5023");
    }

    @Test
    void deleteExpiredScheduleTest_wholeSurvey() throws NotFoundException, IOException {
        //Given
        ScheduleModel scheduleModel = new ScheduleModel(
                null,
                "TESTSURVEYADDED",
                null,
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2002, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2002, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        scheduleApiPortStub.mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(scheduleModel));

        //When
        scheduleController.deleteExpiredSchedules();

        //Then
        //Expired schedule document deleted
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEYADDED")
        ).isEmpty();

        //Expired schedule to log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED.json")
                .toFile()).exists().content().isNotEmpty().contains("2001","2002");
    }

    @Test
    void deleteExpiredScheduleTest_appendLog() throws NotFoundException, IOException {
        //Given
        ScheduleModel scheduleModel = new ScheduleModel(
                null,
                "TESTSURVEYADDED2",
                null,
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2000, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2000, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(5023, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        scheduleApiPortStub.mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(scheduleModel));

        //When
        scheduleController.deleteExpiredSchedules();
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2001, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2001, Month.DECEMBER, 1, 1, 1, 1),
                null
        );
        scheduleModel.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        scheduleApiPortStub.mongoStub.add(ScheduleDocumentMapper.INSTANCE.modelToDocument(scheduleModel));
        scheduleController.deleteExpiredSchedules();

        //Then
        //Expired schedules deleted
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEYADDED2")
        ).isNotEmpty();
        Assertions.assertThat(scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEYADDED2")).toList().getFirst().getKraftwerkExecutionScheduleList()
        ).isNotEmpty().hasSize(1);

        //Expired schedules to only one log json file
        Assertions.assertThat(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                .resolve(Constants.SCHEDULE_ARCHIVE_FOLDER_NAME)
                .resolve("TESTSURVEYADDED2.json")
                .toFile()).exists().content().isNotEmpty().contains("2000","2001");
    }
}
