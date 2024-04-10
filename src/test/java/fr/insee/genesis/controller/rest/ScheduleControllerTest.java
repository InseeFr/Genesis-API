package fr.insee.genesis.controller.rest;

import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.stubs.ScheduleApiPortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
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
}
