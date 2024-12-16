package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VariableDto {
    private String variableName;
    private String idLoop;

    @JsonProperty("variableStates")
    private List<VariableStateDto> variableStateDtoList;
}
