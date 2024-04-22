package fr.insee.genesis.infrastructure.model.document.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.nio.file.Path;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
public class KraftwerkExecutionSchedule {

    public KraftwerkExecutionSchedule(String frequency, ServiceToCall serviceToCall, LocalDateTime scheduleBeginDate, LocalDateTime scheduleEndDate) {
        this.frequency = frequency;
        this.serviceToCall = serviceToCall;
        this.scheduleBeginDate = scheduleBeginDate;
        this.scheduleEndDate = scheduleEndDate;
    }

    private String frequency;

    private ServiceToCall serviceToCall;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private LocalDateTime scheduleBeginDate;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private LocalDateTime scheduleEndDate;

    private Path cipherInputPath;
    private Path cipherOutputPath;
}
