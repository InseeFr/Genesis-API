package fr.insee.genesis.controller.dto.rawdata;

import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ScheduleV2Dto {
    private String surveyName;
    private String collectionInstrumentId;
    private LocalDateTime lastExecution;
    private KraftwerkExecutionScheduleV2 kraftwerkExecutionScheduleV2;
}
