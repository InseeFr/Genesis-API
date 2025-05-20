package fr.insee.genesis.infrastructure.document.schedule;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Constants.MONGODB_SCHEDULE_COLLECTION_NAME)
public class ScheduleDocument {

    public ScheduleDocument(String surveyName, List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList) {
        this.surveyName = surveyName;
        this.kraftwerkExecutionScheduleList = kraftwerkExecutionScheduleList;
    }
    @Id
    private ObjectId id;

    private String surveyName;

    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
}