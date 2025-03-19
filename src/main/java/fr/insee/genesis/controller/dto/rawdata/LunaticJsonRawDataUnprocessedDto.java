package fr.insee.genesis.controller.dto.rawdata;

import lombok.Builder;

@Builder
public record LunaticJsonRawDataUnprocessedDto(String campaignId, String interrogationId){}