package fr.insee.genesis.domain.model.variabletype;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;

@Builder
public record VariableTypeModel (
    String campaignId,
    String questionnaireId,
    Mode mode,
    VariablesMap variablesMap
){}
