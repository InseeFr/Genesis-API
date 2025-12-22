package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

public interface ContextualPreviousVariableApiPort {
    boolean readContextualPreviousFile(String collectionInstrumentId, String sourceState, String filePath) throws GenesisException;

    ContextualPreviousVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
}
