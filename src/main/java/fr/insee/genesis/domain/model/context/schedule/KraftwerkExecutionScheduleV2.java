package fr.insee.genesis.domain.model.context.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KraftwerkExecutionScheduleV2 {

    private String frequency;
    private ExportType exportType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleBeginDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleEndDate;

    private Mode mode;
    private boolean addStates;
    private String destinationFolder;
    private TrustParameters trustParameters;
}
