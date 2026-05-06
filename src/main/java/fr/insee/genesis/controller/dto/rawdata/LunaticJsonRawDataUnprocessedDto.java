package fr.insee.genesis.controller.dto.rawdata;

import lombok.Builder;

@Builder
public record LunaticJsonRawDataUnprocessedDto(String questionnaireId, String interrogationId){}