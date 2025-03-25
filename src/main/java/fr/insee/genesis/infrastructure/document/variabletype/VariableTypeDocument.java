package fr.insee.genesis.infrastructure.document.variabletype;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = Constants.MONGODB_VARIABLETYPE_COLLECTION_NAME)
public class VariableTypeDocument {
    private String campaignId;
    private String questionnaireId;
    private Mode mode;
    private VariablesMap variablesMap;
}
