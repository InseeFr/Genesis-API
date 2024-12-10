package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Builder
public record LunaticXmlDataModel(
    ObjectId id,
    Mode mode,
    LunaticXmlCampaign data,
    LocalDateTime recordDate,
    LocalDateTime processDate
){}
