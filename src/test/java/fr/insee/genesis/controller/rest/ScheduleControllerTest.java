package fr.insee.genesis.controller.rest;


import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.stubs.ScheduleApiPortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

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
    void addSurveyTest() {
        //When
        String surveyName = "TESTADDSURVEY";
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);
        boolean useTrustEncryption = true;


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, useTrustEncryption);

        //Then
        Assertions.assertThat(scheduleApiPortStub.mongoStub).filteredOn(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)
        ).isNotEmpty();

        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals(surveyName)).toList();

        StoredSurveySchedule storedSurveySchedule = mongoStubFiltered.getFirst();

        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo(frequency);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList().getFirst().isUseTrustEncryption()).isTrue();
    }

    @Test
    void addScheduleTest() {
        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, false);

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
    void addScheduleDedupTest()  {
        //Given 2
        StoredSurveySchedule storedSurveyScheduleTest = new StoredSurveySchedule(
                "TESTSURVEY",
                new ArrayList<>()
        );
        KraftwerkExecutionSchedule kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.JANUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                false
        );
        storedSurveyScheduleTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);
        kraftwerkExecutionSchedule = new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.of(2023, Month.FEBRUARY, 1, 1, 1, 1),
                LocalDateTime.of(2023, Month.DECEMBER, 1, 1, 1, 1),
                false
        );
        storedSurveyScheduleTest.getKraftwerkExecutionScheduleList().add(kraftwerkExecutionSchedule);

        scheduleApiPortStub.mongoStub.add(storedSurveyScheduleTest);

        //When
        String surveyName = "TESTSURVEY"; //Already exists in stub
        ServiceToCall serviceToCall = ServiceToCall.MAIN;
        String frequency = "0 0 6 * * *";
        LocalDateTime scheduleBeginDate = LocalDateTime.now();
        LocalDateTime scheduleEndDate = LocalDateTime.now().plusMonths(1);


        scheduleController.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate, false);

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
        scheduleController.setSurveyLastExecution("TESTSURVEY", LocalDateTime.now());

        //Then
        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNotNull();
    }

    @Test
    void setLastExecutionTestToNull(){
        //When
        scheduleController.setSurveyLastExecution("TESTSURVEY", null);

        //Then
        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
                scheduleDocument.getSurveyName().equals("TESTSURVEY")).toList();
        Assertions.assertThat(mongoStubFiltered.getFirst().getLastExecution()).isNull();
    }

    @Test
    void setLastExecutionTest(){
        LocalDateTime date = LocalDateTime.now();
        //When
        scheduleController.setSurveyLastExecution("TESTSURVEY", date);

        //Then
        List<StoredSurveySchedule> mongoStubFiltered = scheduleApiPortStub.mongoStub.stream().filter(scheduleDocument ->
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

        ResponseEntity<Object> response = scheduleController.addSchedule(surveyName,serviceToCall,frequency,scheduleBeginDate,scheduleEndDate, false);
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
}
