package fr.insee.genesis.controller.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitDto {
    private String interrogationId;
    private List<VariableDto> collectedVariables;
    private List<VariableDto> externalVariables;
}
