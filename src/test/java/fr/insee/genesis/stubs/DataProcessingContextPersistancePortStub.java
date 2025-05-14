package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;

@Getter
public class DataProcessingContextPersistancePortStub implements DataProcessingContextPersistancePort {
    List<DataProcessingContextDocument> mongoStub = new ArrayList<>();

    @Override
    public List<DataProcessingContextDocument> findAll(String partitionId) {
        return mongoStub.stream().filter(
                dataProcessingContextDocument -> dataProcessingContextDocument.getPartitionId().equals(partitionId)
        ).toList();
    }

    @Override
    public void save(DataProcessingContextDocument dataProcessingContextDocument) {
        if (
                mongoStub.stream().filter(
                        databaseDocument -> databaseDocument.getId().equals(dataProcessingContextDocument.getId())
                ).toList().isEmpty()
        ) {
            mongoStub.add(dataProcessingContextDocument);
        }
    }
}
