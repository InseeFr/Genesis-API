package fr.insee.genesis.controller.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class SurveyUnitDto {
    private String interrogationId;
    private List<VariableDto> collectedVariables;
    private List<VariableDto> externalVariables;
}
