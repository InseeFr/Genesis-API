package fr.insee.genesis.infrastructure.mappers;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;

@Mapper(componentModel = "spring")
public interface LunaticJsonDocumentMapper {
	LunaticJsonDocumentMapper INSTANCE = Mappers.getMapper(LunaticJsonDocumentMapper.class);

	@Mapping(source = "data", target = "dataJson", qualifiedByName = "fromMapToJson")
	LunaticJsonDataModel documentToModel(LunaticJsonDataDocument lunaticJsonDataDocument);

	@Mapping(source = "dataJson", target = "data", qualifiedByName = "fromJsonToMap")
	LunaticJsonDataDocument modelToDocument(LunaticJsonDataModel lunaticJsonDataModel);

	List<LunaticJsonDataModel> listDocumentToListModel(List<LunaticJsonDataDocument> lunaticJsonDataDocuments);

	List<LunaticJsonDataDocument> listModelToListDocument(List<LunaticJsonDataModel> lunaticJsonDataModels);


	@Named(value = "fromJsonToMap")
	default Map<String, Object> fromJsonToMap(String dataJson) throws JsonProcessingException {
		ObjectMapper objectMapper = new ObjectMapper();
		return objectMapper.readValue(dataJson, new TypeReference<>(){});
	}

	@Named(value = "fromMapToJson")
	default String fromMapToJson(Map<String, Object> dataMap) throws JsonProcessingException {
			ObjectMapper objectMapper = new ObjectMapper();
			return objectMapper.writeValueAsString(dataMap);
	}
}
