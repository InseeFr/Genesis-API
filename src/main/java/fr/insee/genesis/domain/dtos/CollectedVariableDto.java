package fr.insee.genesis.domain.dtos;

import lombok.*;
import org.json.simple.JSONObject;

import java.util.List;


@Getter
@Setter
public class CollectedVariableDto extends VariableDto{

    private String idLoop;
    private String idParent;

    @Builder(builderMethodName = "collectedVariableBuilder")
    public CollectedVariableDto(String idVar, List<String> values, String idLoop, String idParent) {
        super(idVar, values);
        this.idLoop = idLoop;
        this.idParent = idParent;
    }

    public JSONObject toJSONObject(){
        JSONObject jsonObject = super.toJSONObject();
        jsonObject.put("idLoop", idLoop);
        jsonObject.put("idParent", idParent);

        return jsonObject;
    }

}
