package fr.insee.genesis.infrastructure.document.extraction.json;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@Document(collection = Constants.MONGODB_EXTRACTION_JSON_COLLECTION_NAME)
public class LastJsonExtractionDocument {

    @Id
    private String id;
    private String collectionInstrumentId;
    private Mode mode;
    private LocalDateTime lastExtractionDate;

}
