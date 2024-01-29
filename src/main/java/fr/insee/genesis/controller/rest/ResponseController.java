package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.responses.SurveyUnitUpdateSimplified;
import fr.insee.genesis.controller.service.SurveyUnitQualityService;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.dtos.*;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.io.FileInputStream;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/response")
@Controller
@Slf4j
public class ResponseController {

    private final SurveyUnitUpdateApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;

    @Autowired
    public ResponseController(SurveyUnitUpdateApiPort surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, FileUtils fileUtils, ControllerUtils controllerUtils) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
    }

    @Operation(summary = "Save one file of responses in Genesis Database with its path")
    @PutMapping(path = "/save/lunatic-xml/one-file")
    public ResponseEntity<Object> saveResponsesFromXmlFile(     @RequestParam("pathLunaticXml") String xmlFile,
                                                                @RequestParam("pathDDI") String ddiFile,
                                                                @RequestParam(value = "mode", required = true) Mode modeSpecified)
            throws Exception {

        log.info(String.format("Try to read DDI file : %s", ddiFile));
        Path ddiFilePath = Paths.get(ddiFile);
        VariablesMap variablesMap;
        try {
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        log.info(String.format("Try to read Xml file : %s", xmlFile));
        Path filepath = Paths.get(xmlFile);
        LunaticXmlCampaign campaign;

        if(filepath.toFile().length()/1024/1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL){
            // DOM method
            LunaticXmlDataParser parser = new LunaticXmlDataParser();
            try {
                campaign = parser.parseDataFile(filepath);
            } catch (GenesisException e) {
                return ResponseEntity.status(e.getStatus()).body(e.getMessage());
            }

            List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
            for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                suDtos.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(),modeSpecified));
            }
            surveyUnitQualityService.verifySurveyUnits(suDtos,variablesMap);

            log.info("Saving {} survey units updates", suDtos.size());
            surveyUnitService.saveSurveyUnits(suDtos);
            log.info("Survey units updates saved");

            log.info("File {} treated", xmlFile);
            return new ResponseEntity<>("Test", HttpStatus.OK);

        }else{
            //Sequential method
            log.warn("File size > " + Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL + "MB! Parsing XML file using sequential method...");
            try(final InputStream stream = new FileInputStream(filepath.toFile())){
                LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath,stream);
                int suCount = 0;

                campaign = parser.getCampaign();
                LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();

                while(su != null){
                    List<SurveyUnitUpdateDto> suDtos = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(),modeSpecified));

                    surveyUnitQualityService.verifySurveyUnits(suDtos,variablesMap);
                    surveyUnitService.saveSurveyUnits(suDtos);
                    suCount++;

                    su = parser.readNextSurveyUnit();
                }

                log.info("Saving {} survey units updates", suCount);
                log.info("Survey units updates saved");
            }

            log.info("File {} treated", xmlFile);
            return new ResponseEntity<>("Test", HttpStatus.OK);
        }
    }

    @Operation(summary = "Save multiples files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified)
            throws Exception {
        log.info("Try to import data for campaign : {}", campaignName);
        List<Mode> modesList;
        try {
            modesList = controllerUtils.getModesList(campaignName, modeSpecified);
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        List<GenesisError> errors = new ArrayList<>();

        for(Mode currentMode : modesList) {
            log.info("Try to import data for mode : {}", currentMode.getModeName());
            String dataFolder = fileUtils.getDataFolder(campaignName, currentMode.getFolder());
            List<String> dataFiles = fileUtils.listFiles(dataFolder);
            log.info("Numbers of files to load in folder {} : {}", dataFolder, dataFiles.size());
            if (dataFiles.isEmpty()) {
                errors.add(new NoDataError("No data file found",Mode.getEnumFromModeName(currentMode.getModeName())));
                log.info("No data file found in folder " + dataFolder);
            }
            VariablesMap variablesMap;
            try {
                Path ddiFilePath = fileUtils.findDDIFile(campaignName, currentMode.getModeName());
                variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
            } catch (GenesisException e) {
                return ResponseEntity.status(e.getStatus()).body(e.getMessage());
            } catch(Exception e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
            for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
                String filepathString = String.format("%s/%s", dataFolder, fileName);
                Path filepath = Paths.get(filepathString);
                log.info("Try to read Xml file : {}", fileName);

                LunaticXmlCampaign campaign;

                if(filepath.toFile().length()/1024/1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL){
                    //DOM method
                    LunaticXmlDataParser parser = new LunaticXmlDataParser();
                    campaign = parser.parseDataFile(filepath);

                    List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
                    for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                        suDtos.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(),currentMode));
                    }
                    surveyUnitQualityService.verifySurveyUnits(suDtos,variablesMap);

                    log.info("Saving {} survey units updates", suDtos.size());
                    surveyUnitService.saveSurveyUnits(suDtos);
                    log.info("Survey units updates saved");
                }else{
                    //Sequential method
                    log.warn("File size > " + Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL + "MB! Parsing XML file using sequential method...");
                    try(final InputStream stream = new FileInputStream(filepath.toFile())){
                        LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath,stream);
                        int suCount = 0;

                        campaign = parser.getCampaign();
                        LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();

                        while(su != null){
                            List<SurveyUnitUpdateDto> suDtos = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(),currentMode));

                            surveyUnitQualityService.verifySurveyUnits(suDtos,variablesMap);
                            surveyUnitService.saveSurveyUnits(suDtos);
                            suCount++;

                            su = parser.readNextSurveyUnit();
                        }

                        log.info("Saving {} survey units updates", suCount);
                        log.info("Survey units updates saved");
                    }
                }
                log.info("File {} saved", fileName);
                fileUtils.moveDataFile(campaignName, currentMode.getFolder(), fileName);
            }
        }
        return new ResponseEntity<>("Data saved", HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses with IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findResponsesByUEAndQuestionnaire(     @RequestParam("idUE") String idUE,
                                                                                            @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve all responses of one questionnaire")
    @GetMapping(path = "/get-responses/by-questionnaire")
    public ResponseEntity<Path> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : " + idQuestionnaire);
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : " + responses.size());
        String filepathString = String.format("OUT/%s/OUT_ALL_%s.json", idQuestionnaire, LocalDateTime.now().toString().replace(":",""));
        Path filepath = Paths.get(filepathString);
        fileUtils.writeFile(filepath , responses.toString());
        return new ResponseEntity<>(filepath, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses latest state with IdUE and IdQuestionnaire")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitUpdateDto>> getLatestByUE ( @RequestParam("idUE") String idUE,
                                                                                   @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve response latest state with IdUE and IdQuestionnaire in one object in the output")
    @GetMapping(path = "/get-simplified-response/by-ue-and-questionnaire/latest")
    public ResponseEntity<SurveyUnitUpdateSimplified> getLatestByUEOneObject ( @RequestParam("idUE") String idUE,
                                                                                   @RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                               @RequestParam("mode") Mode mode) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE, idQuestionnaire);
        List<CollectedVariableDto> outputVariables = new ArrayList<>();
        List<VariableDto> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep->rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return new ResponseEntity<>(SurveyUnitUpdateSimplified.builder()
                .idQuest(responses.get(0).getIdQuest())
                .idCampaign(responses.get(0).getIdCampaign())
                .idUE(responses.get(0).getIdUE())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
                description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                        "For a given id the endpoint returns a document by collection mode(if there is more than one).")
    @PostMapping(path = "/get-simplified-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitUpdateSimplified>> getLatestForUEList( @RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                          @RequestBody List<SurveyUnitId> idUEs) {
        List<SurveyUnitUpdateSimplified> results = new ArrayList<>();
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        idUEs.forEach(idUE -> {
            List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE.getIdUE(), idQuestionnaire);
            modes.forEach(mode -> {
                List<CollectedVariableDto> outputVariables = new ArrayList<>();
                List<VariableDto> outputExternalVariables = new ArrayList<>();
                responses.stream().filter(rep->rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getCollectedVariables());
                    outputExternalVariables.addAll(response.getExternalVariables());
                });
                if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
                    results.add(SurveyUnitUpdateSimplified.builder()
                            .idQuest(responses.get(0).getIdQuest())
                            .idCampaign(responses.get(0).getIdCampaign())
                            .idUE(responses.get(0).getIdUE())
                            .mode(mode)
                            .variablesUpdate(outputVariables)
                            .externalVariables(outputExternalVariables)
                            .build());
                }
            });
        });
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Operation (summary = "Retrieve all IdUEs for a given questionnaire")
    @GetMapping(path = "/get-idUEs/by-questionnaire")
    public ResponseEntity<List<SurveyUnitId>> getAllIdUEsByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitId> responses = surveyUnitService.findDistinctIdUEsByIdQuestionnaire(idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation (summary = "List sources used for a given questionnaire")
    @GetMapping(path = "/get-modes/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        return new ResponseEntity<>(modes, HttpStatus.OK);
    }
}
