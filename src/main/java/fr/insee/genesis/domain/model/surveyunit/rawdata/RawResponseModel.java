package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Map;

public record RawResponseModel(
        ObjectId id,
        String interrogationId,
        String collectionInstrumentId,
        Mode mode,
        Map<String,Object> payload,
        LocalDateTime recordDate,
        LocalDateTime processDate
)
{}
