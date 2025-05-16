package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

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


    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Operation(summary = "Retrieve number of interrogations for a given questionnaire")
    @GetMapping(path = "/by-questionnaire/{questionnaireId}/count")
    public ResponseEntity<Long> countAllInterrogationIdsByQuestionnaire(
            @Parameter(description = "questionnaireId", required = true) @PathVariable("questionnaireId") String questionnaireId
    ) {
        Long response = surveyUnitService.countInterrogationIdsByQuestionnaireId(questionnaireId);
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
            @Parameter(description = "workersNumbers", required = false) @RequestParam(defaultValue = "1") int workersNumbers,
            @Parameter(description = "workerId", required = false) @RequestParam(defaultValue = "1") int workerId,
            @Parameter(description = "blockSize", required = false) @RequestParam(defaultValue = "1000") long blockSize,
            @Parameter(description = "page number", required = false) @RequestParam(defaultValue = "0") long page) {
        //TODO return blocks like "List<SurveyUnitUpdateLatest> suLatest = client.getUEsLatestState(questionnaireId, listId);"
        List<InterrogationId> responses = surveyUnitService.findDistinctPageableInterrogationIdsByQuestionnaireId(questionnaireId, totalSize, workersNumbers, workerId, blockSize, page);
        return ResponseEntity.ok(responses);
    }
    //======== OPTIMISATIONS PERFS (END) ===========


}
