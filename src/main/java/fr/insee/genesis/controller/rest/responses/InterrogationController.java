package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.InterrogationBatchResponse;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.InterrogationInfo;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

@Controller
@Slf4j
public class InterrogationController implements CommonApiResponse {

    private final SurveyUnitApiPort surveyUnitService;


    public InterrogationController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }


    @Operation(summary = "Retrieve all interrogations for a given questionnaire")
    @GetMapping(path = "interrogations/by-questionnaire")
    public ResponseEntity<List<InterrogationId>> getAllInterrogationIdsByQuestionnaire(@RequestParam("questionnaireId") String questionnaireId) {
        List<InterrogationId> responses = surveyUnitService.findDistinctInterrogationIdsByQuestionnaireId(questionnaireId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve all interrogations for a given collection instrument")
    @GetMapping(path = "collection-instruments/{collectionInstrumentId}/interrogations/all")
    public ResponseEntity<InterrogationBatchResponse> getAllInterrogationIdsByCollectionInstrumentId(
            @PathVariable String collectionInstrumentId) {
        List<InterrogationInfo> idsInfo = surveyUnitService.searchInterrogations(collectionInstrumentId, null, null);
        InterrogationBatchResponse response = buildInterrogationBatchResponse(idsInfo);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Retrieve interrogations recorded since a specified date for a given questionnaire")
    @GetMapping(path = "collection-instruments/{collectionInstrumentId}/interrogations")
    public ResponseEntity<InterrogationBatchResponse> getAllInterrogationIdsByQuestionnaire(
            @PathVariable String collectionInstrumentId,
            @Parameter(
                    description = "Filter interrogations to those recorded strictly after the given timestamp (ISO-8601 UTC format).",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T00:00:00Z")
            )
            @RequestParam(value = "since", required = false)
            Instant since,
            @RequestParam(value = "until") //FIXME Required false
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "Filter interrogations to those recorded before the given timestamp or at the same time (ISO-8601 UTC format).",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-31T23:59:59Z")
            )
            Instant until) {
        List<InterrogationInfo> idsInfo = surveyUnitService.searchInterrogations(collectionInstrumentId, since, until);
        InterrogationBatchResponse response = buildInterrogationBatchResponse(idsInfo);
        return ResponseEntity.ok(response);
    }

    private static @NonNull InterrogationBatchResponse buildInterrogationBatchResponse(List<InterrogationInfo> ids) {
        Optional<Instant> maxTimeStamp = ids.stream()
                .map(InterrogationInfo::recordDate)
                .max(Comparator.naturalOrder());
        InterrogationBatchResponse response = new InterrogationBatchResponse();
        if (maxTimeStamp.isPresent()){
            response.setInterrogationIds(ids.stream()
                    .map(InterrogationInfo::interrogationId)
                    .distinct()
                    .map(InterrogationId::new)
                    .toList());
            response.setNextSince(maxTimeStamp.get());
        }
        return response;
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     * @author Alexis Szmundy
     */
    @Operation(summary = "Retrieve number of interrogations for a given questionnaire/collection instrument")
    @GetMapping(path = "interrogations/by-questionnaire/{questionnaireId}/count")
    public ResponseEntity<Long> countAllInterrogationIdsByQuestionnaireOrCollectionInstrument(
            @Parameter(description = "questionnaireId/collectionInstrumentId", required = true) @PathVariable("questionnaireId") String questionnaireId
    ) {
        //TODO move logic to service
        long response = surveyUnitService.countResponsesByQuestionnaireId(questionnaireId);
        response += surveyUnitService.countResponsesByCollectionInstrumentId(questionnaireId);
        return ResponseEntity.ok(response);
    }


    /**
     * @author Adrien Marchal
     */
    @Operation(summary = "Retrieve paginated interrogations for a given questionnaire")
    @GetMapping(path = "interrogations/by-questionnaire/{questionnaireId}/paginated")
    public ResponseEntity<List<InterrogationId>> getPaginatedInterrogationIdsByQuestionnaire(
            @Parameter(description = "questionnaireId", required = true) @PathVariable("questionnaireId") String questionnaireId,
            @Parameter(description = "if totalSize is 0, a count query is made to get the real totalSize to process", required = false) @RequestParam(defaultValue = "0") long totalSize,
            @Parameter(description = "blockSize", required = false) @RequestParam(defaultValue = "1000") long blockSize,
            @Parameter(description = "page number / block index", required = false) @RequestParam(defaultValue = "0") long page) {
        List<InterrogationId> responses = surveyUnitService.findDistinctPageableInterrogationIdsByQuestionnaireId(questionnaireId, totalSize, blockSize, page);
        return ResponseEntity.ok(responses);
    }
    //======== OPTIMISATIONS PERFS (END) ===========


}
