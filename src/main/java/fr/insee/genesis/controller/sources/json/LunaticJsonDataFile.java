package fr.insee.genesis.controller.sources.json;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class LunaticJsonDataFile {

    @JsonProperty("Id")
    String questionnaireId;

    @JsonProperty("Label")
    String labelQuest;

    @JsonProperty("SurveyUnits")
    List<LunaticJsonSurveyUnit> surveyUnits;
}
