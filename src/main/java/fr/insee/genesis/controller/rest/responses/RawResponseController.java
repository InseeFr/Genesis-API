package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import io.swagger.v3.oas.annotations.Hidden;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Controller
@RequestMapping(path = "/responses/raw" )
public class RawResponseController {

    private static final String SUCCESS_MESSAGE = "Interrogation %s saved";
    private final LunaticJsonRawDataService lunaticJsonRawDataApiPort;
//    private final ControllerUtils controllerUtils;
//    private final MetadataService metadataService;
//    private final SurveyUnitService surveyUnitService;
//    private final SurveyUnitQualityService surveyUnitQualityService;
//    private final FileUtils fileUtils;
    
   /* public RawResponseController(LunaticJsonRawDataService lunaticJsonRawDataApiPort, ControllerUtils controllerUtils, MetadataService metadataService, SurveyUnitService surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, FileUtils fileUtils) {
        this.lunaticJsonRawDataApiPort = lunaticJsonRawDataApiPort;
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
    }*/

    public RawResponseController(LunaticJsonRawDataService lunaticJsonRawDataApiPort) {
        this.lunaticJsonRawDataApiPort = lunaticJsonRawDataApiPort;
    }

    @Operation(summary = "Save lunatic json data from one interrogation in Genesis Database")
    @PutMapping(path = "/lunatic-json/save")
    @PreAuthorize("hasRole('COLLECT_PLATFORM')")
    public ResponseEntity<String> saveRawResponsesFromJsonBody(

            @RequestParam("campaignName") String campaignName,
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam(value = "surveyUnitId", required = false) String idUE,
            @RequestParam(value = "mode") Mode modeSpecified,
            @RequestBody Map<String, Object> dataJson
    ) {
        log.info("Try to save interrogationId {} for campaign {}",interrogationId,campaignName);
        LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                .campaignId(campaignName)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .idUE(idUE)
                .mode(modeSpecified)
                .data(dataJson)
                .recordDate(LocalDateTime.now())
                .build();
        try {
            lunaticJsonRawDataApiPort.save(rawData);
        } catch (Exception e){
            return ResponseEntity.status(500).body("Unexpected error");
        }
        log.info("Data saved for interrogationId {} and campaign {}",interrogationId, campaignName);
        // Collect platform prefer code 201 in case of success
        return ResponseEntity.status(201).body(String.format(SUCCESS_MESSAGE,interrogationId));
    }

    //GET unprocessed
    @Operation(summary = "Get campaign id and interrogationId from all unprocessed raw json data")
    @GetMapping(path = "/lunatic-json/get/unprocessed")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<List<LunaticJsonRawDataUnprocessedDto>> getUnproccessedJsonRawData(){
        log.info("Try to get unprocessed raw JSON datas...");
        return ResponseEntity.ok(lunaticJsonRawDataApiPort.getUnprocessedDataIds());
    }

    @Hidden
    @GetMapping(path= "/lunatic-json/get/by-interrogation-mode-and-campaign")
    @PreAuthorize("hasRole('ADMIN')")
            public ResponseEntity<LunaticJsonRawDataModel> getJsonRawData(
                @RequestParam("interrogationId") String interrogationId,
                @RequestParam("campaignName") String campaignName,
                @RequestParam(value = "mode") Mode modeSpecified
            ){
        List<LunaticJsonRawDataModel> data = lunaticJsonRawDataApiPort.getRawData(campaignName,modeSpecified,List.of(interrogationId));
        return ResponseEntity.ok(data.getFirst());
    }

    //PROCESS
  /*  @Operation(summary = "Process raw data of a campaign")
    @PostMapping(path = "/lunatic-json/process")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> processJsonRawData(
            @RequestParam("campaignName") String campaignName,
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestBody List<String> interrogationIdList
    ){
        log.info("Try to process raw JSON datas for campaign {} and {} interrogationIds", campaignName, interrogationIdList.size());

        int dataCount = 0;
        int forcedDataCount = 0;
        List<GenesisError> errors = new ArrayList<>();

        try {
            List<Mode> modesList = controllerUtils.getModesList(campaignName, null);
            for (Mode mode : modesList) {
                //Load and save metadatas into database, throw exception if none
                VariablesMap variablesMap = metadataService.readMetadatas(campaignName, mode.getModeName(), fileUtils,
                        errors);
                if (variablesMap == null) {
                    throw new GenesisException(400,
                            "Error during metadata parsing for mode %s :%n%s"
                                    .formatted(mode, errors.getLast().getMessage())
                    );
                }

                List<LunaticJsonRawDataModel> rawData = lunaticJsonRawDataApiPort.getRawData(campaignName,mode,interrogationIdList);
                //Save converted data
                List<SurveyUnitModel> surveyUnitModels = lunaticJsonRawDataApiPort.convertRawData(
                        rawData,
                        variablesMap
                );

                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                surveyUnitService.saveSurveyUnits(surveyUnitModels);

                //Update process dates
                lunaticJsonRawDataApiPort.updateProcessDates(surveyUnitModels);

                //Save metadatas
                //TODO Enable when mapping problem solved for get metadatas step
                //variableTypeApiPort.saveMetadatas(campaignName, questionnaireId, mode, variablesMap);

                //Increment data count
                dataCount += surveyUnitModels.size();
                forcedDataCount += surveyUnitModels.stream().filter(
                        surveyUnitModel -> surveyUnitModel.getState().equals(DataState.FORCED)
                ).toList().size();
            }
            return forcedDataCount == 0 ?
                    ResponseEntity.ok("%d document(s) processed".formatted(dataCount))
                    : ResponseEntity.ok("%d document(s) processed, including %d FORCED after data verification"
                    .formatted(dataCount, forcedDataCount));
        }catch (GenesisException e){ //TODO replace with spring exception handler
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }
*/
}
