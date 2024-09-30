package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.schedule.ScheduleService;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.document.schedule.ScheduleDocument;
import fr.insee.genesis.stubs.SchedulePersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ScheduleServiceTest {
    //Given
    static ScheduleService scheduleService;
    static SchedulePersistencePortStub schedulePersistencePortStub;

    @BeforeAll
    static void init(){
        schedulePersistencePortStub = new SchedulePersistencePortStub();

        scheduleService = new ScheduleService(schedulePersistencePortStub);
    }

    @BeforeEach
    void reset(){
        schedulePersistencePortStub.getMongoStub().clear();

        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        ));
        schedulePersistencePortStub.getMongoStub().add(new ScheduleDocument(
                "TEST",
                kraftwerkExecutionScheduleList
        ));
    }

    @Test
    void getAllSchedules_test(){
        //When + Then
        Assertions.assertThat(scheduleService.getAllSchedules()).hasSize(1);
    }

    @Test
    void addSchedule_test_new_survey() throws InvalidCronExpressionException {
        //When
        scheduleService.addSchedule("TEST2",
                ServiceToCall.GENESIS,
                "0 0 6 * * *",
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        );

        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getSurveyName()).isEqualTo("TEST2");
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getLastExecution()).isNull();

        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getFrequency()).isEqualTo("0 0 6 * * *");
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getScheduleBeginDate()).isEqualTo(LocalDateTime.MIN);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getScheduleEndDate()).isEqualTo(LocalDateTime.MAX);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getServiceToCall()).isEqualTo(ServiceToCall.GENESIS);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getTrustParameters()).isNull();
    }

    @Test
    void addSchedule_test_old_survey() throws InvalidCronExpressionException {
        //When
        scheduleService.addSchedule("TEST",
                ServiceToCall.GENESIS,
                "0 0 0 6 * *",
                LocalDateTime.MIN,
                LocalDateTime.MAX,
                null
        );

        //Then
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getSurveyName()).isEqualTo("TEST");
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getLastExecution()).isNull();

        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList()).hasSize(2);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getFrequency()).isEqualTo("0 0 0 6 * *");
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getScheduleBeginDate()).isEqualTo(LocalDateTime.MIN);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getScheduleEndDate()).isEqualTo(LocalDateTime.MAX);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getServiceToCall()).isEqualTo(ServiceToCall.GENESIS);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().get(1).getTrustParameters()).isNull();
    }

    @Test
    void deleteSchedule_test() throws NotFoundException {
        //When
        scheduleService.deleteSchedule("TEST");

        //Then
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).isEmpty();
    }

    @Test
    void updateLastExecutionName_test() throws NotFoundException {
        //Given
        LocalDateTime localDateTime = LocalDateTime.now();

        //When
        scheduleService.updateLastExecutionName("TEST", localDateTime);

        //Then
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getLastExecution()).isNotNull().isEqualTo(localDateTime);
    }

    @Test
    void countSchedules_test(){
        //When + Then
        Assertions.assertThat(scheduleService.countSchedules()).isEqualTo(1);
    }

    @Test
    void removeExpiredSchedules_test_existing_schedule() throws NotFoundException {
        //Given
        //Expired schedule
        schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.of(2000,1,1,1,1,1),
                null
        ));

        //When
        scheduleService.deleteExpiredSchedules("TEST");

        //Then
        //Execution schedule deleted
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getKraftwerkExecutionScheduleList().getFirst().getScheduleEndDate())
                .isEqualTo(LocalDateTime.MAX);
    }
    @Test
    void removeExpiredSchedules_test_delete_document() throws NotFoundException {
        //Given
        //Expired schedule + new survey
        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        kraftwerkExecutionScheduleList.add(new KraftwerkExecutionSchedule(
                "0 0 6 * * *",
                ServiceToCall.MAIN,
                LocalDateTime.MIN,
                LocalDateTime.of(2000,1,1,1,1,1),
                null
        ));
        schedulePersistencePortStub.getMongoStub().add(new ScheduleDocument(
                "TEST2",
                kraftwerkExecutionScheduleList
        ));

        //When
        scheduleService.deleteExpiredSchedules("TEST2");

        //Then
        //Survey schedule document deleted
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().stream().filter(
                scheduleDocument -> scheduleDocument.getSurveyName().equals("TEST2")
        ).toList()).isEmpty();

    }
}
