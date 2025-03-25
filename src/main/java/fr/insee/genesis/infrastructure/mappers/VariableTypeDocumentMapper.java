package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.infrastructure.document.variabletype.VariableTypeDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariableTypeDocumentMapper {
    VariableTypeDocumentMapper INSTANCE = Mappers.getMapper(VariableTypeDocumentMapper.class);
    
    VariableTypeModel documentToModel(VariableTypeDocument variableTypeDocument);

    VariableTypeDocument modelToDocument(VariableTypeModel variableTypeModel);

    List<VariableTypeModel> listDocumentToListModel(List<VariableTypeDocument> variableTypeDocuments);

    List<VariableTypeDocument> listModelToListDocument(List<VariableTypeModel> variableTypeModels);
}
