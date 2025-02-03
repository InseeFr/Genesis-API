package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitSimplified {

	private String questionnaireId;
	private String campaignId;
	private String interrogationId;
	private Mode mode;
	private List<VariableModel> variablesUpdate;
	private List<VariableModel> externalVariables;
}
