package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import lombok.Data;

import java.util.HashMap;
import java.util.Map;

@Data
public class LunaticJsonCollectedVariables {

    private Map<String, LunaticJsonVariableData> collectedMap = new HashMap<>();

    @JsonAnySetter
    public void setVariables(String var, LunaticJsonVariableData value){
        this.collectedMap.put(var, value);
    }
}
