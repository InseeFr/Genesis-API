package fr.insee.genesis.domain.model.variabletype;

import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;

import java.util.Map;

@Builder
public record VariableTypeModel (
    String campaignId,
    String questionnaireId,
    Mode mode,
    Map<String, VariableType> variables
){}
