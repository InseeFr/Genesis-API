package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.LunaticModelApiPort;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RequestMapping(path = "/lunatic-model")
@Controller
@Slf4j
public class LunaticModelController {
    private final LunaticModelApiPort lunaticModelApiPort;
    @Autowired
    public LunaticModelController(LunaticModelApiPort lunaticModelApiPort) {
        this.lunaticModelApiPort = lunaticModelApiPort;
    }

    @Operation(summary = "Save lunatic json data from one interrogation in Genesis Database")
    @PutMapping(path = "/save")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> saveRawResponsesFromJsonBody(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestBody Map<String, Object> dataJson
    ){
        lunaticModelApiPort.save(questionnaireId, dataJson);
        return ResponseEntity.ok().build();
    }

}
