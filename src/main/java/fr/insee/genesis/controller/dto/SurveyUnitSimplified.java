package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class SurveyUnitSimplified {

	private String collectionInstrumentId;
	/**
	 * @deprecated We will not reveive this piece of information anymore
	 */
	@Deprecated(forRemoval = true, since =  "2026-01-01")
	private String campaignId;
	private String interrogationId;
	private String usualSurveyUnitId;
	private Mode mode;
	private List<VariableModel> variablesUpdate;
	private List<VariableModel> externalVariables;
}
