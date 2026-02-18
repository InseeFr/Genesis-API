package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

import java.util.List;
import java.util.Map;

public interface ContextualVariableApiPort {
    ContextualVariableModel getContextualVariable(String collectionInstrumentId, String interrogationId);
    Map<String, ContextualVariableModel> getContextualVariablesByList(String collectionInstrumentId, List<String> interrogationIds);
    int saveContextualVariableFiles(String collectionInstrumentId, FileUtils fileUtils, String contextualFolderPath) throws GenesisException;
}