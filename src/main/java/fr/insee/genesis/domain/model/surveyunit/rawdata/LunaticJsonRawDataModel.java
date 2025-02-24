package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;


@Builder
public record LunaticJsonRawDataModel(
    ObjectId id,
    String campaignId,
    String questionnaireId,
    String interrogationId,
    Mode mode,
    LunaticJsonRawData data,
    LocalDateTime recordDate,
    LocalDateTime processDate
){}
