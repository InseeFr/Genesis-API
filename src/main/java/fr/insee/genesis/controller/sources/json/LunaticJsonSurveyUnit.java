package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LunaticJsonSurveyUnit {

    @JsonProperty("Id")
    private String idUE;

    @JsonProperty("QuestionnaireModelId")
    private String idQuest;

    @JsonProperty("Data")
    private LunaticJsonDataResponse dataResponse;

}
