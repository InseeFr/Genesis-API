package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.service.schedule.ScheduleService;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import fr.insee.genesis.infrastructure.model.document.schedule.SurveyScheduleDocument;
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
        schedulePersistencePortStub.getMongoStub().add(new SurveyScheduleDocument(
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
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().get(1).getKraftwerkExecutionScheduleList().getFirst().getServiceToCall().equals(ServiceToCall.GENESIS)).isTrue();
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
        Assertions.assertThat(schedulePersistencePortStub.getMongoStub().getFirst().getLastExecution()).isNotNull().isEqualTo(localDateTime);
    }

    @Test
    void countSchedules_test(){
        //When + Then
        Assertions.assertThat(scheduleService.countSchedules()).isEqualTo(1);
    }

}
