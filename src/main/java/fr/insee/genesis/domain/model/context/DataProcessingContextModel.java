package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.controller.utils.ExportType;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionScheduleV2;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingContextModel {
    @Id
    private ObjectId id; // Used to remove warning

    private String collectionInstrumentId; //QuestionnaireId

    private LocalDateTime lastExecution;

    private List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;

    private List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List;

    private boolean withReview;

    public List<ScheduleResponseDto> toScheduleV1ResponseDtos() {
        if (kraftwerkExecutionScheduleList == null || kraftwerkExecutionScheduleList.isEmpty()) {
            return List.of();
        }

        return kraftwerkExecutionScheduleList.stream()
                .filter(Objects::nonNull)
                .map(schedule -> ScheduleResponseDto.builder()
                        .collectionInstrumentId(collectionInstrumentId)
                        .lastExecution(lastExecution)
                        .frequency(schedule.getFrequency())
                        .scheduleBeginDate(schedule.getScheduleBeginDate())
                        .scheduleEndDate(schedule.getScheduleEndDate())
                        .exportType(ExportType.CSV_PARQUET)
                        .useSymmetricEncryption(false)
                        .useAsymmetricEncryption(schedule.getTrustParameters() != null)
                        .encryptionVaultPath(
                                schedule.getTrustParameters() != null
                                        ? schedule.getTrustParameters().getVaultPath()
                                        : null
                        )
                        .useSignature(
                                schedule.getTrustParameters() != null
                                        && schedule.getTrustParameters().isUseSignature()
                        )
                        .build()
                )
                .toList();
    }

    public List<ScheduleResponseDto> toScheduleV2ResponseDtos() {
        if (kraftwerkExecutionScheduleV2List == null || kraftwerkExecutionScheduleV2List.isEmpty()) {
            return List.of();
        }

        return kraftwerkExecutionScheduleV2List.stream()
                .filter(schedule -> schedule != null && schedule.getScheduleUuid() != null)
                .map(schedule -> ScheduleResponseDto.builder()
                        .scheduleUuid(schedule.getScheduleUuid())
                        .collectionInstrumentId(collectionInstrumentId)
                        .lastExecution(lastExecution)
                        .frequency(schedule.getFrequency())
                        .exportType(schedule.getExportType())
                        .scheduleBeginDate(schedule.getScheduleBeginDate())
                        .scheduleEndDate(schedule.getScheduleEndDate())
                        .mode(schedule.getMode())
                        .useSymmetricEncryption(schedule.isUseSymmetricEncryption())
                        .useAsymmetricEncryption(schedule.getTrustParameters() != null)
                        .encryptionVaultPath(
                                schedule.getTrustParameters() != null
                                        ? schedule.getTrustParameters().getVaultPath()
                                        : ""
                        )
                        .useSignature(
                                schedule.getTrustParameters() != null
                                        && schedule.getTrustParameters().isUseSignature()
                        )
                        .addStates(schedule.isAddStates())
                        .destinationType(schedule.getDestinationType())
                        .destinationFolder(schedule.getDestinationFolder())
                        .batchSize(schedule.getBatchSize())
                        .build()
                )
                .toList();
    }
}
