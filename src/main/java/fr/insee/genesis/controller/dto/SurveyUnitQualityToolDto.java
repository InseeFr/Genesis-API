package fr.insee.genesis.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitQualityToolDto {
    private String interrogationId;
    private List<VariableQualityToolDto> collectedVariables;
    private List<VariableQualityToolDto> externalVariables;
}
