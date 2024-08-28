package fr.insee.genesis.infrastructure.model.document.schedule;

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
@Document(collection = "schedules")
public class SurveyScheduleDocument {

    public SurveyScheduleDocument(String surveyName, List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList) {
        this.surveyName = surveyName;
        this.kraftwerkExecutionScheduleList = kraftwerkExecutionScheduleList;
    }

    @Id
    private ObjectId id;

    private String surveyName;

    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
}