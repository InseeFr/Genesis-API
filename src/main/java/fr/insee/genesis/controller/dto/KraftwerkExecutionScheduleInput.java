package fr.insee.genesis.controller.dto;

import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.context.schedule.DestinationType;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KraftwerkExecutionScheduleInput {
    private String collectionInstrumentId;
    private String scheduleUuid;
    private ExportType exportType;
    private String frequency;
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    private Mode mode;
    private DestinationType destinationType;
    private boolean addStates;
    private String destinationFolder;
    private TrustParameters trustParameters;
    private Integer batchSize;
}
