package fr.insee.genesis.domain.model.rundeck;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Job {

    @JsonProperty("id")
    private String idJob;
    private long averageDuration;
    private String name;
    private String group;
    private String project;
    private String description;
    private String href;
    private String permalink;
}
