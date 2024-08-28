package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.infrastructure.model.document.surveyunit.ExternalVariable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ExternalVariableMapper {

	ExternalVariableMapper INSTANCE = Mappers.getMapper(ExternalVariableMapper.class);

	Variable entityToModel(ExternalVariable externalVariable);

	ExternalVariable modelToEntity(Variable externalVariable);

	List<Variable> listEntityToListModel(List<ExternalVariable> externalVariables);

	List<ExternalVariable> listModelToListEntity(List<Variable> externalVariablesDto);
}
