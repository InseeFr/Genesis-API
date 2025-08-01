package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.QuestionnaireMetadataApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@AllArgsConstructor
public class QuestionnaireMetadataController {

    private final QuestionnaireMetadataApiPort questionnaireMetadataApiPort;

    @Operation(summary = "Removes questionnaire metadata from database")
    @DeleteMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteMetadata(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode
    ){
        questionnaireMetadataApiPort.remove(questionnaireId, mode);
        return ResponseEntity.ok().build();
    }
}
