package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.editedresponse.EditedExternalResponseModel;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface EditedExternalResponseDocumentMapper {
    EditedExternalResponseDocumentMapper INSTANCE = Mappers.getMapper(EditedExternalResponseDocumentMapper.class);

    EditedExternalResponseModel documentToModel(EditedExternalResponseDocument editedExternalDoc);

    EditedExternalResponseDocument modelToDocument(EditedExternalResponseModel rawDataModel);

    List<EditedExternalResponseModel> listDocumentToListModel(List<EditedExternalResponseDocument> rawDataDocumentList);

    List<EditedExternalResponseDocument> listModelToListDocument(List<EditedExternalResponseModel> rawDataModelList);
}
