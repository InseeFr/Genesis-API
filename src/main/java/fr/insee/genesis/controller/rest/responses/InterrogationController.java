package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(path = "/interrogations" )
@Controller
@Slf4j
public class InterrogationController {

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


}
