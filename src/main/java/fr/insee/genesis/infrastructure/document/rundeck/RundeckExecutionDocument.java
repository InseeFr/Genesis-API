package fr.insee.genesis.infrastructure.document.rundeck;

import com.fasterxml.jackson.annotation.JsonProperty;
import fr.insee.genesis.domain.model.rundeck.DateStarted;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection= "rundeckExecutions")
public class RundeckExecutionDocument {

    private long idExecution;
    private String status;
    private String project;
    private String user;

    @JsonProperty("date-started")
    private DateStarted dateStarted;

    private Job job;
}
