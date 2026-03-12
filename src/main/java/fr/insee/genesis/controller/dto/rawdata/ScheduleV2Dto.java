package fr.insee.genesis.controller.dto.rawdata;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.context.schedule.DestinationType;
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
public class ScheduleV2Dto {
    private String collectionInstrumentId;
    private LocalDateTime lastExecution;

    private String frequency;
    private ExportType exportType;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleBeginDate;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleEndDate;

    private Mode mode;
    private DestinationType destinationType;
    private boolean useEncryption;
    private String encryptionVaultPath;
    private boolean useSignature;
    private boolean addStates;
    private String destinationFolder;
}
