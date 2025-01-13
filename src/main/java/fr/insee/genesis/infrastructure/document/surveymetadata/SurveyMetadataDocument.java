package fr.insee.genesis.infrastructure.document.surveymetadata;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = Constants.MONGODB_SURVEYMETADATA_COLLECTION_NAME)
public record SurveyMetadataDocument (
    String campaignId,
    String questionnaireId,
    Mode mode,
    Map<String, VariableDocument> variableDefinitions
){}