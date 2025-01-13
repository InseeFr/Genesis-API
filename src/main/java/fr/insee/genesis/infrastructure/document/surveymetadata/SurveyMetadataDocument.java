package fr.insee.genesis.infrastructure.document.surveymetadata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = Constants.MONGODB_SURVEYMETADATA_COLLECTION_NAME)
public class SurveyMetadataDocument {
    private String campaignId;
    private String questionnaireId;
    private Mode mode;
    private VariablesMap variablesMap;
    private Map<String, VariableDocument> variableDefinitions;
}
