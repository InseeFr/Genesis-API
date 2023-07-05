package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.infrastructure.model.entity.SurveyUnitUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = VariableStateMapper.class)
public interface SurveyUnitUpdateMapper {

    SurveyUnitUpdateMapper INSTANCE = Mappers.getMapper(SurveyUnitUpdateMapper.class);

    @Mapping(source = "idQuestionnaire", target = "idQuest")
    SurveyUnitUpdateDto entityToDto(SurveyUnitUpdate surveyUnitUpdate);

    @Mapping(source = "idQuest", target = "idQuestionnaire")
    SurveyUnitUpdate dtoToEntity(SurveyUnitUpdateDto surveyUnitUpdateDto);

    List<SurveyUnitUpdateDto> listEntityToListDto(List<SurveyUnitUpdate> surveyUnitUpdates);

    List<SurveyUnitUpdate> listDtoToListEntity(List<SurveyUnitUpdateDto> surveyUnitUpdatesDto);
}
