package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.controller.dto.rawdata.ScheduleV2Dto;
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
    private ObjectId id; //Used to remove warning

    @Deprecated(forRemoval = true)
    private String partitionId;

    private String collectionInstrumentId; //QuestionnaireId

    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;

    private KraftwerkExecutionScheduleV2 kraftwerkExecutionScheduleV2;

    boolean withReview;

    public ScheduleDto toScheduleDto(){
        return ScheduleDto.builder()
                .surveyName(partitionId)
                .collectionInstrumentId(collectionInstrumentId)
                .lastExecution(lastExecution)
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .build();
    }

    public ScheduleV2Dto toScheduleV2Dto() {
        String resolvedCollectionInstrumentId =
                collectionInstrumentId != null ? collectionInstrumentId : partitionId;

        return ScheduleV2Dto.builder()
                .collectionInstrumentId(resolvedCollectionInstrumentId)
                .lastExecution(lastExecution)
                .frequency(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getFrequency() : null)
                .exportType(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getExportType() : null)
                .scheduleBeginDate(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getScheduleBeginDate() : null)
                .scheduleEndDate(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getScheduleEndDate() : null)
                .mode(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getMode() : null)
                .useEncryption(kraftwerkExecutionScheduleV2 != null && kraftwerkExecutionScheduleV2.getTrustParameters() != null)
                .encryptionVaultPath(
                        kraftwerkExecutionScheduleV2 != null && kraftwerkExecutionScheduleV2.getTrustParameters() != null
                                ? kraftwerkExecutionScheduleV2.getTrustParameters().getVaultPath()
                                : ""
                )
                .useSignature(
                        kraftwerkExecutionScheduleV2 != null
                                && kraftwerkExecutionScheduleV2.getTrustParameters() != null
                                && kraftwerkExecutionScheduleV2.getTrustParameters().isUseSignature()
                )
                .addStates(kraftwerkExecutionScheduleV2 != null && kraftwerkExecutionScheduleV2.isAddStates())
                .destinationFolder(kraftwerkExecutionScheduleV2 != null ? kraftwerkExecutionScheduleV2.getDestinationFolder() : null)
                .build();
    }
}
