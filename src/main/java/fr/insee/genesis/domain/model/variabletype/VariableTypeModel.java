package fr.insee.genesis.domain.model.variabletype;

import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VariableTypeModel {
    private String campaignId;
    private String questionnaireId;
    private Mode mode;
    private Map<String, VariableType> variables;
}
