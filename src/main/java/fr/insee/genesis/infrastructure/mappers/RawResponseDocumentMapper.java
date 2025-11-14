package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface RawResponseDocumentMapper {

    RawResponseDocumentMapper INSTANCE = Mappers.getMapper(RawResponseDocumentMapper.class);

    RawResponse documentToModel(RawResponseDocument document);
    RawResponseDocument modelToDocument(RawResponse model);
    List<RawResponse> listDocumentToListModel(List<RawResponseDocument> documentList);
    List<RawResponseDocument> listModelToListDocument(List<RawResponse> modelList);
}
