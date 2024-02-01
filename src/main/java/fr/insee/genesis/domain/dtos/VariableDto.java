package fr.insee.genesis.domain.dtos;

import lombok.*;
import org.json.simple.JSONObject;

import java.util.List;

@Builder
@Getter
@Setter
public class VariableDto {

	private String idVar;
	private List<String> values;

	public JSONObject toJSONObject(){
		JSONObject jsonObject = new JSONObject();
		jsonObject.put("idVar", idVar);
		jsonObject.put("values",values);

		return jsonObject;
	}
}
