package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.io.InputStream;

public interface ContextualPreviousVariableApiPort {
    boolean readContextualPreviousFile(String questionnaireId, String sourceState, String filePath) throws GenesisException;

    ContextualPreviousVariableModel findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}
