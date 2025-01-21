package fr.insee.genesis.infrastructure.document.rawdata;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Data;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Data
@Document(collection = Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
public class LunaticJsonDataDocument { //TODO try to use record
    @Id
    private ObjectId id;
    private String campaignId;
    private String idQuest;
    private String idUE;
    private Mode mode;
    private Map<String, Object> data;
    private LocalDateTime recordDate;
    private LocalDateTime processDate;
}
