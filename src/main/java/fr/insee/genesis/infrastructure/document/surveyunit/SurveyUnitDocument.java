package fr.insee.genesis.infrastructure.document.surveyunit;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.genesis.Constants;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.Data;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Document(collection = Constants.MONGODB_RESPONSE_COLLECTION_NAME)

@CompoundIndex(name = "questionnaireId_interrogationId", def = "{'questionnaireId': 1, 'interrogationId': 1}")
@CompoundIndex(name = "collectionInstrumentId_interrogationId", def = "{'collectionInstrumentId': 1, 'interrogationId': 1}")
@CompoundIndex(name = "usualSurveyUnitId_collectionInstrumentId", def = "{'usualSurveyUnitId': 1, 'collectionInstrumentId': 1}")
@CompoundIndex(name = "usualSurveyUnitId_questionnaireId", def = "{'usualSurveyUnitId': 1, 'questionnaireId': 1}")
@CompoundIndex(name = "questionnaireId_mode_interrogationId", def = "{'questionnaireId': 1, 'mode': 1, 'interrogationId': 1}")
@CompoundIndex(name = "questionnaireId_recordDate", def = "{'questionnaireId': 1, 'recordDate': 1}")
@CompoundIndex(name = "collectionInstrumentId_recordDate", def = "{'collectionInstrumentId': 1, 'recordDate': 1}")

@JsonIgnoreProperties(ignoreUnknown = true)
public class SurveyUnitDocument {
	@Indexed
	private String interrogationId;

	/**
	 * @deprecated It will be replaced by usualSurveyUnitId
	 */
	@Deprecated(forRemoval = true, since ="2026-01-01")
	private String idUE;

	private String usualSurveyUnitId;

	/**
	 * @deprecated It will be replaced by collectionInstrumentId
	 */
	@Deprecated(since ="2026-01-01")
	@Indexed
	private String questionnaireId;

	@Indexed
	private String collectionInstrumentId;

	private String majorModelVersion;
	private String state;
	@Indexed
	private String mode;
	private Instant recordDate;
    /**
     * @deprecated use {@link #rawRecordDate} instead.
     * This field is kept temporarily for backward compatibility and will be removed in a future version.
     */
    @Deprecated(since = "2026-05-11")
	private LocalDateTime fileDate;
    private LocalDateTime rawRecordDate;
	private List<VariableDocument> collectedVariables;
	private List<VariableDocument> externalVariables;
	private String modifiedBy;
    private Boolean isCapturedIndirectly;
	private LocalDateTime validationDate;
	private RawResponseDto.QuestionnaireStateEnum questionnaireState;
}
