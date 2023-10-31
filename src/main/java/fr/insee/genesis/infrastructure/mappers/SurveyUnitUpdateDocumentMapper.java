package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = VariableStateMapper.class)
public interface SurveyUnitUpdateDocumentMapper {

	SurveyUnitUpdateDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitUpdateDocumentMapper.class);

	@Mapping(source = "idQuestionnaire", target = "idQuest")
	SurveyUnitUpdateDto documentToDto(SurveyUnitUpdateDocument surveyUnitUpdateDocument);

	@Mapping(source = "idQuest", target = "idQuestionnaire")
	SurveyUnitUpdateDocument dtoToDocument(SurveyUnitUpdateDto surveyUnitUpdateDto);

	List<SurveyUnitUpdateDto> listDocumentToListDto(List<SurveyUnitUpdateDocument> surveyUnitUpdateDocuments);

	List<SurveyUnitUpdateDocument> listDtoToListDocument(List<SurveyUnitUpdateDto> surveyUnitUpdatesDto);
}
