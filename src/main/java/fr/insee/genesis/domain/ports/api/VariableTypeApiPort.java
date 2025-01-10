package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;

public interface VariableTypeApiPort {
    void saveMetadatas(String campaignId, String questionnaireId, Mode mode, VariablesMap variablesMap);
    VariableTypeModel getMetadatas(String campaignId, String questionnaireId, Mode mode);

}
