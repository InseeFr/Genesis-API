package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.model.SurveyUnitId;
import fr.insee.genesis.controller.responses.SurveyUnitUpdateSimplified;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.dtos.*;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.service.SurveyUnitVerificationService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataError;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Description;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/response")
@Controller
@Slf4j
public class ResponseController {

    private final SurveyUnitUpdateApiPort surveyUnitService;
    private final SurveyUnitVerificationService surveyUnitVerificationService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;

    @Autowired
    public ResponseController(SurveyUnitUpdateApiPort surveyUnitService,SurveyUnitVerificationService surveyUnitVerificationService, FileUtils fileUtils, ControllerUtils controllerUtils) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitVerificationService = surveyUnitVerificationService;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
    }

    @Operation(summary = "Save responses from XML Lunatic in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml/one-file")
    public ResponseEntity<Object> saveResponsesFromXmlFile(     @RequestParam("pathLunaticXml") String xmlFile,
                                                                @RequestParam("pathDDI") String ddiFile)
            throws Exception {
        log.info(String.format("Try to read Xml file : %s", xmlFile));
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        LunaticXmlCampaign campaign;
        try {
            campaign = parser.parseDataFile(Paths.get(xmlFile));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        log.info(String.format("Try to read DDI file : %s", ddiFile));
        Path ddiFilePath = Paths.get(ddiFile);
        VariablesMap variablesMap;
        try {
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign());
            suDtos.add(suDto);
        }
        surveyUnitVerificationService.verifySurveyUnits(suDtos,variablesMap);
        surveyUnitService.saveSurveyUnits(suDtos);
        log.info("File {} treated", xmlFile);
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    @Operation(summary = "Save multiples files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified)
            throws Exception {
        log.info("Try to import data for campaign : {}", campaignName);
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
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
            for (String fileName : dataFiles) {
                String pathFile = String.format("%s/%s", dataFolder, fileName);
                log.info("Try to read Xml file : {}", fileName);
                LunaticXmlCampaign campaign = parser.parseDataFile(Paths.get(pathFile));
                VariablesMap variablesMap;
                try {
                    Path ddiFilePath = fileUtils.findDDIFile(campaignName, currentMode.getModeName());
                    variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
                } catch (GenesisException e) {
                    return ResponseEntity.status(e.getStatus()).body(e.getMessage());
                } catch(Exception e) {
                    return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
                }
                List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
                for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                    SurveyUnitUpdateDto suDto = LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign());
                    suDtos.add(suDto);
                }
                surveyUnitService.saveSurveyUnits(suDtos);
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
    public ResponseEntity<List<SurveyUnitUpdateDto>> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : " + idQuestionnaire);
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : " + responses.size());
        return new ResponseEntity<>(responses, HttpStatus.OK);
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
        List<VariableStateDto> outputVariables = new ArrayList<>();
        List<ExternalVariableDto> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep->rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getVariablesUpdate());
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
                List<VariableStateDto> outputVariables = new ArrayList<>();
                List<ExternalVariableDto> outputExternalVariables = new ArrayList<>();
                responses.stream().filter(rep->rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getVariablesUpdate());
                    outputExternalVariables.addAll(response.getExternalVariables());
                });
                results.add(SurveyUnitUpdateSimplified.builder()
                        .idQuest(responses.get(0).getIdQuest())
                        .idCampaign(responses.get(0).getIdCampaign())
                        .idUE(responses.get(0).getIdUE())
                        .mode(mode)
                        .variablesUpdate(outputVariables)
                        .externalVariables(outputExternalVariables)
                        .build());
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
