package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface LastJsonExtractionDocumentMapper {

    LastJsonExtractionDocumentMapper INSTANCE = Mappers.getMapper(LastJsonExtractionDocumentMapper.class);

    LastJsonExtractionModel documentToModel(LastJsonExtractionDocument lastJsonExtractionDoc);

    LastJsonExtractionDocument modelToDocument(LastJsonExtractionModel lastJsonExtractionModel);

    List<LastJsonExtractionModel> listDocumentToListModel(List<LastJsonExtractionDocument> lastJsonExtractionDocumentList);

    List<LastJsonExtractionDocument> listModelToListDocument(List<LastJsonExtractionModel> lastJsonExtractionModelList);
}
