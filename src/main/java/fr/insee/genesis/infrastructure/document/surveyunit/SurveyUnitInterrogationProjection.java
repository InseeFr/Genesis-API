package fr.insee.genesis.infrastructure.document.surveyunit;

import java.time.Instant;

public interface SurveyUnitInterrogationProjection {

    String getInterrogationId();
    Instant getRecordDate();
}
