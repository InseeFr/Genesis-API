package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

public interface ContextualExternalVariableApiPort {
    boolean readContextualExternalFile(String questionnaireId, String filePath) throws GenesisException;

    ContextualExternalVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}
