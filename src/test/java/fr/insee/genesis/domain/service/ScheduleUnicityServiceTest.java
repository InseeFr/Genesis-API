package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.schedule.ScheduleUnicityService;
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
        List<ScheduleModel> scheduleModels = new ArrayList<>();

        //When
        ScheduleModel scheduleModel = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, scheduleModels);

        //Then
        Assertions.assertThat(scheduleModel).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<ScheduleModel> scheduleModels = new ArrayList<>();
        ScheduleModel surveySchedule = ScheduleModel.builder()
                .surveyName(surveyName)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
        scheduleModels.add(surveySchedule);
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
        ScheduleModel scheduleModel = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, scheduleModels);

        //Then
        Assertions.assertThat(scheduleModel).isNotNull();
        Assertions.assertThat(scheduleModel.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(scheduleModel.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    void multipleElementsListTest() {
        //Given
        List<ScheduleModel> scheduleModels = new ArrayList<>();
        ScheduleModel surveySchedule = ScheduleModel.builder()
                .surveyName(surveyName)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        scheduleModels.add(surveySchedule);

        surveySchedule = ScheduleModel.builder()
                .surveyName(surveyName)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 6 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.now(),
                        LocalDateTime.MAX,
                        null
                )
        );
        scheduleModels.add(surveySchedule);


        //When
        ScheduleModel scheduleModel = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, scheduleModels);

        //Then
        Assertions.assertThat(scheduleModel).isNotNull();
        Assertions.assertThat(scheduleModel.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(scheduleModel.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    void duplicateSheduleListTest() {
        //Given
        List<ScheduleModel> scheduleModels = new ArrayList<>();
        ScheduleModel surveySchedule = ScheduleModel.builder()
                .surveyName(surveyName)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
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
        scheduleModels.add(surveySchedule);

        surveySchedule = ScheduleModel.builder()
                .surveyName(surveyName)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
        surveySchedule.getKraftwerkExecutionScheduleList().add(
                new KraftwerkExecutionSchedule(
                        "0 0 0 * * *",
                        ServiceToCall.MAIN,
                        LocalDateTime.MIN,
                        LocalDateTime.MAX,
                        null
                )
        );
        scheduleModels.add(surveySchedule);

        //When
        ScheduleModel scheduleModel = scheduleUnicityServiceToTest.deduplicateSurveySchedules(surveyName, scheduleModels);

        //Then
        Assertions.assertThat(scheduleModel).isNotNull();
        Assertions.assertThat(scheduleModel.getSurveyName()).isEqualTo(surveyName);
        Assertions.assertThat(scheduleModel.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

}
