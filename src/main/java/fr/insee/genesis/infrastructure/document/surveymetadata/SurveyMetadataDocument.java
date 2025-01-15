package fr.insee.genesis.infrastructure.document.surveymetadata;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Document(collection = Constants.MONGODB_SURVEYMETADATA_COLLECTION_NAME)
@CompoundIndex(name = "idCampaign_1_idQuestionnaire_1_mode_1", def = "{'idCampaign': 1, 'idQuestionnaire': 1, 'mode':1}") //1 = ascending, -1 = descending
public record SurveyMetadataDocument (
    String campaignId,
    String questionnaireId,
    Mode mode,
    Map<String, VariableDocument> variableDefinitions
){}