package fr.insee.genesis.controller.dto;

import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Data
public class SurveyUnitSimplifiedDto {

	private String collectionInstrumentId;
	private String interrogationId;
	private String usualSurveyUnitId;
	private Mode mode;
    private Boolean isCapturedIndirectly;
    private LocalDateTime validationDate;
	private RawResponseDto.QuestionnaireStateEnum questionnaireState;
	private List<VariableModel> variablesUpdate;
	private List<VariableModel> externalVariables;
}
