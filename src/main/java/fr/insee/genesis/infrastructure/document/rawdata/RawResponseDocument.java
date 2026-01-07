package fr.insee.genesis.infrastructure.document.rawdata;

import lombok.Builder;
import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.IndexDirection;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Document(collection = "rawResponses")
public record RawResponseDocument (
        @Id
        ObjectId id,
        @Indexed
        String interrogationId,
        String collectionInstrumentId,
        String mode,
        Map<String,Object> payload,
        LocalDateTime recordDate,
        @Indexed(direction = IndexDirection.DESCENDING)
        LocalDateTime processDate,
        @Indexed
        String campaignId
){}
