package fr.insee.genesis.domain.service.rundeck;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.api.RundeckExecutionApiPort;
import fr.insee.genesis.domain.ports.spi.RundeckExecutionPersistencePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class RundeckExecutionService implements RundeckExecutionApiPort {
    @Qualifier("rundeckExecutionMongoAdapter")
    private final RundeckExecutionPersistencePort rundeckExecutionPersistencePort;

    @Autowired
    public RundeckExecutionService(RundeckExecutionPersistencePort rundeckExecutionPersistencePort) {
        this.rundeckExecutionPersistencePort = rundeckExecutionPersistencePort;
    }

    @Override
    public void addExecution(RundeckExecution rundeckExecution) {
        rundeckExecutionPersistencePort.save(rundeckExecution);
    }
}
