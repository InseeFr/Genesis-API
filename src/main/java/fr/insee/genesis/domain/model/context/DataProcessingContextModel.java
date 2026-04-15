package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DataProcessingContextModel {
    @Id
    private ObjectId id; // Used to remove warning

    @Deprecated(forRemoval = true)
    private String partitionId;

    private String collectionInstrumentId; // QuestionnaireId

    private LocalDateTime lastExecution;

    private List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;

    private List<KraftwerkExecutionScheduleV2> kraftwerkExecutionScheduleV2List;

    private boolean withReview;

    public ScheduleDto toScheduleDto() {
        return ScheduleDto.builder()
                .surveyName(partitionId)
                .collectionInstrumentId(collectionInstrumentId)
                .lastExecution(lastExecution)
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .build();
    }

    public List<ScheduleResponseDto> toScheduleResponseDtos() {
        if (kraftwerkExecutionScheduleV2List == null || kraftwerkExecutionScheduleV2List.isEmpty()) {
            return List.of();
        }

        return kraftwerkExecutionScheduleV2List.stream()
                .filter(schedule -> schedule != null && schedule.getScheduleUuid() != null)
                .map(schedule -> ScheduleResponseDto.builder()
                        .scheduleUuid(schedule.getScheduleUuid())
                        .collectionInstrumentId(getResolvedCollectionInstrumentId())
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

    public String getResolvedCollectionInstrumentId() {
        return collectionInstrumentId != null && !collectionInstrumentId.isBlank()
                ? collectionInstrumentId
                : partitionId;
    }
}
