package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.spi.RundeckExecutionPersistencePort;
import fr.insee.genesis.infrastructure.document.rundeck.RundeckExecutionDocument;
import fr.insee.genesis.infrastructure.mappers.RundeckExecutionDocumentMapper;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class RundeckExecutionPersistencePortStub implements RundeckExecutionPersistencePort {

    List<RundeckExecutionDocument> mongoStub = new ArrayList<>();

    @Override
    public void save(RundeckExecution rundeckExecution) {
        mongoStub.add(RundeckExecutionDocumentMapper.INSTANCE.modelToDocument(rundeckExecution));
    }
}
