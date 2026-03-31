package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.controller.validation.schedule.ValidScheduleRequest;
import fr.insee.genesis.domain.model.context.schedule.DestinationType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Request used to schedule a Kraftwerk export workflow")
@ValidScheduleRequest
public class ScheduleRequestDto {

    @NotBlank
    @Schema(description = "Collection instrument to call Kraftwerk on", example = "EAP2026A00", requiredMode = Schema.RequiredMode.REQUIRED)
    private String collectionInstrumentId;

    @NotNull
    @Schema(description = "Export type", allowableValues = {"JSON", "CSV_PARQUET"}, requiredMode = Schema.RequiredMode.REQUIRED)
    private ExportType exportType;

    @NotBlank
    @Schema(description = "Frequency in Spring cron format (6 inputs). Example: 0 0 6 * * *", example = "0 0 6 * * *", requiredMode = Schema.RequiredMode.REQUIRED)
    private String frequency;

    @NotNull
    @Schema(description = "Schedule effective date and time", example = "2024-01-01T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleBeginDate;

    @NotNull
    @Schema(description = "Schedule end date and time", example = "2024-01-01T12:00:00", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime scheduleEndDate;

    private Mode mode;

    @Schema(defaultValue = "APPLISHARE")
    private DestinationType destinationType = DestinationType.APPLISHARE;

    @Schema(defaultValue = "false")
    private boolean useSymmetricEncryption = false;

    @Schema(defaultValue = "false")
    private boolean useAsymmetricEncryption = false;

    @Schema(description = "Encryption vault path")
    private String encryptionVaultPath = "";

    @Schema(defaultValue = "false")
    private boolean useSignature = false;

    @Schema(description = "Add variable states to export", example = "false", defaultValue = "false")
    private boolean addStates = false;

    @NotBlank
    @Schema(description = "Destination folder (Applishare or S3)", requiredMode = Schema.RequiredMode.REQUIRED)
    private String destinationFolder;

    @Schema(description = "Batch size", defaultValue = "100")
    private Integer batchSize;
}