package fr.insee.genesis.infrastructure.document.editedprevious;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Map;

@Data
@Document(collection = "editedPrevious")
public class EditedPreviousResponseDocument {
    @Id
    private String id;
    @Indexed
    String questionnaireId;
    String interrogationId;
    Map<String,Object> variables;
    String sourceState;
}
