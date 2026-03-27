package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitInputDto {
    private String questionnaireId;
    private String interrogationId;
    private Mode mode;
    private List<VariableInputDto> collectedVariables;
}
