package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
@Builder
public class LunaticXmlDataModel{
    private ObjectId id;
    private Mode mode;
    private LunaticXmlCampaign data;
    private LocalDateTime recordDate;
}
