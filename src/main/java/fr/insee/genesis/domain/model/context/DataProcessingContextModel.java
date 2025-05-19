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
    @Id
    private ObjectId id; //Used to remove warning

    private String partitionId;

    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;

    boolean withReview;

    public ScheduleDto toScheduleDto(){
        return ScheduleDto.builder()
                .surveyName(partitionId)
                .lastExecution(lastExecution)
                .kraftwerkExecutionScheduleList(kraftwerkExecutionScheduleList)
                .build();
    }
}
