package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.api.LunaticModelApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

@RequestMapping(path = "/lunatic-model")
@Controller
@Slf4j
public class LunaticModelController implements CommonApiResponse{
    private final LunaticModelApiPort lunaticModelApiPort;


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

    @Operation(summary = "Get lunatic model from one interrogation in Genesis Database")
    @GetMapping(path = "/get")
    @PreAuthorize("hasRole('READER')")
    public ResponseEntity<String> getLunaticModelFromQuestionnaireId(
            @RequestParam("questionnaireId") String questionnaireId
    ) throws JsonProcessingException {
        try {
            LunaticModelModel lunaticModelModel = lunaticModelApiPort.get(questionnaireId);
            ObjectMapper objectMapper = new ObjectMapper();
            return ResponseEntity.ok(objectMapper.writeValueAsString(lunaticModelModel.lunaticModel()));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
}
