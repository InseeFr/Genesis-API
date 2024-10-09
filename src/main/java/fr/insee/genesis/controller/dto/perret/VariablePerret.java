package fr.insee.genesis.controller.dto.perret;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class VariablePerret {
    private String variableName;

    @JsonProperty("variableStates")
    private List<VariableStatePerret> variableStatePerretList;
}
