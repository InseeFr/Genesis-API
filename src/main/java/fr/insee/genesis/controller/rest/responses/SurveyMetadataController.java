package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.surveymetadata.SurveyMetadataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/metadatas" )
@Controller
@Tag(name = "Metadata services", description = "Services to interact with survey metadatas (especially variable types)")
@Slf4j
public class SurveyMetadataController {
    SurveyMetadataService surveyMetadataService;

    public SurveyMetadataController(SurveyMetadataService surveyMetadataService) {
        this.surveyMetadataService = surveyMetadataService;
    }

    @Operation(summary = "Get BPM metadatas")
    @PutMapping(path = "/get")
    public ResponseEntity<SurveyMetadataModel> getSurveyMetadata(String campaignId, String questionnaireId, Mode mode) {
        return new ResponseEntity<>(surveyMetadataService.getMetadatas(campaignId, questionnaireId, mode),
                HttpStatus.OK);
    }
}
