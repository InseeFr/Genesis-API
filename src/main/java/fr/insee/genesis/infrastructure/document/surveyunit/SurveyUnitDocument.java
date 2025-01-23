package fr.insee.genesis.infrastructure.document.surveyunit;

import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = Constants.MONGODB_RESPONSE_COLLECTION_NAME)
@CompoundIndex(name = "idCampaign_1_idQuestionnaire_1", def = "{'idCampaign': 1, 'idQuestionnaire': 1}") //1 = ascending, -1 = descending
@CompoundIndex(name = "idQuestionnaire_1_idCampaign_1", def = "{'idQuestionnaire': 1, 'idCampaign': 1}")
@CompoundIndex(name = "idQuestionnaire_1_idUE_1", def = "{'idQuestionnaire': 1, 'idUE': 1}")
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
	private String modifiedBy;
}
