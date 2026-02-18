package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.List;
import java.util.Map;

public interface ContextualPreviousVariableApiPort {
    boolean readContextualPreviousFile(String collectionInstrumentId, String sourceState, String filePath) throws GenesisException;

    ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
    Map<String, ContextualPreviousVariableModel> findByCollectionInstrumentIdAndInterrogationIdList(
            String collectionInstrumentId, List<String> interrogationId
    );
}
