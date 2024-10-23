package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.adapter.ContextJsonAdapter;
import fr.insee.genesis.controller.sources.contextJson.ContextJsonFile;
import fr.insee.genesis.domain.model.context.ContextModel;
import fr.insee.genesis.domain.ports.api.ContextApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/context")
@Controller
@Slf4j
public class ContextController {

    private final ContextApiPort contextApiPort;

    @Autowired
    public ContextController(ContextApiPort contextApiPort) {
        this.contextApiPort = contextApiPort;
    }

    @Operation(summary = "Read a new survey context")
    @PostMapping(path = "/create")
    public ResponseEntity<Object> readContext(
            @RequestBody(
                    description = "JSON file containing context",
                    required = true,
                    content = @Content(mediaType = "application/json", schema = @Schema(implementation = ContextJsonFile.class))
            ) ContextJsonFile jsonFile) {
                log.info("New context received for {}",jsonFile.getId());
        ContextModel context = ContextJsonAdapter.convert(jsonFile);
        contextApiPort.addContext(context);
        return ResponseEntity.ok().build();
    }

}
