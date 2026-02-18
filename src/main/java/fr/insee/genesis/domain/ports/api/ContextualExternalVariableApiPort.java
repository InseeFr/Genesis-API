package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.List;
import java.util.Map;

public interface ContextualExternalVariableApiPort {
    boolean readContextualExternalFile(String collectionInstrumentId, String filePath) throws GenesisException;

    ContextualExternalVariableModel findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);

    Map<String, ContextualPreviousVariableModel> findByCollectionInstrumentIdAndInterrogationIdList(
            String collectionInstrumentId, List<String> interrogationIds
    );
}
