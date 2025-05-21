package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface DataProcessingContextMapper {
    DataProcessingContextMapper INSTANCE = Mappers.getMapper(DataProcessingContextMapper.class);

    DataProcessingContextModel documentToModel(DataProcessingContextDocument rawDataDoc);

    DataProcessingContextDocument modelToDocument(DataProcessingContextModel rawDataModel);

    List<DataProcessingContextModel> listDocumentToListModel(List<DataProcessingContextDocument> rawDataDocumentList);

    List<DataProcessingContextDocument> listModelToListDocument(List<DataProcessingContextModel> rawDataModelList);
}
