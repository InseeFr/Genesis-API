package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.rest.CommonApiResponse;
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
public class ModeController implements CommonApiResponse {

    private final SurveyUnitApiPort surveyUnitService;


    public ModeController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }


    @Operation(summary = "List sources/modes used for a given collection instrument (ex questionnaire)")
    @GetMapping(path = "/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("questionnaireId") String collectionInstrumentId) {
        List<Mode> modes = surveyUnitService.findModesByCollectionInstrumentId(collectionInstrumentId);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List sources/modes used for a given campaign")
    @GetMapping(path = "/by-campaign")
    public ResponseEntity<List<Mode>> getModesByCampaign(@RequestParam("campaignId") String campaignId) {
        List<Mode> modes = surveyUnitService.findModesByCampaignId(campaignId);
        return ResponseEntity.ok(modes);
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    @Operation(summary = "List sources/modes used for a given questionnaire")
    @GetMapping(path = "/by-questionnaireV2")
    public ResponseEntity<List<Mode>> getModesByQuestionnaireV2(@RequestParam("questionnaireId") String questionnaireId) {
        List<Mode> modes = surveyUnitService.findModesByQuestionnaireIdV2(questionnaireId);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List sources/modes used for a given campaign")
    @GetMapping(path = "/by-campaignV2")
    public ResponseEntity<List<Mode>> getModesByCampaignV2(@RequestParam("campaignId") String campaignId) {
        List<Mode> modes = surveyUnitService.findModesByCampaignIdV2(campaignId);
        return ResponseEntity.ok(modes);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

}
