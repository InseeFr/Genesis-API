package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import org.mapstruct.AfterMapping;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface SurveyUnitDocumentMapper {
	SurveyUnitDocumentMapper INSTANCE = Mappers.getMapper(SurveyUnitDocumentMapper.class);

	@Mapping(target = "mode", source = "mode", qualifiedByName = "mapMode")
	SurveyUnitModel documentToModel(SurveyUnitDocument surveyUnit);

	SurveyUnitDocument modelToDocument(SurveyUnitModel surveyUnitModel);

	@Mapping(target = "mode", source = "mode", qualifiedByName = "mapMode")
	List<SurveyUnitModel> listDocumentToListModel(List<SurveyUnitDocument> surveyUnits);

	List<SurveyUnitDocument> listModelToListDocument(List<SurveyUnitModel> surveyUnitModels);

	@AfterMapping
	@SuppressWarnings("deprecation")
	default void handleDeprecatedFields(SurveyUnitDocument doc,
									@MappingTarget SurveyUnitModel model) {

		if (model.getUsualSurveyUnitId() == null) {
			model.setUsualSurveyUnitId(doc.getIdUE());
		}

		if (model.getCollectionInstrumentId() == null) {
			model.setCollectionInstrumentId(doc.getQuestionnaireId());
		}

	}

	@Named("mapMode")
	default Mode mapMode(String value) {
		return Mode.fromString(value);
	}
}
