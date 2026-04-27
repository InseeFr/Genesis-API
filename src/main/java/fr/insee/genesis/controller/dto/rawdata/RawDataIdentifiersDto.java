package fr.insee.genesis.controller.dto.rawdata;

import java.util.List;

public record RawDataIdentifiersDto(
        String campaignId,
        List<RawDataIdentifierDto> interrogations
) {}
