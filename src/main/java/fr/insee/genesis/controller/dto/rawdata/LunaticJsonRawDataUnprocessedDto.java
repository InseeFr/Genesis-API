package fr.insee.genesis.controller.dto.rawdata;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class LunaticJsonRawDataUnprocessedDto {
    private String campaignId;
    private String idUE;
}
