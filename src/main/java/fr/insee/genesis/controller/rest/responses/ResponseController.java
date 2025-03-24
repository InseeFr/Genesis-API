package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.controller.utils.DataTransformer;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
import java.util.stream.Stream;

@RequestMapping(path = "/responses" )
@Controller
@Tag(name = "Response services", description = "A **response** is considered the entire set of data associated with an interrogation (survey unit x questionnaireId). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class ResponseController {
    public static final String PATH_FORMAT = "%s/%s";
    private static final String CAMPAIGN_ERROR = "Error for campaign {}: {}";
    private static final String SUCCESS_MESSAGE = "Data saved";
    private static final String SUCCESS_NO_DATA_MESSAGE = "No data has been saved";
    private final SurveyUnitApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;
    private final AuthUtils authUtils;
    private final MetadataService metadataService;


    public ResponseController(SurveyUnitApiPort surveyUnitService,
                              SurveyUnitQualityService surveyUnitQualityService,
                              FileUtils fileUtils,
                              ControllerUtils controllerUtils,
                              AuthUtils authUtils,
                              MetadataService metadataService
    ) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
        this.authUtils = authUtils;
        this.metadataService = metadataService;
    }

    //SAVE
    @Operation(summary = "Save one file of responses to Genesis Database, passing its path as a parameter")
    @PutMapping(path = "/lunatic-xml/save-one")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("pathLunaticXml") String xmlFile,
                                                           @RequestParam(value = "pathSpecFile") String metadataFilePath,
                                                           @RequestParam(value = "mode") Mode modeSpecified,
                                                           @RequestParam(value = "withDDI", defaultValue = "true") boolean withDDI
    )throws Exception {
        VariablesMap variablesMap;
        if(withDDI) {
            //Parse DDI
            log.info(String.format("Try to read DDI file : %s", metadataFilePath));
            try {
                variablesMap =
                        DDIReader.getMetadataFromDDI(Path.of(metadataFilePath).toFile().toURI().toURL().toString(),
                                new FileInputStream(metadataFilePath)).getVariables();
            } catch (MetadataParserException e) {
                return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
            }
        }else{
            //Parse Lunatic
            log.info(String.format("Try to read lunatic file : %s", metadataFilePath));

            variablesMap = LunaticReader.getMetadataFromLunatic(new FileInputStream(metadataFilePath)).getVariables();
        }

        log.info(String.format("Try to read Xml file : %s", xmlFile));
        Path filepath = Paths.get(xmlFile);

        if (getFileSizeInMB(filepath) <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
            return processXmlFileWithMemory(filepath, modeSpecified, variablesMap);
        }
        return processXmlFileSequentially(filepath, modeSpecified, variablesMap);
    }

    @Operation(summary = "Save multiple files to Genesis Database from the campaign root folder")
    @PutMapping(path = "/lunatic-xml/save-folder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified
    )throws Exception {
        List<GenesisError> errors = new ArrayList<>();
        boolean isAnyDataSaved = false;

        log.info("Try to import XML data for campaign: {}", campaignName);

        List<Mode> modesList = controllerUtils.getModesList(campaignName, modeSpecified);
        for (Mode currentMode : modesList) {
            try {
                processCampaignWithMode(campaignName, currentMode, errors, null);
                isAnyDataSaved = true;
            }catch (NoDataException nde){
                //Don't stop if NoDataError thrown
                log.warn(nde.getMessage());
            }catch (Exception e){
                log.error(CAMPAIGN_ERROR, campaignName, e.toString());
                return ResponseEntity.status(500).body(e.getMessage());
            }
        }

        if (errors.isEmpty()){
            return ResponseEntity.ok(getSuccessMessage(isAnyDataSaved));
        }
        return ResponseEntity.internalServerError().body(errors.getFirst().getMessage());
    }

    //SAVE ALL
    @Operation(summary = "Save all files to Genesis Database (differential data folder only), regardless of the campaign")
    @PutMapping(path = "/lunatic-xml/save-all-campaigns")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> saveResponsesFromAllCampaignFolders(){
        List<GenesisError> errors = new ArrayList<>();
        List<File> campaignFolders = fileUtils.listAllSpecsFolders();

        if (campaignFolders.isEmpty()) {
            return ResponseEntity.ok("No campaign to save");
        }

        for (File  campaignFolder: campaignFolders) {
            String campaignName = campaignFolder.getName();
            log.info("Try to import data for campaign: {}", campaignName);

            try {
                List<Mode> modesList = controllerUtils.getModesList(campaignName, null); //modeSpecified null = all modes
                for (Mode currentMode : modesList) {
                    processCampaignWithMode(campaignName, currentMode, errors, Constants.DIFFERENTIAL_DATA_FOLDER_NAME);
                }
            }catch (NoDataException nde){
                log.warn(nde.getMessage());
            }
            catch (Exception e) {
                log.warn(CAMPAIGN_ERROR, campaignName, e.toString());
                errors.add(new GenesisError(e.getMessage()));
            }
        }
        if (errors.isEmpty()) {
            return ResponseEntity.ok(SUCCESS_MESSAGE);
        }
        return ResponseEntity.status(209).body("Data saved with " + errors.size() + " errors");
    }

    
    //DELETE
    @Operation(summary = "Delete all responses associated with a questionnaire")
    @DeleteMapping(path = "/delete/by-questionnaire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteAllResponsesByQuestionnaire(@RequestParam("questionnaireId") String questionnaireId) {
        log.info("Try to delete all responses of questionnaire : {}", questionnaireId);
        Long ndDocuments = surveyUnitService.deleteByQuestionnaireId(questionnaireId);
        log.info("{} responses deleted", ndDocuments);
        return ResponseEntity.ok(String.format("%d responses deleted", ndDocuments));
    }

    //GET
    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and questionnaireId from Genesis Database")
    @GetMapping(path = "/by-ue-and-questionnaire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyUnitModel>> findResponsesByInterrogationAndQuestionnaire(@RequestParam("interrogationId") String interrogationId,
                                                                                   @RequestParam("questionnaireId") String questionnaireId) {
        List<SurveyUnitModel> responses = surveyUnitService.findByIdsInterrogationAndQuestionnaire(interrogationId, questionnaireId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and questionnaireId from Genesis Database with the latest value for each available state of every variable")
    @GetMapping(path = "/by-ue-and-questionnaire/latest-states")
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<SurveyUnitQualityToolDto> findResponsesByInterrogationAndQuestionnaireLatestStates(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("questionnaireId") String questionnaireId) {
        SurveyUnitDto response = surveyUnitService.findLatestValuesByStateByIdAndByQuestionnaireId(interrogationId, questionnaireId);
        SurveyUnitQualityToolDto responseQualityTool = DataTransformer.transformSurveyUnitDto(response);
        return ResponseEntity.ok(responseQualityTool);
    }

    @Operation(summary = "Retrieve all responses (for all interrogations) of one questionnaire")
    @GetMapping(path = "/by-questionnaire")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Path> findAllResponsesByQuestionnaire(@RequestParam("questionnaireId") String questionnaireId) {
        log.info("Try to find all responses of questionnaire : {}", questionnaireId);

        //Get all interrogationIds/modes of the survey
        List<SurveyUnitModel> interrogationIdsResponses = surveyUnitService.findInterrogationIdsAndModesByQuestionnaireId(questionnaireId);
        log.info("Responses found : {}", interrogationIdsResponses.size());

        String filepathString = String.format("OUT/%s/OUT_ALL_%s.json", questionnaireId, LocalDateTime.now().toString().replace(":", ""));
        Path filepath = Path.of(fileUtils.getDataFolderSource(), filepathString);

        try (Stream<SurveyUnitModel> responsesStream = surveyUnitService.findByQuestionnaireId(questionnaireId)) {
            fileUtils.writeSuUpdatesInFile(filepath, responsesStream);
        } catch (IOException e) {
            log.error("Error while writing file", e);
            return ResponseEntity.internalServerError().body(filepath);
        }
        log.info("End of extraction, responses extracted: {}", interrogationIdsResponses.size());
        return ResponseEntity.ok(filepath);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and questionnaireId from Genesis Database. It returns only the latest value of each variable regardless of the state.")
    @GetMapping(path = "/by-ue-and-questionnaire/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyUnitModel>> getLatestByInterrogation(@RequestParam("interrogationId") String interrogationId,
                                                               @RequestParam("questionnaireId") String questionnaireId) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByQuestionnaireId(interrogationId, questionnaireId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and questionnaireId from Genesis Database. For a given mode, it returns only the latest value of each variable regardless of the state. The result is one object by mode in the output")
    @GetMapping(path = "/simplified/by-ue-questionnaire-and-mode/latest")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<SurveyUnitSimplified> getLatestByInterrogationOneObject(@RequestParam("interrogationId") String interrogationId,
                                                                             @RequestParam("questionnaireId") String questionnaireId,
                                                                             @RequestParam("mode") Mode mode) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByQuestionnaireId(interrogationId, questionnaireId);
        List<VariableModel> outputVariables = new ArrayList<>();
        List<VariableModel> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return ResponseEntity.ok(SurveyUnitSimplified.builder()
                .questionnaireId(responses.getFirst().getQuestionnaireId())
                .campaignId(responses.getFirst().getCampaignId())
                .interrogationId(responses.getFirst().getInterrogationId())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build());
    }


    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
            description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                    "For a given id, the endpoint returns a document by collection mode (if there is more than one).")
    @PostMapping(path = "/simplified/by-list-interrogation-and-questionnaire/latest")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<List<SurveyUnitSimplified>> getLatestForInterrogationList(@RequestParam("questionnaireId") String questionnaireId,
                                                                               @RequestBody List<InterrogationId> interrogationIds) {
        List<SurveyUnitSimplified> results = new ArrayList<>();
        List<Mode> modes = surveyUnitService.findModesByQuestionnaireId(questionnaireId);
        interrogationIds.forEach(interrogationId -> {
            List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByQuestionnaireId(interrogationId.getInterrogationId(), questionnaireId);
            modes.forEach(mode -> {
                List<VariableModel> outputVariables = new ArrayList<>();
                List<VariableModel> outputExternalVariables = new ArrayList<>();
                responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getCollectedVariables());
                    outputExternalVariables.addAll(response.getExternalVariables());
                });
                if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
                    results.add(SurveyUnitSimplified.builder()
                            .questionnaireId(responses.getFirst().getQuestionnaireId())
                            .campaignId(responses.getFirst().getCampaignId())
                            .interrogationId(responses.getFirst().getInterrogationId())
                            .mode(mode)
                            .variablesUpdate(outputVariables)
                            .externalVariables(outputExternalVariables)
                            .build());
                }
            });
        });
        return ResponseEntity.ok(results);
    }

    @Operation(summary = "Save edited variables",
            description = "Save edited variables document into database")
    @PostMapping(path = "/save-edited")
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<Object> saveEditedVariables(
            @RequestBody SurveyUnitInputDto surveyUnitInputDto
    ){
        //Parse metadata
        //Try to look for DDI first, if no DDI found looks for lunatic components
        List<GenesisError> errors = new ArrayList<>();
        VariablesMap variablesMap = metadataService.readMetadatas(surveyUnitInputDto.getCampaignId(),
                surveyUnitInputDto.getMode().getModeName(), fileUtils, errors);
        if(variablesMap == null){
            log.warn("Can't find DDI, trying with lunatic...");
            variablesMap = metadataService.readMetadatas(surveyUnitInputDto.getCampaignId(),
                    surveyUnitInputDto.getMode().getModeName(), fileUtils, errors);
            if(variablesMap == null){
                return ResponseEntity.status(404).body(errors.getLast().getMessage());
            }
        }

        //Check if input edited variables are in metadatas
        List<String> absentCollectedVariableNames =
                surveyUnitQualityService.checkVariablesPresentInMetadata(surveyUnitInputDto.getCollectedVariables(),
                variablesMap);
        if (!absentCollectedVariableNames.isEmpty()) {
            String absentVariables = String.join("\n", absentCollectedVariableNames);
            return ResponseEntity.badRequest().body(
                    String.format("The following variables are absent in metadatas : %n%s", absentVariables)
            );
        }

        //Fetch user identifier from OIDC token
        String userIdentifier = authUtils.getIDEP();


        //Create surveyUnitModel for each STATE received (Quality tool could send variables with another STATE than EDITED)
        List<SurveyUnitModel> surveyUnitModels;
        try{
            surveyUnitModels = surveyUnitService.parseEditedVariables(
                    surveyUnitInputDto,
                    userIdentifier,
                    variablesMap
            );
        }catch (GenesisException e){
           return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        //Check data with dataverifier (might create a FORCED document)
        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

        //Save documents
        surveyUnitService.saveSurveyUnits(surveyUnitModels);
        return ResponseEntity.ok(SUCCESS_MESSAGE);
    }

    //Utilities
    /**
     * Process a campaign with a specific mode
     * @param campaignName name of campaign
     * @param mode mode of collected data
     * @param errors error list to fill
     */
    private void processCampaignWithMode(String campaignName, Mode mode, List<GenesisError> errors, String rootDataFolder)
            throws IOException, ParserConfigurationException, SAXException, XMLStreamException, NoDataException {
        log.info("Starting data import for mode: {}", mode.getModeName());
        String dataFolder = rootDataFolder == null ?
                fileUtils.getDataFolder(campaignName, mode.getFolder(), null)
                : fileUtils.getDataFolder(campaignName, mode.getFolder(), rootDataFolder);
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Number of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            throw new NoDataException("No data file found in folder %s".formatted(dataFolder));
        }

        VariablesMap variablesMap = metadataService.readMetadatas(campaignName, mode.getModeName(), fileUtils, errors);
        if (variablesMap == null){
            return;
        }

        //For each XML data file
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            processOneXmlFileForCampaign(campaignName, mode, fileName, dataFolder, variablesMap);
        }
    }

    private void processOneXmlFileForCampaign(String campaignName,
                                              Mode mode,
                                              String fileName,
                                              String dataFolder,
                                              VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException, XMLStreamException {
        String filepathString = String.format(PATH_FORMAT, dataFolder, fileName);
        Path filepath = Paths.get(filepathString);
        //Check if file not in done folder, delete if true
        if(isDataFileInDoneFolder(filepath, campaignName, mode.getFolder())){
            log.warn("File {} already exists in DONE folder ! Deleting...", fileName);
            Files.deleteIfExists(filepath);
            return;
        }
        //Read file
        log.info("Try to read Xml file : {}", fileName);
        ResponseEntity<Object> response;
        if (getFileSizeInMB(filepath) <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
            response = processXmlFileWithMemory(filepath, mode, variablesMap);
        } else {
            response = processXmlFileSequentially(filepath, mode, variablesMap);
        }
        log.debug("File {} saved", fileName);
        if (response.getStatusCode() == HttpStatus.OK) {
            fileUtils.moveDataFile(campaignName, mode.getFolder(),filepath);
            return;
        }
        log.error("Error {} on file {} : {}", response.getStatusCode(), fileName,  response.getBody());

    }

    private static long getFileSizeInMB(Path filepath) {
        return filepath.toFile().length() / 1024 / 1024;
    }

    private boolean isDataFileInDoneFolder(Path filepath, String campaignName, String modeFolder) {
        return Path.of(fileUtils.getDoneFolder(campaignName, modeFolder)).resolve(filepath.getFileName()).toFile().exists();
    }

    private ResponseEntity<Object> processXmlFileWithMemory(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, ParserConfigurationException, SAXException {
        LunaticXmlCampaign campaign;
        // DOM method
        LunaticXmlDataParser parser = new LunaticXmlDataParser();
        try {
            campaign = parser.parseDataFile(filepath);
        } catch (GenesisException e) {
            log.error(e.toString());
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }

        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            surveyUnitModels.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), modeSpecified));
        }
        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

        log.debug("Saving {} survey units updates", surveyUnitModels.size());
        surveyUnitService.saveSurveyUnits(surveyUnitModels);
        log.debug("Survey units updates saved");

        log.info("File {} processed with {} survey units", filepath.getFileName(), surveyUnitModels.size());
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<Object> processXmlFileSequentially(Path filepath, Mode modeSpecified, VariablesMap variablesMap) throws IOException, XMLStreamException {
        LunaticXmlCampaign campaign;
        //Sequential method
        log.warn("File size > {} MB! Parsing XML file using sequential method...", Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL);
        try (final InputStream stream = new FileInputStream(filepath.toFile())) {
            LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath, stream);
            int suCount = 0;

            campaign = parser.getCampaign();
            LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();

            while (su != null) {
                List<SurveyUnitModel> surveyUnitModels = new ArrayList<>(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), modeSpecified));

                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                surveyUnitService.saveSurveyUnits(surveyUnitModels);
                suCount++;

                su = parser.readNextSurveyUnit();
            }

            log.info("Saved {} survey units updates from Xml file {}", suCount,  filepath.getFileName());
        }

        return ResponseEntity.ok().build();
    }

    private static String getSuccessMessage(boolean isAnyDataSaved) {
        return isAnyDataSaved ? SUCCESS_MESSAGE : SUCCESS_NO_DATA_MESSAGE;
    }
}
