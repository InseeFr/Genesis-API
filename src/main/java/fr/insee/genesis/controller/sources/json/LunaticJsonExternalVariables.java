package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LunaticJsonExternalVariables {
    private Map<String, String> externalMap = new HashMap<>();

    @JsonAnySetter
    public void setVariables(String variable, String value){
        this.externalMap.put(variable, value);
    }

}
