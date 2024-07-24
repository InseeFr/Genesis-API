package fr.insee.genesis.domain.service;

import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.StoredSurveySchedule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ScheduleUnicityServiceTest {

    private final String surveyName = "TEST";

    ScheduleUnicityService scheduleUnicityServiceToTest;

    @BeforeEach
    void clean() {
        scheduleUnicityServiceToTest = new ScheduleUnicityService();
    }

    @Test
    void emptyListTest() {
        //Given
        List<StoredSurveySchedule> storedSurveySchedules = new ArrayList<>();

        //When
        StoredSurveySchedule storedSurveySchedule = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, storedSurveySchedules);

        //Then
        Assertions.assertThat(storedSurveySchedule).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<StoredSurveySchedule> storedSurveySchedules = new ArrayList<>();
        StoredSurveySchedule surveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );
        storedSurveySchedules.add(surveySchedule);
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        false
                )
        );

        //When
        StoredSurveySchedule storedSurveySchedule = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, storedSurveySchedules);

        //Then
        Assertions.assertThat(storedSurveySchedule).isNotNull();
        Assertions.assertThat(storedSurveySchedule.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    void multipleElementsListTest() {
        //Given
        List<StoredSurveySchedule> storedSurveySchedules = new ArrayList<>();
        StoredSurveySchedule surveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        false
                )
        );
        storedSurveySchedules.add(surveySchedule);

        surveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        false
                )
        );
        storedSurveySchedules.add(surveySchedule);


        //When
        StoredSurveySchedule storedSurveySchedule = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, storedSurveySchedules);

        //Then
        Assertions.assertThat(storedSurveySchedule).isNotNull();
        Assertions.assertThat(storedSurveySchedule.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    void duplicateSheduleListTest() {
        //Given
        List<StoredSurveySchedule> storedSurveySchedules = new ArrayList<>();
        StoredSurveySchedule surveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        false
                )
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        false
                )
        );
        storedSurveySchedules.add(surveySchedule);

        surveySchedule = new StoredSurveySchedule(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        false
                )
        );
        storedSurveySchedules.add(surveySchedule);

        //When
        StoredSurveySchedule storedSurveySchedule = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, storedSurveySchedules);

        //Then
        Assertions.assertThat(storedSurveySchedule).isNotNull();
        Assertions.assertThat(storedSurveySchedule.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(storedSurveySchedule.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

}
