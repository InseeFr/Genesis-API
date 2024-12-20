package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;

public interface VariableTypeApiPort {
    void saveMetadatas(String campaignId, VariablesMap variablesMap);
}
