package fr.insee.genesis.controller.dto.perret;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitPerret {
    private String surveyUnitId;
    private List<VariablePerret> collectedVariables;
    private List<VariablePerret> externalVariables;
}
