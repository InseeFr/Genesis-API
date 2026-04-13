package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class SurveyUnitQualityToolService {

    private final SurveyUnitQualityToolPort surveyUnitQualityToolPort;
    private final DataProcessingContextService dataProcessingContextService;

    public SurveyUnitQualityToolService(
            SurveyUnitQualityToolPort surveyUnitQualityToolPort,
            DataProcessingContextService dataProcessingContextService
    ) {
        this.surveyUnitQualityToolPort = surveyUnitQualityToolPort;
        this.dataProcessingContextService = dataProcessingContextService;
    }

    /**
     * Returns the value of the 'withReview' property in the context object.
     * dataProcessingContext {@link DataProcessingContextModel}
     * @param collectionInstrumentId Passed for logging purposes.
     * @return The 'withReview' value, false if context is null.
     */
    public boolean resolveWithReviewValue(String collectionInstrumentId) {
        DataProcessingContextModel dataProcessingContext =
                dataProcessingContextService.getContextByCollectionInstrumentId(collectionInstrumentId);
        if (dataProcessingContext == null) {
            log.warn("Data processing context not found for collection instrument {}. " +
                    "Ids processed not sent to quality tool.", collectionInstrumentId);
            return false;
        }
        return dataProcessingContext.isWithReview();
    }

    public void sendProcessedIdsToQualityTool(List<SurveyUnitModel> surveyUnitModels) {
        try {
            Map<String, Set<String>> idsPerQuestionnaire = getProcessedIdsMap(surveyUnitModels);
            ResponseEntity<Object> response = surveyUnitQualityToolPort.sendProcessedIds(idsPerQuestionnaire);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent {} ids to quality tool", idsPerQuestionnaire.size());
            } else {
                log.warn("Survey unit quality tool responded non-2xx code {} and body {}",
                        response.getStatusCode(), response.getBody());
            }
        } catch (IOException e) {
            log.error("Error during Perret call request building : {}", e.toString());
        }
    }

    private static Map<String, Set<String>> getProcessedIdsMap(List<SurveyUnitModel> models) {
        Map<String, Set<String>> processedInterrogationIdsPerQuestionnaire = new HashMap<>();
        models.forEach(m -> processedInterrogationIdsPerQuestionnaire
                .computeIfAbsent(m.getCollectionInstrumentId(), k -> new HashSet<>())
                .add(m.getInterrogationId()));
        return processedInterrogationIdsPerQuestionnaire;
    }
}
