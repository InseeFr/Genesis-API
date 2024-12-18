package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@RequestMapping(path = "/modes" )
@Controller
@Slf4j
public class ModeController {

    private final SurveyUnitApiPort surveyUnitService;


    public ModeController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;

    }


    @Operation(summary = "List sources/modes used for a given questionnaire")
    @GetMapping(path = "/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List sources/modes used for a given campaign")
    @GetMapping(path = "/by-campaign")
    public ResponseEntity<List<Mode>> getModesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        List<Mode> modes = surveyUnitService.findModesByIdCampaign(idCampaign);
        return ResponseEntity.ok(modes);
    }

}
