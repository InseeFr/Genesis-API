package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticXmlDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticXmlDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LunaticXmlDocumentMapper {
	LunaticXmlDocumentMapper INSTANCE = Mappers.getMapper(LunaticXmlDocumentMapper.class);

	@Mapping(source = "lunaticXmlData", target = "data")
	LunaticXmlDataModel documentToModel(LunaticXmlDataDocument lunaticXmlDataDocument);

	@Mapping(source = "data", target = "lunaticXmlData")
	LunaticXmlDataDocument modelToDocument(LunaticXmlDataModel lunaticXmlDataModel);

	List<LunaticXmlDataModel> listDocumentToListModel(List<LunaticXmlDataDocument> lunaticXmlDataDocuments);

	List<LunaticXmlDataDocument> listModelToListDocument(List<LunaticXmlDataModel> lunaticXmlDataModels);

}
