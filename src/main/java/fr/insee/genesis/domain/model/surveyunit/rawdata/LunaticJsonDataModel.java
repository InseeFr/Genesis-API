package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;


@Builder
public record LunaticJsonDataModel(
    ObjectId id,
    String campaignId,
    String idQuest,
    String idUE,
    Mode mode,
    String dataJson,
    LocalDateTime recordDate,
    LocalDateTime processDate
){}
