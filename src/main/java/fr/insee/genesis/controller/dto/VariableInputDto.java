package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class VariableInputDto {
    private String variableName;
    private String idLoop;

    @JsonProperty("newVariableState")
    private VariableStateInputDto variableStateInputDto;
}
