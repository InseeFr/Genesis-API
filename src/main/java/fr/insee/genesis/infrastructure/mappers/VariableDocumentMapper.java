package fr.insee.genesis.infrastructure.mappers;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.genesis.infrastructure.document.surveymetadata.VariableDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(componentModel = "spring")
public interface VariableDocumentMapper {
    VariableDocumentMapper INSTANCE = Mappers.getMapper(VariableDocumentMapper.class);

    @Mapping(source = "isInQuestionGrid", target = "inQuestionGrid")
    Variable documentToBpm(VariableDocument variableDocument);

    @Mapping(source = "inQuestionGrid", target = "isInQuestionGrid")
    VariableDocument bpmToDocument(Variable variable);

    List<Variable> listDocumentToListBpm(List<VariableDocument> variableDocumentList);

    List<VariableDocument> listBpmToListDocument(List<Variable> variableList);
}
