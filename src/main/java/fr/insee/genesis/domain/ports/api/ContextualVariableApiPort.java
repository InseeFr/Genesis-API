package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

public interface ContextualVariableApiPort {
    ContextualVariableModel getContextualVariable(String questionnaireId, String interrogationId);
    int saveContextualVariableFiles(String questionnaireId, FileUtils fileUtils,String contextualFolderPath) throws GenesisException;
}