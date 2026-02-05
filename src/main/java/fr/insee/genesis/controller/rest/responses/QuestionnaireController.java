package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;
import java.util.Set;

@RequestMapping(path = "/questionnaires" )
@Controller
@Slf4j
public class QuestionnaireController implements CommonApiResponse {

    private final SurveyUnitApiPort surveyUnitService;
    private final DataProcessingContextApiPort dataProcessingContextService;


    public QuestionnaireController(
            SurveyUnitApiPort surveyUnitService,
            DataProcessingContextApiPort dataProcessingContextService
    ) {
        this.surveyUnitService = surveyUnitService;
        this.dataProcessingContextService = dataProcessingContextService;
    }



    @Operation(summary = "List questionnaires/collection instruments from responses database")
    @GetMapping(path = "/")
    public ResponseEntity<Set<String>> getQuestionnaires() {
        Set<String> questionnaires = surveyUnitService.findDistinctQuestionnairesAndCollectionInstrumentIds();
        return ResponseEntity.ok(questionnaires);
    }

    @Operation(summary = "List questionnaires in database that have a context with a specific withReview")
    @GetMapping(path = "/with-review")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<List<String>> getQuestionnairesWithReview(
            @RequestParam(value = "withReview") boolean withReview
    ) {
        List<String> questionnaires = dataProcessingContextService.getPartitionIds(withReview);
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
    public ResponseEntity<Set<String>> getQuestionnairesByCampaign(@RequestParam("campaignId") String campaignId) {
        Set<String> questionnaires = surveyUnitService.findQuestionnaireIdsByCampaignId(campaignId);
        return ResponseEntity.ok(questionnaires);
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Operation(summary = "List questionnaires used for a given campaign (using a DISTINCT query)")
    @GetMapping(path = "/by-campaignV2")
    public ResponseEntity<Set<String>> getQuestionnairesByCampaignV2(@RequestParam("campaignId") String campaignId) {
        Set<String> questionnaires = surveyUnitService.findQuestionnaireIdsByCampaignIdV2(campaignId);
        return ResponseEntity.ok(questionnaires);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

    @Operation(summary = "Get the questionnaireId corresponding to an interrogationId")
    @GetMapping(path = "/by-interrogation")
    @ApiResponse(responseCode = "200", description = "Successfully retrieved the questionnaireId")
    public ResponseEntity<String> getQuestionnaireByInterrogation(@RequestParam("interrogationId") String interrogationId){
        try {
            String questionnaireId = surveyUnitService.findQuestionnaireIdByInterrogationId(interrogationId);
            return ResponseEntity.ok(questionnaireId);
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

    }


}
