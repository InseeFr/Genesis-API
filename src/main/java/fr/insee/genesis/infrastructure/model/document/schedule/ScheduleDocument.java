package fr.insee.genesis.infrastructure.model.document.schedule;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class ScheduleDocument {

    public ScheduleDocument(String surveyName, List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList) {
        this.surveyName = surveyName;
        this.kraftwerkExecutionScheduleList = kraftwerkExecutionScheduleList;
    }

    @Id
    private ObjectId id;

    private String surveyName;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'hh:mm:ss")
    private LocalDateTime lastExecution;

    List<KraftwerkExecutionSchedule> kraftwerkExecutionScheduleList;
}