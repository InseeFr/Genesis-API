package fr.insee.genesis.controller.rest;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.QuestionnaireMetadataApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping(path = "/questionnaire-metadata")
@AllArgsConstructor
public class QuestionnaireMetadataController {

    private final QuestionnaireMetadataApiPort questionnaireMetadataApiPort;

    @Operation(summary = "Get questionnaire metadata from database")
    @GetMapping("")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> getMetadata(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode
    ){
        try {
            return ResponseEntity.ok().body(questionnaireMetadataApiPort.find(questionnaireId, mode));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Save questionnaire metadata into database")
    @PostMapping("")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> saveMetadata(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode,
            @RequestBody MetadataModel body
    ) {
        questionnaireMetadataApiPort.save(questionnaireId, mode, body);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Removes questionnaire metadata from database")
    @DeleteMapping("")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteMetadata(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode
    ){
        questionnaireMetadataApiPort.remove(questionnaireId, mode);
        return ResponseEntity.ok().build();
    }
}
