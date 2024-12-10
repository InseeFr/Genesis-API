package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@RequestMapping(path = "/questionnaires" )
@Controller
@Slf4j
public class QuestionnaireController {

    private final SurveyUnitApiPort surveyUnitService;


    public QuestionnaireController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }



    @Operation(summary = "List questionnaires in database")
    @GetMapping(path = "/")
    public ResponseEntity<Set<String>> getQuestionnaires() {
        Set<String> questionnaires = surveyUnitService.findDistinctIdQuestionnaires();
        return ResponseEntity.ok(questionnaires);
    }


    @Operation(summary = "List questionnaires in database with their campaigns")
    @GetMapping(path = "/with-campaigns")
    public ResponseEntity<List<QuestionnaireWithCampaign>> getQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList =
                surveyUnitService.findQuestionnairesWithCampaigns();
        return ResponseEntity.ok(questionnaireWithCampaignList);
    }

    @Operation(summary = "List questionnaires used for a given campaign")
    @GetMapping(path = "/by-campaign")
    public ResponseEntity<Set<String>> getQuestionnairesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        Set<String> questionnaires = surveyUnitService.findIdQuestionnairesByIdCampaign(idCampaign);
        return ResponseEntity.ok(questionnaires);
    }


}
