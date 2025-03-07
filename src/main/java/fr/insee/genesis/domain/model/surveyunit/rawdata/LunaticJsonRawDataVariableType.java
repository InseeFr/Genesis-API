package fr.insee.genesis.domain.model.surveyunit.rawdata;

import lombok.Getter;

@Getter
public enum LunaticJsonRawDataVariableType {
    COLLECTED("COLLECTED"),
    EXTERNAL("EXTERNAL");

    private final String jsonNodeName;

    LunaticJsonRawDataVariableType(String jsonNodeName) {
        this.jsonNodeName = jsonNodeName;
    }
}
