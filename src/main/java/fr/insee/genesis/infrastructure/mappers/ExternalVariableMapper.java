package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.ExternalVariableDto;
import fr.insee.genesis.infrastructure.model.entity.ExternalVariable;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface ExternalVariableMapper {

	ExternalVariableMapper INSTANCE = Mappers.getMapper(ExternalVariableMapper.class);

	ExternalVariableDto entityToDto(ExternalVariable externalVariable);

	ExternalVariable dtoToEntity(ExternalVariableDto externalVariableDto);

	List<ExternalVariableDto> listEntityToListDto(List<ExternalVariable> externalVariables);

	List<ExternalVariable> listDtoToListEntity(List<ExternalVariableDto> externalVariablesDto);
}
