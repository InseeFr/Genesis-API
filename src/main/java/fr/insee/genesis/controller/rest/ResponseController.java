package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.responses.SurveyUnitUpdateSimplified;
import fr.insee.genesis.controller.service.SurveyUnitQualityService;
import fr.insee.genesis.controller.service.VolumetryLogService;
import fr.insee.genesis.controller.sources.ddi.DDIReader;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.dtos.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.QuestionnaireWithCampaign;
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
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@RequestMapping(path = "/response")
@Controller
@Slf4j
public class ResponseController {

    private final SurveyUnitUpdateApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final VolumetryLogService volumetryLogService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;

    @Autowired
    public ResponseController(SurveyUnitUpdateApiPort surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, VolumetryLogService volumetryLogService, FileUtils fileUtils, ControllerUtils controllerUtils) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.volumetryLogService = volumetryLogService;
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

        log.info("Try to import XML data for campaign : {}", campaignName);

        try {
            List<Mode> modesList = controllerUtils.getModesList(campaignName, modeSpecified);
            for (Mode currentMode : modesList) {
                treatCampaignWithMode(campaignName, currentMode, errors);
            }
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
        return ResponseEntity.ok("Data saved");
    }

    @Operation(summary = "Save all files in Genesis Database")
    @PutMapping(path = "/save/lunatic-xml/all-campaigns")
    public ResponseEntity<Object> saveResponsesFromAllCampaignFolders(){
        List<GenesisError> errors = new ArrayList<>();
        List<File> campaignFolders = fileUtils.listAllSpecsFolders();

        if (campaignFolders.isEmpty()) {
            return ResponseEntity.ok("No campaign to save");
        }

        for (File  campaignFolder: campaignFolders) {
            String campaignName = campaignFolder.getName();
            log.info("Try to import data for campaign : {}", campaignName);

            try {
                List<Mode> modesList = controllerUtils.getModesList(campaignName, null); //modeSpecified null = all modes
                for (Mode currentMode : modesList) {
                    treatCampaignWithMode(campaignName, currentMode, errors);
                }
            } catch (Exception e) {
                log.warn("Error for campaign {} : {}", campaignName, e.toString());
                errors.add(new GenesisError(e.getMessage()));
            }
        }
        if (errors.isEmpty()) {
            return ResponseEntity.ok("Data saved");
        } else {
            return ResponseEntity.status(209).body("Data saved with " + errors.size() + " errors");
        }
    }

    @Operation(summary = "Write volumetries of each campaign in a folder")
    @PutMapping(path = "/save-volumetry/all-campaigns")
    public ResponseEntity<Object> saveVolumetry() throws IOException {
        volumetryLogService.writeVolumetries(surveyUnitService);
        volumetryLogService.cleanOldFiles();
        return ResponseEntity.ok("Volumetry saved");
    }

