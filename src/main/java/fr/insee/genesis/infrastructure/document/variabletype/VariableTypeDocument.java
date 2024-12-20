package fr.insee.genesis.infrastructure.document.variabletype;

import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = Constants.MONGODB_VARIABLETYPE_COLLECTION_NAME)
public class VariableTypeDocument {
    private String campaignId;
    private String questionnaireId;
    private Mode mode;
    private Map<String, VariableType> variables;
}
