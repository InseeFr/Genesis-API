package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyUnitInputDto {
    private String questionnaireId;
    private String interrogationId;
    private Mode mode;
    private List<VariableInputDto> collectedVariables;
}
