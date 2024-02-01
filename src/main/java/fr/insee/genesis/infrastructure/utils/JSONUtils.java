package fr.insee.genesis.infrastructure.utils;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import lombok.extern.slf4j.Slf4j;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.util.List;

@Slf4j
public class JSONUtils {
    private JSONUtils() {
        throw new IllegalStateException("Utility class");
    }
    //TODO Tests
    /**
     * Convert a list of survey units DTOs to a JSONArray ready to write
     * @param surveyUnitUpdateDtos survey units to convert
     * @return a JSONArray containing all objects
     */
    public static JSONArray getJSONArrayFromResponses(List<SurveyUnitUpdateDto> surveyUnitUpdateDtos) {
        JSONArray jsonArray = new JSONArray();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : surveyUnitUpdateDtos){
            jsonArray.add(surveyUnitUpdateDto.toJSONObject());
        }
        return jsonArray;
    }
}
