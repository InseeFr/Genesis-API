package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Set;

@RequestMapping(path = "/campaigns" )
@Controller
@Tag(name = "Response services for interrogations", description = "A **response** is considered the entire set of data associated with an interrogation (idUE x idQuestionnaire). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class CampaignController {

    private final SurveyUnitApiPort surveyUnitService;

    @Autowired
    public CampaignController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }

    @Operation(summary = "List campaigns in database")
    @GetMapping(path = "/")
    public ResponseEntity<Set<String>> getCampaigns() {
        Set<String> campaigns = surveyUnitService.findDistinctIdCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    @Operation(summary = "List campaigns in database with their questionnaires")
    @GetMapping(path = "/with-questionnaires")
    public ResponseEntity<List<CampaignWithQuestionnaire>> getCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> questionnairesByCampaigns = surveyUnitService.findCampaignsWithQuestionnaires();
        return ResponseEntity.ok(questionnairesByCampaigns);
    }


}