package fr.insee.genesis.controller.responses;

import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.ExternalVariableDto;
import fr.insee.genesis.domain.dtos.Source;
import fr.insee.genesis.domain.dtos.VariableStateDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class SurveyUnitUpdateSimplified {

	private String idQuest;
	private String idCampaign;
	private String idUE;
	private List<VariableStateDto> variablesUpdate;
	private List<ExternalVariableDto> externalVariables;
}
