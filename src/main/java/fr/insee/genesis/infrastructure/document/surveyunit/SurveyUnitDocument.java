package fr.insee.genesis.infrastructure.document.surveyunit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@CompoundIndex(name = "collectionInstrumentId_1_interrogationId_1", def = "{'collectionInstrumentId': 1, 'interrogationId': 1}")
@CompoundIndex(name = "interrogationId_1_collectionInstrumentId_1", def = "{'interrogationId': 1, 'collectionInstrumentId': 1}")
@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyUnitDocument {

	/**
	 * @deprecated This piece of information will not be available anymore in the raw responses
	 */
	@Deprecated(forRemoval = true)
	private String campaignId;
	@Indexed
	private String interrogationId;

	/**
	 * @deprecated It will be replaced by usualSurveyUnitId
	 */
	@Deprecated(forRemoval = true)
	private String idUE;

	private String usualSurveyUnitId;

	/**
	 * @deprecated It will be replaced by collectionInstrumentId
	 */
	@Deprecated(forRemoval = true)
	private String questionnaireId;

	private String collectionInstrumentId;

	private String majorModelVersion;
	private String state;
	@Indexed
	private String mode;
	private LocalDateTime recordDate;
	private LocalDateTime fileDate;
	private List<VariableDocument> collectedVariables;
	private List<VariableDocument> externalVariables;
	private String modifiedBy;
}
