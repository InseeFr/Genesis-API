package fr.insee.genesis.controller.responses;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.dtos.Mode;
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
	private List<CollectedVariableDto> variablesUpdate;
	private List<VariableDto> externalVariables;
}
