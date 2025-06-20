package fr.insee.genesis.domain.ports.spi;

import org.springframework.http.ResponseEntity;

import java.util.Map;
import java.util.Set;

public interface SurveyUnitQualityToolPort {
    ResponseEntity<Object> sendProcessedIds(Map<String, Set<String>> processedIdsMap);
}
