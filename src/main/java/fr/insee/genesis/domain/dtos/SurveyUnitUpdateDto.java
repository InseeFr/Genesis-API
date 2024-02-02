package fr.insee.genesis.domain.dtos;

import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Data;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

@Builder
@Data
public class SurveyUnitUpdateDto {

    private String idQuest;
    private String idCampaign;
    private String idUE;
    private DataState state;
    private Mode mode;
    private LocalDateTime recordDate;
    private LocalDateTime fileDate;
    private List<CollectedVariableDto> collectedVariables;
    private List<VariableDto> externalVariables;

    public JSONObject toJSONObject(){
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("idQuest",idQuest);
        jsonObject.put("idCampaign",idCampaign);
        jsonObject.put("idUE",idUE);
        jsonObject.put("state",state.toString());
        jsonObject.put("mode",mode.getModeName());
        jsonObject.put("recordDate",recordDate.toString());
        jsonObject.put("fileDate",fileDate.toString());

        JSONArray collectedVariablesJSONArray = new JSONArray();
        if(collectedVariables != null)
            for(CollectedVariableDto collectedVariableDto : collectedVariables)
                collectedVariablesJSONArray.add(collectedVariableDto.toJSONObject());

        JSONArray externalVariablesJSONArray = new JSONArray();
        if(externalVariables != null)
            for (VariableDto variableDto : externalVariables)
                externalVariablesJSONArray.add(variableDto.toJSONObject());

        jsonObject.put("collectedVariables",collectedVariablesJSONArray);
        jsonObject.put("externalVariables",externalVariablesJSONArray);

        return jsonObject;
    }
}
