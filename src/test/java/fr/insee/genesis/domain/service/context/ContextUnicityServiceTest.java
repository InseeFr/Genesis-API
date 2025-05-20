package fr.insee.genesis.domain.service.context;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class ContextUnicityServiceTest {

    private final String partitionId = "TEST";

    ContextUnicityService contextUnicityServiceToTest;

    @BeforeEach
    void clean() {
        contextUnicityServiceToTest = new ContextUnicityService();
    }

    @Test
    void emptyListTest() {
        //Given
        List<DataProcessingContextModel> dataProcessingContextModels = new ArrayList<>();

        //When
        DataProcessingContextModel dataProcessingContextModel = contextUnicityServiceToTest.deduplicateContexts(partitionId, dataProcessingContextModels);

        //Then
        Assertions.assertThat(dataProcessingContextModel).isNull();
    }

    @Test
    void oneElementListTest() {
        //Given
        List<DataProcessingContextModel> dataProcessingContextModels = new ArrayList<>();
        DataProcessingContextModel surveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
                .kraftwerkExecutionScheduleList(new ArrayList<>())
                .build();
        dataProcessingContextModels.add(surveySchedule);
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
        DataProcessingContextModel dataProcessingContextModel = contextUnicityServiceToTest.deduplicateContexts(partitionId, dataProcessingContextModels);

        //Then
        Assertions.assertThat(dataProcessingContextModel).isNotNull();
        Assertions.assertThat(dataProcessingContextModel.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextModel.getKraftwerkExecutionScheduleList()).isNotEmpty();
    }

    @Test
    void multipleElementsListTest() {
        //Given
        List<DataProcessingContextModel> dataProcessingContextModels = new ArrayList<>();
        DataProcessingContextModel surveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
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
        dataProcessingContextModels.add(surveySchedule);

        surveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
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
        dataProcessingContextModels.add(surveySchedule);


        //When
        DataProcessingContextModel dataProcessingContextModel = contextUnicityServiceToTest.deduplicateContexts(partitionId, dataProcessingContextModels);

        //Then
        Assertions.assertThat(dataProcessingContextModel).isNotNull();
        Assertions.assertThat(dataProcessingContextModel.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextModel.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

    @Test
    void duplicateSheduleListTest() {
        //Given
        List<DataProcessingContextModel> dataProcessingContextModels = new ArrayList<>();
        DataProcessingContextModel surveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
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
        dataProcessingContextModels.add(surveySchedule);

        surveySchedule = DataProcessingContextModel.builder()
                .partitionId(partitionId)
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
        dataProcessingContextModels.add(surveySchedule);

        //When
        DataProcessingContextModel dataProcessingContextModel = contextUnicityServiceToTest.deduplicateContexts(partitionId, dataProcessingContextModels);

        //Then
        Assertions.assertThat(dataProcessingContextModel).isNotNull();
        Assertions.assertThat(dataProcessingContextModel.getPartitionId()).isEqualTo(partitionId);
        Assertions.assertThat(dataProcessingContextModel.getKraftwerkExecutionScheduleList()).isNotEmpty().hasSize(2);
    }

}
