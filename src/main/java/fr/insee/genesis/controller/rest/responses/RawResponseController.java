package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@RequestMapping(path = "/responses/raw" )
@Controller
@Tag(name = "Response services")
@Slf4j
public class RawResponseController {


    private static final String SUCCESS_MESSAGE = "Data saved";
    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;


    public RawResponseController(LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort) {
        this.lunaticJsonRawDataApiPort = lunaticJsonRawDataApiPort;
    }

    //SAVE
    @Operation(summary = "Save lunatic json data to Genesis Database from the campaign name")
    @PutMapping(path = "lunatic-json/save")
    public ResponseEntity<Object> saveRawResponsesFromJsonBody(
            @RequestParam("campaignName") String campaignName,
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam(value = "surveyUnitId", required = false) String idUE,
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam(value = "mode", required = false) Mode modeSpecified,
            @RequestBody String dataJson
    ) {
        log.info("Try to import raw lunatic JSON data for campaign: {}", campaignName);
        try {
            lunaticJsonRawDataApiPort.saveData(campaignName, interrogationId, idUE, questionnaireId, modeSpecified, dataJson);
        }catch (JsonProcessingException jpe ){
            log.error(jpe.toString());
            return ResponseEntity.badRequest().body("Invalid JSON syntax");
        }
        log.info("Data saved for {}", campaignName);
        return ResponseEntity.ok(SUCCESS_MESSAGE);
    }

}
