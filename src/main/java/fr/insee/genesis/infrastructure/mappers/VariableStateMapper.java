package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.infrastructure.model.VariableState;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VariableStateMapper {

	VariableStateMapper INSTANCE = Mappers.getMapper(VariableStateMapper.class);

	CollectedVariableDto entityToDto(VariableState variableState);

	VariableState dtoToEntity(CollectedVariableDto variableStateDto);

	List<CollectedVariableDto> listEntityToListDto(List<VariableState> variableStates);

	List<VariableState> listDtoToListEntity(List<CollectedVariableDto> variableStatesDto);

}