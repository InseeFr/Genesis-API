package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ExternalVariableMapper {

	ExternalVariableMapper INSTANCE = Mappers.getMapper(ExternalVariableMapper.class);

	VariableDto entityToDto(ExternalVariable externalVariable);

	ExternalVariable dtoToEntity(VariableDto externalVariableDto);

	List<VariableDto> listEntityToListDto(List<ExternalVariable> externalVariables);

	List<ExternalVariable> listDtoToListEntity(List<VariableDto> externalVariablesDto);
}
