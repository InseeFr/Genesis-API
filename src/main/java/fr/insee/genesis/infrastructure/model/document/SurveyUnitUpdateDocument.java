package fr.insee.genesis.infrastructure.model.document;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.mongodb.core.mapping.Document;

import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import lombok.Data;

@Data
@Document(collection = "responses")
public class SurveyUnitUpdateDocument {


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
