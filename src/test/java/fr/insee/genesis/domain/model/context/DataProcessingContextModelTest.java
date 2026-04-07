package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class DataProcessingContextModelTest {
    @Test
    void toScheduleDto_test() {
        //GIVEN
        String collectionInstrumentId = "collectionInstrumentId";
        LocalDateTime lastExecution = LocalDateTime.now();
        List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList = new ArrayList<>();
        boolean withReview = true;


        DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .lastExecution(lastExecution)
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .withReview(withReview)
                .build();
        //WHEN
        ScheduleDto scheduleDto = dataProcessingContextModel.toScheduleDto();

        //THEN
        Assertions.assertThat(scheduleDto.collectionInstrumentId()).isEqualTo(collectionInstrumentId);
        Assertions.assertThat(scheduleDto.lastExecution()).isEqualTo(lastExecution);
        Assertions.assertThat(scheduleDto.kraftwerkExecutionScheduleList()).isEqualTo(kraftwerkExecutionScheduleList);
        Assertions.assertThat(scheduleDto.surveyName()).isNull();
    }
}