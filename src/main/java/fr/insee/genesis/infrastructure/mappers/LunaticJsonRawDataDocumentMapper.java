package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LunaticJsonRawDataDocumentMapper {

    LunaticJsonRawDataDocumentMapper INSTANCE = Mappers.getMapper(LunaticJsonRawDataDocumentMapper.class);

    LunaticJsonRawDataModel documentToModel(LunaticJsonRawDataDocument rawDataDoc);

    LunaticJsonRawDataDocument modelToDocument(LunaticJsonRawDataModel rawDataModel);

    List<LunaticJsonRawDataModel> listDocumentToListModel(List<LunaticJsonRawDataDocument> rawDataDocumentList);

    List<LunaticJsonRawDataDocument> listModelToListDocument(List<LunaticJsonRawDataModel> rawDataModelList);
}
