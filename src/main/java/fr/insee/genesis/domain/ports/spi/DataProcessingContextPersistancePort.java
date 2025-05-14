package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;

import java.util.List;

public interface DataProcessingContextPersistancePort {
    List<DataProcessingContextDocument> findAll(String partitionId);

    void save(DataProcessingContextDocument dataProcessingContextDocument);
}
