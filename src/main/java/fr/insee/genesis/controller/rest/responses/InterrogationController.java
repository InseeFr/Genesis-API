package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(path = "/interrogations" )
@Controller
@Slf4j
public class InterrogationController implements CommonApiResponse {

    private final SurveyUnitApiPort surveyUnitService;


    public InterrogationController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }


    @Operation(summary = "Retrieve all interrogations for a given questionnaire")
    @GetMapping(path = "/by-questionnaire")
    public ResponseEntity<List<InterrogationId>> getAllInterrogationIdsByQuestionnaire(@RequestParam("questionnaireId") String questionnaireId) {
        List<InterrogationId> responses = surveyUnitService.findDistinctInterrogationIdsByQuestionnaireId(questionnaireId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve interrogations recorded since a specified date for a given questionnaire")
    @GetMapping(path = "/by-questionnaire-and-since-datetime")
    public ResponseEntity<List<InterrogationId>> getAllInterrogationIdsByQuestionnaire(
            @RequestParam("questionnaireId") String questionnaireId,
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime since) {
        List<InterrogationId> responses = surveyUnitService.findDistinctInterrogationIdsByQuestionnaireIdAndDateAfter(questionnaireId, since);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve interrogations recorded between two dates for a given collection instrument")
    @GetMapping(path = "/by-collection-instrument-and-between-datetime")
    public ResponseEntity<List<InterrogationId>> getAllInterrogationIdsByCollectionInstrumentIdAndDate(
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestParam("start")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "sinceDate",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T00:00:00")
            )
            LocalDateTime start,

            @RequestParam("end")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            @Parameter(
                    description = "untilDate",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-31T23:59:59")
            )
            LocalDateTime end) {
        List<InterrogationId> responses = surveyUnitService.findDistinctInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(collectionInstrumentId, start,end);
        return ResponseEntity.ok(responses);
    }



    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     * @author Alexis Szmundy
     */
    @Operation(summary = "Retrieve number of interrogations for a given questionnaire/collection instrument")
    @GetMapping(path = "/by-questionnaire/{questionnaireId}/count")
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
    @GetMapping(path = "/by-questionnaire/{questionnaireId}/paginated")
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
