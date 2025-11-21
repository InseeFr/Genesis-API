package fr.insee.genesis.infrastructure.document.contextualprevious;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME)
@CompoundIndex(name = "questionnaireId_1_interrogationId_1", def = "{'questionnaireId': 1, 'interrogationId': 1}")
@CompoundIndex(name = "collectionInstrumentId_1_interrogationId_1", def = "{'collectionInstrumentId': 1, 'interrogationId': 1}")
@JsonIgnoreProperties(ignoreUnknown = true)
public class ContextualPreviousVariableDocument {
    @Id
    private String id;
    /**
     * @deprecated it will be replaced by collectionInstrumentId
     */
    @Deprecated(forRemoval = true)
    @Indexed
    String questionnaireId;
    @Indexed
    String collectionInstrumentId;
    String interrogationId;
    Map<String,Object> variables;
    String sourceState;
}
