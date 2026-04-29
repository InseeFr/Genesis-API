package fr.insee.genesis.controller.dto.rawdata;

import java.time.LocalDateTime;

public record RawDataIdentifierDto(
        String interrogationId,
        String usualSurveyUnitId,
        LocalDateTime recordDate,
        LocalDateTime processDate
) {}
