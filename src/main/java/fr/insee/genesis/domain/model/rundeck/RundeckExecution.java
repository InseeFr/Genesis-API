package fr.insee.genesis.domain.model.rundeck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RundeckExecution {

    private long id;
    private String href;
    private String permalink;
    private String status;
    private String project;
    private String executionType;
    private String user;

    @JsonProperty("date-started")
    private DateStarted dateStarted;

    private Job job;
    private String description;
    private String argstring;
    private String serverUUID;
}
