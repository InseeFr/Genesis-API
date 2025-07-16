package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.EditedResponseDto;
import fr.insee.genesis.domain.ports.api.EditedResponseApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/edited")
@Controller
@Slf4j
public class EditedResponseController {
    private final EditedResponseApiPort editedResponseApiPort;

    @Autowired
    public EditedResponseController(EditedResponseApiPort editedResponseApiPort) {
        this.editedResponseApiPort = editedResponseApiPort;
    }

    @Operation(summary = "Get edited variables (edited and previous)")
    @GetMapping(path = "/")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> getEditedResponses(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("interrogationId") String interrogationId
    ){
        return ResponseEntity.ok().body(
                editedResponseApiPort.getEditedResponse(questionnaireId, interrogationId)
        );
    }
}
