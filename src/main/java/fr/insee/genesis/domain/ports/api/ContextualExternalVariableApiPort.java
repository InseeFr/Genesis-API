package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

public interface ContextualExternalVariableApiPort {
    boolean readContextualExternalFile(String collectionInstrumentId, String filePath) throws GenesisException;

    ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
}
