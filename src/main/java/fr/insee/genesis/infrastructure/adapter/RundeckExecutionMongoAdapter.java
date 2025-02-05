package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.spi.RundeckExecutionPersistencePort;
import fr.insee.genesis.infrastructure.mappers.RundeckExecutionDocumentMapper;
import fr.insee.genesis.infrastructure.repository.RundeckExecutionDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
@Qualifier("rundeckExecutionMongoAdapter")
@Slf4j
public class RundeckExecutionMongoAdapter implements RundeckExecutionPersistencePort {

    private final RundeckExecutionDBRepository rundeckExecutionDBRepository;

    @Autowired
    public RundeckExecutionMongoAdapter(RundeckExecutionDBRepository rundeckExecutionDBRepository) {
        this.rundeckExecutionDBRepository = rundeckExecutionDBRepository;
    }

    @Override
    public void save(RundeckExecution rundeckExecution) {
        rundeckExecutionDBRepository.insert(RundeckExecutionDocumentMapper.INSTANCE.modelToDocument(rundeckExecution));
    }
}
