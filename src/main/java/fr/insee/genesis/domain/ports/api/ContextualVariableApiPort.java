package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.SaveContextualVariablesReportDto;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;

public interface ContextualVariableApiPort {
    ContextualVariableModel getContextualVariable(String collectionInstrumentId, String interrogationId);
    int saveContextualVariableFiles(String collectionInstrumentId, FileUtils fileUtils, String contextualFolderPath) throws GenesisException;
    SaveContextualVariablesReportDto saveContextualVariableFilesWithReport(
            String collectionInstrumentId,
            FileUtils fileUtils,
            String contextualFolderPath
    ) throws GenesisException;
}