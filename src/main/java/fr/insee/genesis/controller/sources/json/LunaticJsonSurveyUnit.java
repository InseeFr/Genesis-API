package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class LunaticJsonSurveyUnit {

    @JsonProperty("Id")
    private String interrogationId;

    @JsonProperty("QuestionnaireModelId")
    private String questionnaireId;

    @JsonProperty("Data")
    private LunaticJsonDataResponse dataResponse;

}
