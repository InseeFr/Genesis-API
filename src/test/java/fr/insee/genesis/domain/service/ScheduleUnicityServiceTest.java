package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.service.schedule.ScheduleUnicityService;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
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
        List<SurveyScheduleDocument> surveyScheduleDocuments = new ArrayList<>();

        //When
        SurveyScheduleDocument surveyScheduleDocument = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, surveyScheduleDocuments);

        //Then
        Assertions.assertThat(surveyScheduleDocument).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<SurveyScheduleDocument> surveyScheduleDocuments = new ArrayList<>();
        SurveyScheduleDocument surveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );
        surveyScheduleDocuments.add(surveySchedule);
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );

        //When
        SurveyScheduleDocument surveyScheduleDocument = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, surveyScheduleDocuments);

        //Then
        Assertions.assertThat(surveyScheduleDocument).isNotNull();
        Assertions.assertThat(surveyScheduleDocument.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(surveyScheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    void multipleElementsListTest() {
        //Given
        List<SurveyScheduleDocument> surveyScheduleDocuments = new ArrayList<>();
        SurveyScheduleDocument surveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        surveyScheduleDocuments.add(surveySchedule);

        surveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        surveyScheduleDocuments.add(surveySchedule);


        //When
        SurveyScheduleDocument surveyScheduleDocument = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, surveyScheduleDocuments);

        //Then
        Assertions.assertThat(surveyScheduleDocument).isNotNull();
        Assertions.assertThat(surveyScheduleDocument.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(surveyScheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    void duplicateSheduleListTest() {
        //Given
        List<SurveyScheduleDocument> surveyScheduleDocuments = new ArrayList<>();
        SurveyScheduleDocument surveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        surveyScheduleDocuments.add(surveySchedule);

        surveySchedule = new SurveyScheduleDocument(
                surveyName,
                new ArrayList<>()
        );
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        surveyScheduleDocuments.add(surveySchedule);

        //When
        SurveyScheduleDocument surveyScheduleDocument = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, surveyScheduleDocuments);

        //Then
        Assertions.assertThat(surveyScheduleDocument).isNotNull();
        Assertions.assertThat(surveyScheduleDocument.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(surveyScheduleDocument.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

}
