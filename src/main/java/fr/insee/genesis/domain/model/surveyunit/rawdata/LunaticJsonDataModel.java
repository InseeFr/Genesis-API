package fr.insee.genesis.domain.model.surveyunit.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import lombok.Builder;
import lombok.Data;
import org.bson.types.ObjectId;

import java.time.LocalDateTime;

@Data
@Builder
public class LunaticJsonDataModel {
    private ObjectId id;
    private String campaignId;
    private Mode mode;
    private String dataJson;
    private LocalDateTime recordDate;
    private LocalDateTime processDate;
}
