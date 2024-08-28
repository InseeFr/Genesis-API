package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitSimplified {

	private String idQuest;
	private String idCampaign;
	private String idUE;
	private Mode mode;
	private List<CollectedVariable> variablesUpdate;
	private List<Variable> externalVariables;
}
