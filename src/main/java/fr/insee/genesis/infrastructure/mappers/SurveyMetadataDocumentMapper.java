package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface SurveyMetadataDocumentMapper {
    SurveyMetadataDocumentMapper INSTANCE = Mappers.getMapper(SurveyMetadataDocumentMapper.class);

    @Mapping(source = "variableDefinitions", target = "variableDocumentMap")
    SurveyMetadataModel documentToModel(SurveyMetadataDocument surveyMetadataDocument);

    @Mapping(source = "variableDocumentMap", target = "variableDefinitions")
    SurveyMetadataDocument modelToDocument(SurveyMetadataModel surveyMetadataModel);

    List<SurveyMetadataModel> listDocumentToListModel(List<SurveyMetadataDocument> surveyMetadataDocuments);

    List<SurveyMetadataDocument> listModelToListDocument(List<SurveyMetadataModel> surveyMetadataModels);

}
