package fr.insee.genesis.domain.model.context;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
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

    /** (Added to the class only to remove a warning) */
    @Id
    private ObjectId id;

    @Deprecated(forRemoval = true)
    private String partitionId;

    /** New name of legacy 'questionnaireId' property. */
    private String collectionInstrumentId;

    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;

    /** Determines if some review service must be called during the process. */
    boolean withReview;

    public ScheduleDto toScheduleDto(){
        return ScheduleDto.builder()
                .surveyName(partitionId)
                .collectionInstrumentId(collectionInstrumentId)
                .lastExecution(lastExecution)
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .build();
    }

}
