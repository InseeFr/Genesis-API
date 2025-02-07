package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitInputDto {
    private String surveyUnitId;
    private String campaignId;
    private Mode mode;
    private String questionnaireId;
    private List<VariableInputDto> collectedVariables;
}
