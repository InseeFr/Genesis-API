package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitDocument;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SurveyUnitDocumentMapper {

	SurveyUnitDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitDocumentMapper.class);

	SurveyUnitDto documentToDto(SurveyUnitDocument surveyUnit);

	SurveyUnitDocument dtoToDocument(SurveyUnitDto surveyUnitDto);

	List<SurveyUnitDto> listDocumentToListDto(List<SurveyUnitDocument> surveyUnits);

	List<SurveyUnitDocument> listDtoToListDocument(List<SurveyUnitDto> surveyUnitsDto);

}
