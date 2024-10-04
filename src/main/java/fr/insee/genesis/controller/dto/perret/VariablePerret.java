package fr.insee.genesis.controller.dto.perret;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;
import lombok.Data;

import java.util.Map;

@Builder
@Data
public class VariablePerret {
    private String variableName;
    private Map<DataState, VariableStatePerret> variableStatePerretMap;
}
