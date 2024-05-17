package fr.insee.genesis.infrastructure.model.document.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
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
}
