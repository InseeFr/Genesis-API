package fr.insee.genesis.infrastructure.mappers;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.genesis.infrastructure.document.surveymetadata.VariableDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariableDocumentMapper {
    VariableDocumentMapper INSTANCE = Mappers.getMapper(VariableDocumentMapper.class);

    Variable documentToBpm(VariableDocument variableDocument);

    VariableDocument bpmToDocument(Variable variable);

    List<Variable> listDocumentToListBpm(List<VariableDocument> variableDocumentList);

    List<VariableDocument> listBpmToListDocument(List<Variable> variableList);
}
