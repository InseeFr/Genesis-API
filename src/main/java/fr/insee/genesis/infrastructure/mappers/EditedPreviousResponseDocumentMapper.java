package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.editedresponse.EditedPreviousResponseModel;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EditedPreviousResponseDocumentMapper {
    EditedPreviousResponseDocumentMapper INSTANCE = Mappers.getMapper(EditedPreviousResponseDocumentMapper.class);

    EditedPreviousResponseModel documentToModel(EditedPreviousResponseDocument editedPreviousDoc);

    EditedPreviousResponseDocument modelToDocument(EditedPreviousResponseModel rawDataModel);

    List<EditedPreviousResponseModel> listDocumentToListModel(List<EditedPreviousResponseDocument> rawDataDocumentList);

    List<EditedPreviousResponseDocument> listModelToListDocument(List<EditedPreviousResponseModel> rawDataModelList);
}
