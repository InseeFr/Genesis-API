package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LunaticJsonVariableData {

    @JsonProperty("EDITED")
    String edited;
    @JsonProperty("FORCED")
    String forced;
    @JsonProperty("INPUTED")
    String inputed;
    @JsonProperty("PREVIOUS")
    String previous;
    @JsonProperty("COLLECTED")
    String collected;
}
