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
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
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
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.XMLStreamException;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

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
    public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("pathLunaticXml") String xmlFile,
                                                           @RequestParam("pathDDI") String ddiFile,
                                                           @RequestParam(value = "mode") Mode modeSpecified)
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

        if (filepath.toFile().length() / 1024 / 1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
            return treatXmlFileWithMemory(filepath, modeSpecified, variablesMap);
        } else {
            return treatXmlFileSequentially(filepath, modeSpecified, variablesMap);
        }
    }

    @Operation(summary = "Save multiples files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified)
            throws Exception {
        List<GenesisError> errors = new ArrayList<>();

        log.info("Try to import data for campaign : {}", campaignName);

        try {
            List<Mode> modesList = controllerUtils.getModesList(campaignName, modeSpecified);
            for (Mode currentMode : modesList) {
                treatCampaignWithMode(campaignName, currentMode, errors);
            }
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());

        }
        return new ResponseEntity<>("Data saved", HttpStatus.OK);
    }

    @Operation(summary = "Save all files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml/all-campaigns")
    public ResponseEntity<Object> saveResponsesFromAllCampaignFolders(){
        List<GenesisError> errors = new ArrayList<>();
        List<String> campaignFolders = fileUtils.listAllSpecsFolders();

        if (campaignFolders.isEmpty()) {
            return new ResponseEntity<>("No campaign to save", HttpStatus.OK);
        }

        for (String campaignName : campaignFolders) {
            log.info("Try to import data for campaign : {}", campaignName);

            try {
                List<Mode> modesList = controllerUtils.getModesList(campaignName, null); //modeSpecified null = all modes
                for (Mode currentMode : modesList) {
                    treatCampaignWithMode(campaignName, currentMode, errors);
                }
            } catch (Exception e) {
                log.warn("Error for campaign " + campaignName + ": " + e.getMessage());
                errors.add(new GenesisError(e.getMessage()));
            }
        }
        if (errors.isEmpty()) {
            return new ResponseEntity<>("Data saved", HttpStatus.OK);
        } else {
            return new ResponseEntity<>("Data saved with " + errors.size() + " errors", HttpStatus.OK);
        }
    }

    @Operation(summary = "Delete all responses of one questionnaire")
    @DeleteMapping(path = "/delete-responses/by-questionnaire")
    public ResponseEntity<Object> deleteAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to delete all responses of questionnaire : " + idQuestionnaire);
        Long ndDocuments = surveyUnitService.deleteByIdQuestionnaire(idQuestionnaire);
        log.info("{} responses deleted", ndDocuments);
        return new ResponseEntity<>(String.format("%d responses deleted", ndDocuments), HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses with IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findResponsesByUEAndQuestionnaire(@RequestParam("idUE") String idUE,
                                                                                       @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve all responses of one questionnaire")
    @GetMapping(path = "/get-responses/by-questionnaire")
    public ResponseEntity<Path> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : " + idQuestionnaire);

        //Get all IdUEs/modes of the survey
        List<SurveyUnitDto> idUEsResponses = surveyUnitService.findIdUEsAndModesByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : {}", idUEsResponses.size());

        String filepathString = String.format("OUT/%s/OUT_ALL_%s.json", idQuestionnaire, LocalDateTime.now().toString().replace(":", ""));
        Path filepath = Path.of(fileUtils.getDataFolderSource(), filepathString);

        try (Stream<SurveyUnitUpdateDto> responsesStream = surveyUnitService.findByIdQuestionnaire(idQuestionnaire)) {
            fileUtils.writeSuUpdatesInFile(filepath, responsesStream);
        } catch (IOException e) {
            log.error("Error while writing file", e);
            return new ResponseEntity<>(filepath, HttpStatus.INTERNAL_SERVER_ERROR);
        }
        log.info("End of extraction, responses extracted : {}", idUEsResponses.size());
        return new ResponseEntity<>(filepath, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve responses latest state with IdUE and IdQuestionnaire")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitUpdateDto>> getLatestByUE(@RequestParam("idUE") String idUE,
                                                                   @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE, idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve response latest state with IdUE and IdQuestionnaire in one object in the output")
    @GetMapping(path = "/get-simplified-response/by-ue-and-questionnaire/latest")
    public ResponseEntity<SurveyUnitUpdateSimplified> getLatestByUEOneObject(@RequestParam("idUE") String idUE,
                                                                             @RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                             @RequestParam("mode") Mode mode) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE, idQuestionnaire);
        List<CollectedVariableDto> outputVariables = new ArrayList<>();
        List<VariableDto> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return new ResponseEntity<>(SurveyUnitUpdateSimplified.builder()
                .idQuest(responses.getFirst().getIdQuest())
                .idCampaign(responses.getFirst().getIdCampaign())
                .idUE(responses.getFirst().getIdUE())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build(), HttpStatus.OK);
    }


    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
            description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                    "For a given id the endpoint returns a document by collection mode(if there is more than one).")
    @PostMapping(path = "/get-simplified-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitUpdateSimplified>> getLatestForUEList(@RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                               @RequestBody List<SurveyUnitId> idUEs) {
        List<SurveyUnitUpdateSimplified> results = new ArrayList<>();
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        idUEs.forEach(idUE -> {
            List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByMode(idUE.getIdUE(), idQuestionnaire);
            modes.forEach(mode -> {
                List<CollectedVariableDto> outputVariables = new ArrayList<>();
                List<VariableDto> outputExternalVariables = new ArrayList<>();
                responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getCollectedVariables());
                    outputExternalVariables.addAll(response.getExternalVariables());
                });
                if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
                    results.add(SurveyUnitUpdateSimplified.builder()
                            .idQuest(responses.getFirst().getIdQuest())
                            .idCampaign(responses.getFirst().getIdCampaign())
                            .idUE(responses.getFirst().getIdUE())
                            .mode(mode)
                            .variablesUpdate(outputVariables)
                            .externalVariables(outputExternalVariables)
                            .build());
                }
            });
        });
        return new ResponseEntity<>(results, HttpStatus.OK);
    }

    @Operation(summary = "Retrieve all IdUEs for a given questionnaire")
    @GetMapping(path = "/get-idUEs/by-questionnaire")
    public ResponseEntity<List<SurveyUnitId>> getAllIdUEsByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitId> responses = surveyUnitService.findDistinctIdUEsByIdQuestionnaire(idQuestionnaire);
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    @Operation(summary = "List sources used for a given questionnaire")
    @GetMapping(path = "/get-modes/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        return new ResponseEntity<>(modes, HttpStatus.OK);
    }

    //Utilities

    private void treatCampaignWithMode(String campaignName, Mode mode, List<GenesisError> errors) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        log.info("Try to import data for mode : {}", mode.getModeName());
        String dataFolder = fileUtils.getDataFolder(campaignName, mode.getFolder());
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Numbers of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            errors.add(new NoDataError("No data file found", Mode.getEnumFromModeName(mode.getModeName())));
            log.info("No data file found in folder " + dataFolder);
        }
        VariablesMap variablesMap;
        try {
            Path ddiFilePath = fileUtils.findDDIFile(campaignName, mode.getModeName());
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
        } catch (Exception e) {
            errors.add(new GenesisError(e.getMessage()));
            return;
        }
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            String filepathString = String.format("%s/%s", dataFolder, fileName);
            Path filepath = Paths.get(filepathString);
            log.info("Try to read Xml file : {}", fileName);

            if (filepath.toFile().length() / 1024 / 1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
                treatXmlFileWithMemory(filepath, mode, variablesMap);
            } else {
                treatXmlFileSequentially(filepath, mode, variablesMap);
            }
            log.info("File {} saved", fileName);
            fileUtils.moveDataFile(campaignName, mode.getFolder(), fileName);
        }
    }

    private ResponseEntity<Object> treatXmlFileWithMemory(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException {
        LunaticXmlCampaign campaign;
        // DOM method
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        try {
            campaign = parser.parseDataFile(filepath);
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        List<SurveyUnitUpdateDto> suDtos = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            suDtos.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), modeSpecified));
        }
        surveyUnitQualityService.verifySurveyUnits(suDtos, variablesMap);

        log.info("Saving {} survey units updates", suDtos.size());
        surveyUnitService.saveSurveyUnits(suDtos);
        log.info("Survey units updates saved");

        log.info("File {} treated", filepath.getFileName());
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }

    private ResponseEntity<Object> treatXmlFileSequentially(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, XMLStreamException {
        LunaticXmlCampaign campaign;
        //Sequential method
        log.warn("File size > " + Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL + "MB! Parsing XML file using sequential method...");
        try (final InputStream stream = new FileInputStream(filepath.toFile())) {
            LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath, stream);
            int suCount = 0;

            campaign = parser.getCampaign();
            LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();

            while (su != null) {
                List<SurveyUnitUpdateDto> suDtos = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), modeSpecified));

                surveyUnitQualityService.verifySurveyUnits(suDtos, variablesMap);
                surveyUnitService.saveSurveyUnits(suDtos);
                suCount++;

                su = parser.readNextSurveyUnit();
            }

            log.info("Saved {} survey units updates", suCount);
        }

        log.info("File {} treated", filepath.getFileName());
        return new ResponseEntity<>("Test", HttpStatus.OK);
    }
}
