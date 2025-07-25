package fr.insee.genesis.infrastructure.document.editedexternal;

import fr.insee.genesis.Constants;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
@CompoundIndex(name = "questionnaireId_1_interrogationId_1", def = "{'questionnaireId': 1, 'interrogationId': 1}")
public class EditedExternalResponseDocument {
    @Id
    private String id;
    @Indexed
    String questionnaireId;
    String interrogationId;
    Map<String,Object> variables;
}
