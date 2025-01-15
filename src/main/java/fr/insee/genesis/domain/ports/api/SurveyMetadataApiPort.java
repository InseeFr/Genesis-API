package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;

import java.util.Set;

public interface SurveyMetadataApiPort {
    void saveMetadatas(String campaignId, String questionnaireId, Mode mode, VariablesMap variablesMap);
    SurveyMetadataModel getMetadatas(String campaignId, String questionnaireId, Mode mode);

    VariablesMap getVariableMapFromMetadatas(String campaignId, String questionnaireId, Mode mode);
}
