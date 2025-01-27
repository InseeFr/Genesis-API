package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;

public interface RundeckExecutionApiPort {

    void addExecution(RundeckExecution rundeckExecution);

}
