package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;


@Builder
public record LunaticJsonDataModel(
    ObjectId id,
    String campaignId,
    String interrogationId,
    String idUE,
    String questionnaireId,
    Mode mode,
    String dataJson,
    LocalDateTime recordDate,
    LocalDateTime processDate
){}
