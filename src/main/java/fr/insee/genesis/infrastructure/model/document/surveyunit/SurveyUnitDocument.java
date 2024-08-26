package fr.insee.genesis.infrastructure.model.document.surveyunit;

import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = "responses")
public class SurveyUnitDocument {
	private String idCampaign;
	private String idUE;
	private String idQuestionnaire;
	private String state;
	private String mode;
	private LocalDateTime recordDate;
	private LocalDateTime fileDate;
	private List<VariableState> collectedVariables;
	private List<ExternalVariable> externalVariables;
}
