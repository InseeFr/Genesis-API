package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.SurveyUnitId;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(path = "/idUEs" )
@Controller
@Tag(name = "Response services for interrogations", description = "A **response** is considered the entire set of data associated with an interrogation (idUE x idQuestionnaire). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class IdUEController {

    private final SurveyUnitApiPort surveyUnitService;

    @Autowired
    public IdUEController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }


    @Operation(summary = "Retrieve all IdUEs for a given questionnaire")
    @GetMapping(path = "/by-questionnaire")
    public ResponseEntity<List<SurveyUnitId>> getAllIdUEsByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitId> responses = surveyUnitService.findDistinctIdUEsByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(responses);
    }


}