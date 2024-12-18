package fr.insee.genesis.infrastructure.document.surveyunit;

import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = Constants.MONGODB_RESPONSE_COLLECTION_NAME)
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
