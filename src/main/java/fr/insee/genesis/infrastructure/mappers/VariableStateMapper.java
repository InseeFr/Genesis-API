package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.infrastructure.model.document.surveyunit.VariableState;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VariableStateMapper {

	VariableStateMapper INSTANCE = Mappers.getMapper(VariableStateMapper.class);

	CollectedVariable entityToModel(VariableState variableState);

	VariableState modelToEntity(CollectedVariable variableStateDto);

	List<CollectedVariable> listEntityToListModel(List<VariableState> variableStates);

	List<VariableState> listModelToListEntity(List<CollectedVariable> variableStatesDto);

}