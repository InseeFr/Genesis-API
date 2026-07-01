package fr.insee.genesis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SurveyUnitQualityToolDto {
    private String interrogationId;
    private List<VariableQualityToolDto> collectedVariables;
    private List<VariableQualityToolDto> externalVariables;
}
