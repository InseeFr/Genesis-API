package fr.insee.genesis.domain.model.variabletype;

import fr.insee.bpm.metadata.model.VariableType;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Data
@Builder
public class VariableTypeModel {
    String campaignId;
    Map<String, VariableType> variableTypeMap;
}
