package fr.insee.genesis.controller.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class VariableDto {
    private String variableName;
    private String scope;
    private int iteration;

    @JsonProperty("variableStates")
    private List<VariableStateDto> variableStateDtoList;
}
