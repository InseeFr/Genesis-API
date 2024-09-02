package fr.insee.genesis.domain.model.schedule;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KraftwerkExecutionSchedule {
    private String frequency;

    private ServiceToCall serviceToCall;

    private LocalDateTime scheduleBeginDate;
    private LocalDateTime scheduleEndDate;

    private TrustParameters trustParameters;
}
