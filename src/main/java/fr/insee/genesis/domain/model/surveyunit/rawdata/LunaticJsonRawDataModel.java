package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record LunaticJsonRawDataModel(
        ObjectId id,
        String campaignId,
        String questionnaireId,
        String interrogationId,
        String idUE,
        String contextualId,
        Boolean isCapturedIndirectly,
        LocalDateTime validationDate,
        Mode mode,
        Map<String,Object> data,
        LocalDateTime recordDate,
        LocalDateTime processDate
){}
