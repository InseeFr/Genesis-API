package fr.insee.genesis.infrastructure.document.surveyunit;

import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = Constants.MONGODB_RESPONSE_COLLECTION_NAME)
@CompoundIndex(name = "campaignId_1_questionnaireId_1", def = "{'campaignId': 1, 'questionnaireId': 1}") //1 = ascending, -1 = descending
@CompoundIndex(name = "questionnaireId_1_campaignId_1", def = "{'questionnaireId': 1, 'campaignId': 1}")
@CompoundIndex(name = "questionnaireId_1_interrogationId_1", def = "{'questionnaireId': 1, 'interrogationId': 1}")
@CompoundIndex(name = "interrogationId_1_questionnaireId_1", def = "{'interrogationId': 1, 'questionnaireId': 1}")
public class SurveyUnitDocument {
	private String campaignId;
	@Indexed
	private String interrogationId;
	private String questionnaireId;
	private String state;
	private String mode;
	private LocalDateTime recordDate;
	private LocalDateTime fileDate;
	private List<VariableDocument> collectedVariables;
	private List<VariableDocument> externalVariables;
	private String modifiedBy;
}
