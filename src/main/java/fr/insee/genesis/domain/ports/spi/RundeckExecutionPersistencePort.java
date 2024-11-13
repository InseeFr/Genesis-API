package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;

public interface RundeckExecutionPersistencePort {

    void save(RundeckExecution rundeckExecution);
}
