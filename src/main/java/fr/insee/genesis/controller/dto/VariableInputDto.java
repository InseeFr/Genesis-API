package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class VariableInputDto {
    private String variableName;
    private int iteration;

    @JsonProperty("newVariableState")
    private VariableStateInputDto variableStateInputDto;
}
