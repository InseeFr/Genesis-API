package fr.insee.genesis.domain.model.surveyunit;

import java.time.Instant;

public record InterrogationInfo(
        String interrogationId,
        Instant recordDate
) {}
