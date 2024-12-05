package fr.insee.genesis.infrastructure.document.rawdata;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = Constants.MONGODB_LUNATIC_JSON_DATA_COLLECTION_NAME)
public class LunaticJsonDataDocument {
    @Id
    private ObjectId id;
    private String campaignId;
    private Mode mode;
    private Map<String, Object> data;
    private LocalDateTime recordDate;
    private LocalDateTime processDate;
}
