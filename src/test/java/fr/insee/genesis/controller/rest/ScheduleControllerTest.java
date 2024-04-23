package fr.insee.genesis.controller.rest;

import cucumber.TestConstants;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.stubs.ScheduleApiPortStub;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class ScheduleControllerTest {
    //Given
    private static ScheduleController scheduleController;

    private static ScheduleApiPortStub scheduleApiPortStub;

    @BeforeEach
    void clean() {
        scheduleApiPortStub = new ScheduleApiPortStub();
        scheduleController = new ScheduleController(scheduleApiPortStub);
    }

    @Test
    void getAllSchedulesTest() {
        //When
        ResponseEntity<Object> response = scheduleController.getAllSchedules();

        //Then
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Test
    void addSurveyTest() throws InvalidCronExpressionException {
        //When
        String surveyName = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty();

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleTest() throws InvalidCronExpressionException {
        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
        Assertions.assertThat(storedSurveySchedule.getLastExecution()).isNull();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }

    @Test
    void addScheduleDedupTest() throws InvalidCronExpressionException {
        //Given 2
        StoredSurveySchedule storedSurveyScheduleTest = new StoredSurveySchedule(
                "TESTSURVEY",
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1)
        );
        storedSurveyScheduleTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1)
        );
        storedSurveyScheduleTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        scheduleApiPortStub.mongoStub.add(storedSurveyScheduleTest);

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
        Assertions.assertThat(storedSurveySchedule.getLastExecution()).isNull();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
    }
    @Test
    void updateLastExecutionTest(){
        //When
        scheduleController.updateSurveyLastExecution("TESTSURVEY");

        //Then
        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void wrongFrequencyTest(){
        //When+Then
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "ERROR";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);

        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName,serviceToCall,frequency,scheduleBeginDate,scheduleEndDate);
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void notFoundTest(){
        //When+Then
        ResponseEntity<Object> response = scheduleController.updateSurveyLastExecution("ERROR");
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    // Schedule with cipher OK tests
    @Test
    void addScheduleWithCipherTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        String cipherInputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTKWK/TESTSURVEY";
        String cipherOutputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTCIPHER/TESTSURVEY";


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, cipherInputPathString, cipherOutputPathString);

        //Then
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error(response.toString());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
        Assertions.assertThat(storedSurveySchedule.getLastExecution()).isNull();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherInputPath()).isEqualTo(cipherInputPathString);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherOutputPath()).isEqualTo(cipherOutputPathString);
    }

    @Test
    void addScheduleWithCipherFileTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        String cipherInputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTKWK/TESTSURVEY";
        String cipherOutputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTCIPHER/TESTSURVEY";


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, cipherInputPathString, cipherOutputPathString);

        //Then
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error(response.toString());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
        Assertions.assertThat(storedSurveySchedule.getLastExecution()).isNull();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherInputPath()).isEqualTo(cipherInputPathString);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherOutputPath()).isEqualTo(cipherOutputPathString);
    }

    @Test
    void addScheduleWithCipherWithoutOutputPathTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        String cipherInputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTKWK/TESTSURVEY";


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, cipherInputPathString, null);

        //Then
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error(response.toString());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty().hasSize(1);

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();
        Assertions.assertThat(storedSurveySchedule.getLastExecution()).isNull();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherInputPath()).isEqualTo(cipherInputPathString);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getCipherOutputPath()).isNull();
    }

    //Cipher input path error tests
    @Test
    void noInputPathTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, null, null);

        //Then
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    @Test
    void wrongInputPathTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        String cipherInputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTKWK/TESTSURVEY/wrong.csv";
        String cipherOutputPathString = TestConstants.TEST_RESOURCES_DIRECTORY + "/OUTCIPHER/TESTSURVEY";


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, cipherInputPathString, cipherOutputPathString);

        //Then
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }

    //Cipher output path error test
    @Test
    void wrongOutputPathTest(){
        //Given
        scheduleApiPortStub.mongoStub.clear();

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        Path cipherInputPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"OUTKWK/TESTSURVEY/test.csv");
        Path cipherOutputPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"OUTCIPHER/TESTSURVEY/test.enc");


        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, cipherInputPath.toString(), cipherOutputPath.toString());

        //Then
        Assertions.assertThat(response.getStatusCode().is4xxClientError()).isTrue();
    }
}
