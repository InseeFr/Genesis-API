package fr.insee.genesis.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitQualityToolDto {
    private String surveyUnitId;
    private List<VariableQualityToolDto> collectedVariables;
    private List<VariableQualityToolDto> externalVariables;
}
