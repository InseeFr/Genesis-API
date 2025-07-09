package fr.insee.genesis.infrastructure.document.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Document(collection = "lunaticjsondata")
public record LunaticJsonRawDataDocument(
        @Id
        ObjectId id,
        String campaignId,
        String questionnaireId,
        String interrogationId,
        String idUE,
        Mode mode,
        Map<String,Object> data,
        LocalDateTime recordDate,
        @Indexed(direction = IndexDirection.DESCENDING)
        LocalDateTime processDate
){}