package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.VariableStateDto;
import fr.insee.genesis.infrastructure.model.entity.VariableState;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface VariableStateMapper {

    VariableStateMapper INSTANCE = Mappers.getMapper(VariableStateMapper.class);

    VariableStateDto entityToDto(VariableState variableState);

    VariableState dtoToEntity(VariableStateDto variableStateDto);

    List<VariableStateDto> listEntityToListDto(List<VariableState> variableStates);

    List<VariableState> listDtoToListEntity(List<VariableStateDto> variableStatesDto);

}
