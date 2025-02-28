package fr.insee.genesis.infrastructure.document.rawdata;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawData;
import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Builder
@Document(collection = Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME)
public record LunaticJsonDataDocument(
    @Id
    ObjectId id,
    String campaignId,
    String questionnaireId,
    String interrogationId,
    String idUE,
    Mode mode,
    LunaticJsonRawData data,
    LocalDateTime recordDate,
    LocalDateTime processDate
){}