    @Operation(summary = "Delete all responses of one questionnaire")
    @DeleteMapping(path = "/delete-responses/by-questionnaire")
    public ResponseEntity<Object> deleteAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to delete all responses of questionnaire : {}", idQuestionnaire);
        Long ndDocuments = surveyUnitService.deleteByIdQuestionnaire(idQuestionnaire);
        log.info("{} responses deleted", ndDocuments);
        return ResponseEntity.ok(String.format("%d responses deleted", ndDocuments));
    }

    @Operation(summary = "Retrieve responses with IdUE and IdQuestionnaire from Genesis Database")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire")
    public ResponseEntity<List<SurveyUnitUpdateDto>> findResponsesByUEAndQuestionnaire(@RequestParam("idUE") String idUE,
                                                                                       @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findByIdsUEAndQuestionnaire(idUE, idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve all responses of one questionnaire")
    @GetMapping(path = "/get-responses/by-questionnaire")
    public ResponseEntity<Path> findAllResponsesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        log.info("Try to find all responses of questionnaire : {}", idQuestionnaire);

        //Get all IdUEs/modes of the survey
        List<SurveyUnitDto> idUEsResponses = surveyUnitService.findIdUEsAndModesByIdQuestionnaire(idQuestionnaire);
        log.info("Responses found : {}", idUEsResponses.size());

        String filepathString = String.format("OUT/%s/OUT_ALL_%s.json", idQuestionnaire, LocalDateTime.now().toString().replace(":", ""));
        Path filepath = Path.of(fileUtils.getDataFolderSource(), filepathString);

        try (Stream<SurveyUnitUpdateDto> responsesStream = surveyUnitService.findByIdQuestionnaire(idQuestionnaire)) {
            fileUtils.writeSuUpdatesInFile(filepath, responsesStream);
        } catch (IOException e) {
            log.error("Error while writing file", e);
            return ResponseEntity.internalServerError().body(filepath);
        }
        log.info("End of extraction, responses extracted : {}", idUEsResponses.size());
        return ResponseEntity.ok(filepath);
    }

    @Operation(summary = "Retrieve responses latest state with IdUE and IdQuestionnaire")
    @GetMapping(path = "/get-responses/by-ue-and-questionnaire/latest")
    public ResponseEntity<List<SurveyUnitUpdateDto>> getLatestByUE(@RequestParam("idUE") String idUE,
                                                                   @RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE, idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve response latest state with IdUE and IdQuestionnaire in one object in the output")
    @GetMapping(path = "/get-simplified-response/by-ue-and-questionnaire/latest")
    public ResponseEntity<SurveyUnitUpdateSimplified> getLatestByUEOneObject(@RequestParam("idUE") String idUE,
                                                                             @RequestParam("idQuestionnaire") String idQuestionnaire,
                                                                             @RequestParam("mode") Mode mode) {
        List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE, idQuestionnaire);
        List<CollectedVariableDto> outputVariables = new ArrayList<>();
        List<VariableDto> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return ResponseEntity.ok(SurveyUnitUpdateSimplified.builder()
                .idQuest(responses.getFirst().getIdQuest())
                .idCampaign(responses.getFirst().getIdCampaign())
                .idUE(responses.getFirst().getIdUE())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build());
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
            List<SurveyUnitUpdateDto> responses = surveyUnitService.findLatestByIdAndByIdQuestionnaire(idUE.getIdUE(), idQuestionnaire);
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
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Retrieve all IdUEs for a given questionnaire")
    @GetMapping(path = "/get-idUEs/by-questionnaire")
    public ResponseEntity<List<SurveyUnitId>> getAllIdUEsByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<SurveyUnitId> responses = surveyUnitService.findDistinctIdUEsByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "List sources used for a given questionnaire")
    @GetMapping(path = "/get-modes/by-questionnaire")
    public ResponseEntity<List<Mode>> getModesByQuestionnaire(@RequestParam("idQuestionnaire") String idQuestionnaire) {
        List<Mode> modes = surveyUnitService.findModesByIdQuestionnaire(idQuestionnaire);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List sources used for a given campaign")
    @GetMapping(path = "/get-modes/by-campaign")
    public ResponseEntity<List<Mode>> getModesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        List<Mode> modes = surveyUnitService.findModesByIdCampaign(idCampaign);
        return ResponseEntity.ok(modes);
    }

    @Operation(summary = "List questionnaires in database")
    @GetMapping(path = "/get-questionnaires")
    public ResponseEntity<Set<String>> getQuestionnaires() {
        Set<String> questionnaires = surveyUnitService.findDistinctIdQuestionnaires();
        return ResponseEntity.ok(questionnaires);
    }


    @Operation(summary = "List questionnaires in database with their campaigns")
    @GetMapping(path = "/get-questionnaires/with-campaigns")
    public ResponseEntity<List<QuestionnaireWithCampaign>> getQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList =
                surveyUnitService.findQuestionnairesWithCampaigns();
        return ResponseEntity.ok(questionnaireWithCampaignList);
    }

    @Operation(summary = "List questionnaires used for a given campaign")
    @GetMapping(path = "/get-questionnaires/by-campaign")
    public ResponseEntity<Set<String>> getQuestionnairesByCampaign(@RequestParam("idCampaign") String idCampaign) {
        Set<String> questionnaires = surveyUnitService.findIdQuestionnairesByIdCampaign(idCampaign);
        return ResponseEntity.ok(questionnaires);
    }

    @Operation(summary = "List campaigns in database")
    @GetMapping(path = "/get-campaigns")
    public ResponseEntity<Set<String>> getCampaigns() {
        Set<String> campaigns = surveyUnitService.findDistinctIdCampaigns();
        return ResponseEntity.ok(campaigns);
    }

    @Operation(summary = "List campaigns in database with their questionnaires")
    @GetMapping(path = "/get-campaigns/with-questionnaires")
    public ResponseEntity<List<CampaignWithQuestionnaire>> getCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> questionnairesByCampaigns = surveyUnitService.findCampaignsWithQuestionnaires();
        return ResponseEntity.ok(questionnairesByCampaigns);
    }

    //Utilities

    private void treatCampaignWithMode(String campaignName, Mode mode, List<GenesisError> errors) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        log.info("Try to import data for mode : {}", mode.getModeName());
        String dataFolder = fileUtils.getDataFolder(campaignName, mode.getFolder());
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Numbers of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            errors.add(new NoDataError("No data file found", Mode.getEnumFromModeName(mode.getModeName())));
            log.warn("No data file found in folder {}", dataFolder);
            return;
        }
        //Read DDI
        VariablesMap variablesMap;
        try {
            Path ddiFilePath = fileUtils.findDDIFile(campaignName, mode.getModeName());
            variablesMap = DDIReader.getVariablesFromDDI(ddiFilePath.toUri().toURL());
        } catch (Exception e) {
            errors.add(new GenesisError(e.toString()));
            return;
        }
        //For each XML data file
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            String filepathString = String.format("%s/%s", dataFolder, fileName);
            Path filepath = Paths.get(filepathString);
            //Check if file not in done folder, delete if true
            if(isDataFileInDoneFolder(filepath, campaignName, mode.getFolder())){
                log.warn("File {} already exists in DONE folder ! Deleting...", fileName);
                Files.deleteIfExists(filepath);
            }else{
                //Read file
                log.info("Try to read Xml file : {}", fileName);
                if (filepath.toFile().length() / 1024 / 1024 <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
                    treatXmlFileWithMemory(filepath, mode, variablesMap);
                } else {
                    treatXmlFileSequentially(filepath, mode, variablesMap);
                }
                log.debug("File {} saved", fileName);
                fileUtils.moveDataFile(campaignName, mode.getFolder(), fileName);
            }
        }
    }

    private boolean isDataFileInDoneFolder(Path filepath, String campaignName, String modeFolder) {
        return Path.of(fileUtils.getDoneFolder(campaignName, modeFolder)).resolve(filepath.getFileName()).toFile().exists();
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

        log.debug("Saving {} survey units updates", suDtos.size());
        surveyUnitService.saveSurveyUnits(suDtos);
        log.debug("Survey units updates saved");

        log.info("File {} treated with {} survey units", filepath.getFileName(), suDtos.size());
        return ResponseEntity.ok().build();
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
        return ResponseEntity.ok().build();
    }
}
