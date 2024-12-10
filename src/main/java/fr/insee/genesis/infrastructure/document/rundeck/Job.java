package fr.insee.genesis.infrastructure.document.rundeck;

import lombok.Data;

@Data
public class Job {

    private String idJob;
    private long averageDuration;
    private String name;
    private String project;
}
