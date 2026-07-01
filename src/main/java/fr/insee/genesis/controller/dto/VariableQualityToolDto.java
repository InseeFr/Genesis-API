package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VariableQualityToolDto {
    private String variableName;
    private Integer iteration;

    @JsonProperty("variableStates")
    private List<VariableStateDto> variableStateDtoList;
}
