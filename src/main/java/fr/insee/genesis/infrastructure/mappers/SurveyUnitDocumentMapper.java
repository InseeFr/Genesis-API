package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper(uses = VariableStateMapper.class)
public interface SurveyUnitDocumentMapper {

	SurveyUnitDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitDocumentMapper.class);

	@Mapping(source = "idQuestionnaire", target = "idQuest")
	SurveyUnit documentToModel(SurveyUnitDocument surveyUnit);

	@Mapping(source = "idQuest", target = "idQuestionnaire")
	SurveyUnitDocument modelToDocument(SurveyUnit surveyUnit);

	List<SurveyUnit> listDocumentToListModel(List<SurveyUnitDocument> surveyUnits);

	List<SurveyUnitDocument> listModelToListDocument(List<SurveyUnit> surveyUnitsDto);

}
