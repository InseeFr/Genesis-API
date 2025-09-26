package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.infrastructure.document.contextualexternal.ContextualExternalVariableDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ContextualExternalVariableDocumentMapper {
    ContextualExternalVariableDocumentMapper INSTANCE = Mappers.getMapper(ContextualExternalVariableDocumentMapper.class);

    ContextualExternalVariableModel documentToModel(ContextualExternalVariableDocument contextualExternalDoc);

    ContextualExternalVariableDocument modelToDocument(ContextualExternalVariableModel rawDataModel);

    List<ContextualExternalVariableModel> listDocumentToListModel(List<ContextualExternalVariableDocument> rawDataDocumentList);

    List<ContextualExternalVariableDocument> listModelToListDocument(List<ContextualExternalVariableModel> rawDataModelList);
}
