package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = VariableStateMapper.class)
public interface SurveyUnitDocumentMapper {

	SurveyUnitDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitDocumentMapper.class);

	@Mapping(source = "idQuestionnaire", target = "idQuest")
	SurveyUnitDto documentToDto(SurveyUnitDocument surveyUnit);

	@Mapping(source = "idQuest", target = "idQuestionnaire")
	SurveyUnitDocument dtoToDocument(SurveyUnitDto surveyUnitDto);

	List<SurveyUnitDto> listDocumentToListDto(List<SurveyUnitDocument> surveyUnits);

	List<SurveyUnitDocument> listDtoToListDocument(List<SurveyUnitDto> surveyUnitsDto);

}
