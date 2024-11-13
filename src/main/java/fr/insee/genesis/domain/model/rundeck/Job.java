package fr.insee.genesis.domain.model.rundeck;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Job {

    private String id;
    private long averageDuration;
    private String name;
    private String group;
    private String project;
    private String description;
    private String href;
    private String permalink;
}
