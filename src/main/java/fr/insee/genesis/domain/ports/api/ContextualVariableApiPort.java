package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;

public interface ContextualVariableApiPort {
    ContextualVariableModel getContextualVariable(String questionnaireId, String interrogationId);
}