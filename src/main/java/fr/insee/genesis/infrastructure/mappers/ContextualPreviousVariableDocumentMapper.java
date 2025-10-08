package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ContextualPreviousVariableDocumentMapper {
    ContextualPreviousVariableDocumentMapper INSTANCE = Mappers.getMapper(ContextualPreviousVariableDocumentMapper.class);

    ContextualPreviousVariableModel documentToModel(ContextualPreviousVariableDocument contextualPreviousDoc);

    ContextualPreviousVariableDocument modelToDocument(ContextualPreviousVariableModel rawDataModel);

    List<ContextualPreviousVariableModel> listDocumentToListModel(List<ContextualPreviousVariableDocument> rawDataDocumentList);

    List<ContextualPreviousVariableDocument> listModelToListDocument(List<ContextualPreviousVariableModel> rawDataModelList);
}
