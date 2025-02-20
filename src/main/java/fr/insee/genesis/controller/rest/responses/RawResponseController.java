package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
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

    private static final String DDI_REGEX = "ddi[\\w,\\s-]+\\.xml";
    public static final String S_S = "%s/%s";
    private static final String CAMPAIGN_ERROR = "Error for campaign {}: {}";
    private static final String SUCCESS_MESSAGE = "Data saved";
    private static final String SUCCESS_NO_DATA_MESSAGE = "No data has been saved";
    public static final String TRY_TO_READ_XML_FILE = "Try to read Xml file : {}";
    private final SurveyUnitApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;
    private final AuthUtils authUtils;
    private final MetadataService metadataService;


    public RawResponseController(SurveyUnitApiPort surveyUnitService,
                                 SurveyUnitQualityService surveyUnitQualityService,
                                 LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort,
                                 FileUtils fileUtils,
                                 ControllerUtils controllerUtils,
                                 AuthUtils authUtils,
                                 MetadataService metadataService
    ) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.lunaticJsonRawDataApiPort = lunaticJsonRawDataApiPort;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
        this.authUtils = authUtils;
        this.metadataService = metadataService;
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
