package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ReaderUtils;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataSequentialParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.controller.utils.DataTransformer;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.lang.Nullable;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@RequestMapping(path = "/responses" )
@Controller
@Tag(name = "Response services", description = "A **response** is considered the entire set of data associated with an interrogation (survey unit x collecton Intrument Id [ex questionnaire]). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class ResponseController implements CommonApiResponse {

    public static final String PATH_FORMAT = "%s/%s";
    private static final String CAMPAIGN_ERROR = "Error for campaign {}: {}";
    private static final String SUCCESS_MESSAGE = "Data saved";
    private static final String SUCCESS_NO_DATA_MESSAGE = "No data has been saved";
    private final SurveyUnitApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final DataProcessingContextApiPort contextService;
    private final FileUtils fileUtils;
    private final ControllerUtils controllerUtils;
    private final AuthUtils authUtils;
    private final QuestionnaireMetadataService metadataService;

    public ResponseController(SurveyUnitApiPort surveyUnitService,
                              SurveyUnitQualityService surveyUnitQualityService,
                              FileUtils fileUtils,
                              ControllerUtils controllerUtils,
                              AuthUtils authUtils,
                              QuestionnaireMetadataService metadataService,
                              DataProcessingContextApiPort contextService
    ) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
        this.controllerUtils = controllerUtils;
        this.authUtils = authUtils;
        this.metadataService = metadataService;
        this.contextService = contextService;
    }

    //SAVE
    @Deprecated(since = "2026-01-01")
    @Operation(summary = "Save one file of responses to Genesis Database, passing its path as a parameter")
    @PutMapping(path = "/lunatic-xml/save-one")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveResponsesFromXmlFile(@RequestParam("pathLunaticXml") String xmlFile,
                                                           @RequestParam(value = "pathSpecFile") String metadataFilePath,
                                                           @RequestParam(value = "mode") Mode modeSpecified
    )throws Exception {
        log.info("Try to read one Xml file : {}", xmlFile);
        Path filepath = Paths.get(xmlFile);

        if (getFileSizeInMB(filepath) <= Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL) {
            return processXmlFileWithMemory(filepath, modeSpecified, metadataFilePath);
        }
        return processXmlFileSequentially(filepath, modeSpecified, metadataFilePath);
    }

    @Deprecated(since = "2026-01-01")
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
                processCampaignWithMode(campaignName, currentMode, null);
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
    @Deprecated(since = "2026-01-01")
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
                    processCampaignWithMode(campaignName, currentMode, Constants.DIFFERENTIAL_DATA_FOLDER_NAME);
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
    @Operation(summary = "Delete all responses associated with a collection instrument (formerly questionnaire)")
    @DeleteMapping(path = "/delete/{collectionInstrumentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteAllResponsesByCollectionInstrument(@PathVariable("collectionInstrumentId") String collectionInstrumentId) {
        log.info("Try to delete all responses of collection instrument : {}", collectionInstrumentId);
        Long ndDocuments = surveyUnitService.deleteByCollectionInstrumentId(collectionInstrumentId);
        log.info("{} responses deleted", ndDocuments);
        return ResponseEntity.ok(String.format("%d responses deleted", ndDocuments));
    }

    //GET
    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and collectionInstrumentId (formerly questionnaireId) from Genesis Database")
    @GetMapping(path = "/by-interrogation-and-collection-instrument")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyUnitModel>> findResponsesByInterrogationAndCollectionInstrument(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("collectionInstrumentId") String collectionInstrumentId)
    {
        List<SurveyUnitModel> responses = surveyUnitService.findByIdsInterrogationAndCollectionInstrument(interrogationId, collectionInstrumentId);
        return ResponseEntity.ok(responses);
    }

    /**
     * @deprecated
     * This endpoint is deprecated because the parameter `questionnaireId` has been renamed
     * to `collectionInstrumentId` in the Information System (modeled in the modelefiliere library).
     * A new endpoint using the updated parameter names will be provided to remain compliant with
     * the current data model. This endpoint will be removed once all dependent APIs have adopted
     * the new naming convention.
     *
     * Use the new endpoint with `collectionInstrumentId` for future implementations.
     */
    @Deprecated(forRemoval = true, since= "2026-01-01")
    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and questionnaireId from Genesis Database with the latest value for each available state of every variable",
                description = "use /by-interrogation-and-collection-instrument/latest-states instead")
    @GetMapping(path = "/by-ue-and-questionnaire/latest-states",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> findResponsesByInterrogationAndQuestionnaireLatestStates(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("questionnaireId") String questionnaireId) throws GenesisException {
        //Check context
        DataProcessingContextModel dataProcessingContextModel =
                contextService.getContext(interrogationId);

        if(dataProcessingContextModel == null || !dataProcessingContextModel.isWithReview()){
            return ResponseEntity.status(403).body(new ApiError("Review is disabled for that partition"));
        }

        SurveyUnitDto response = surveyUnitService.findLatestValuesByStateByIdAndByCollectionInstrumentId(interrogationId, questionnaireId);
        SurveyUnitQualityToolDto responseQualityToolDto = DataTransformer.transformSurveyUnitDto(response);
        return ResponseEntity.ok(responseQualityToolDto);
    }

    @Operation(summary = "Retrieve the latest available values for each variable state for a given interrogation and collection instrument (formerly questionnaire).")
    @GetMapping(path = "/by-interrogation-and-collection-instrument/latest-states",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> findResponsesByInterrogationAndCollectionInstrumentLatestStates(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("collectionInstrumentId") String collectionInstrumentId) throws GenesisException {
        //Check context
        DataProcessingContextModel dataProcessingContextModel = contextService.getContext(interrogationId);

        if(dataProcessingContextModel == null || !dataProcessingContextModel.isWithReview()){
            return ResponseEntity.status(403).body(new ApiError("Review is disabled for that partition"));
        }

        SurveyUnitDto response = surveyUnitService.findLatestValuesByStateByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);
        SurveyUnitQualityToolDto responseQualityToolDto = DataTransformer.transformSurveyUnitDto(response);
        return ResponseEntity.ok(responseQualityToolDto);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and collectionInstrumentId from Genesis Database. It returns only the latest value of each variable regardless of the state.")
    @GetMapping(path = "/by-interrogation-and-collection-instrument/latest")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyUnitModel>> getLatestByInterrogationAndCollectionInstrument(@RequestParam("interrogationId") String interrogationId,
                                                               @RequestParam("collectionInstrumentId") String collectionInstrumentId) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve responses for an interrogation, using interrogationId and collectionInstrumentId from Genesis Database. For a given mode, it returns only the latest value of each variable regardless of the state. The result is one object by mode in the output")
    @GetMapping(path = "/simplified/by-interrogation-collection-instrument-and-mode/latest")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<SurveyUnitSimplified> getLatestByInterrogationOneObject(@RequestParam("interrogationId") String interrogationId,
                                                                             @RequestParam("collectionInstrumentId") String collectionInstrumentId,
                                                                             @RequestParam("mode") Mode mode) {
        List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(interrogationId, collectionInstrumentId);
        List<VariableModel> outputVariables = new ArrayList<>();
        List<VariableModel> outputExternalVariables = new ArrayList<>();
        responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });
        return ResponseEntity.ok(SurveyUnitSimplified.builder()
                .collectionInstrumentId(responses.getFirst().getCollectionInstrumentId())
                .campaignId(responses.getFirst().getCampaignId())
                .interrogationId(responses.getFirst().getInterrogationId())
                .usualSurveyUnitId(responses.getFirst().getUsualSurveyUnitId())
                .variablesUpdate(outputVariables)
                .externalVariables(outputExternalVariables)
                .build());
    }


    @Operation(summary = "Retrieve all responses for a collection instrument and a list of interrogations",
            description = "Return the latest state for each variable for the given interrogationIds and a given collection instrument (formerly questionnaire).<br>" +
                    "For a given id, the endpoint returns a document by collection mode (if there is more than one).")
    @PostMapping(path = "/simplified/by-list-interrogation-and-collection-instrument/latest")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    //TODO move logic and unit test to surveyUnitService (also extract some methods instead of multiple lambdas)
    public ResponseEntity<List<SurveyUnitSimplified>> getLatestForInterrogationListAndCollectionInstrument(
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestBody List<InterrogationId> interrogationIds)
    {
        List<SurveyUnitSimplified> results = new ArrayList<>();
        List<Mode> modes = surveyUnitService.findModesByCollectionInstrumentId(collectionInstrumentId);
        interrogationIds.forEach(interrogationId -> {
            List<SurveyUnitModel> responses = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(interrogationId.getInterrogationId(), collectionInstrumentId);
            modes.forEach(mode -> {
                List<VariableModel> outputVariables = new ArrayList<>();
                List<VariableModel> outputExternalVariables = new ArrayList<>();
                List<String> usualSurveyUnitIds = new ArrayList<>();
                responses.stream().filter(rep -> rep.getMode().equals(mode)).forEach(response -> {
                    outputVariables.addAll(response.getCollectedVariables());
                    outputExternalVariables.addAll(response.getExternalVariables());
                    if(response.getUsualSurveyUnitId() != null){
                        usualSurveyUnitIds.add(response.getUsualSurveyUnitId());
                    }
                });
                if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
                    results.add(SurveyUnitSimplified.builder()
                            .collectionInstrumentId(responses.getFirst().getCollectionInstrumentId())
                            .campaignId(responses.getFirst().getCampaignId())
                            .interrogationId(interrogationId.getInterrogationId())
                            .usualSurveyUnitId(!usualSurveyUnitIds.isEmpty() ? usualSurveyUnitIds.getFirst() : null)
                            .mode(mode)
                            .variablesUpdate(outputVariables)
                            .externalVariables(outputExternalVariables)
                            .build());
                }
            });
        });
        return ResponseEntity.ok(results);
    }


    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
            description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                    "For a given id, the endpoint returns a document by collection mode (if there is more than one).")
    @PostMapping(path = "/simplified/by-list-interrogation-and-questionnaire/latestV2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<List<SurveyUnitSimplified>> getLatestForInterrogationListV2(@RequestParam("questionnaireId") String questionnaireId,
                                                                                      @RequestParam List<String> modes,
                                                                                        @RequestBody List<InterrogationId> interrogationIds) {
        List<SurveyUnitSimplified> results = new ArrayList<>();

        //!!!WARNING!!! : FOR PERFORMANCES PURPOSES, WE DONT'MAKE REQUESTS ON INDIVIDUAL ELEMENTS ANYMORE, BUT ON A SUBLIST OF THE INPUTLIST
        final int SUBBLOCK_SIZE = 100;
        int offset = 0;
        List<InterrogationId> interrogationIdsSubList;

        for(String mode : modes) {

            while(offset <= interrogationIds.size()) {
                //extract part of input list
                int endOffset = Math.min(offset + SUBBLOCK_SIZE, interrogationIds.size());
                interrogationIdsSubList = interrogationIds.subList(offset, endOffset);

                //1) For each InterrogationId, we collect all responses versions, in which ONLY THE LATEST VERSION of each variable is kept.
                List<List<SurveyUnitModel>> responses = surveyUnitService.findLatestByIdAndByQuestionnaireIdAndModeOrdered(questionnaireId, mode, interrogationIdsSubList);

                responses.forEach(responsesForSingleInterrId -> {
                    SurveyUnitSimplified simplifiedResponse = fusionWithLastUpdated(responsesForSingleInterrId, mode);
                    if(simplifiedResponse != null) {
                        results.add(simplifiedResponse);
                    }
                });

                offset = offset + SUBBLOCK_SIZE;
            }
        }

        return ResponseEntity.ok(results);
    }


    private SurveyUnitSimplified fusionWithLastUpdated(List<SurveyUnitModel> responsesForSingleInterrId, String mode) {
        //NOTE : 1) "responses" in input here corresponds to all collected responses versions of a given "InterrogationId",
        //       in which ONLY THE LATEST VERSION of each variable is kept.

        //return simplifiedResponse
        SurveyUnitSimplified simplifiedResponse = null;

        //2) storage of the !!!FUSION!!! OF ALL LATEST UPDATED variables (located in the different versions of the stored "InterrogationId")
        List<VariableModel> outputVariables = new ArrayList<>();
        List<VariableModel> outputExternalVariables = new ArrayList<>();

        responsesForSingleInterrId.forEach(response -> {
            outputVariables.addAll(response.getCollectedVariables());
            outputExternalVariables.addAll(response.getExternalVariables());
        });

        //3) add to the result list the compiled fusion of all the latest variables
        if (!outputVariables.isEmpty() || !outputExternalVariables.isEmpty()) {
            Mode modeWrapped = Mode.getEnumFromModeName(mode);

            simplifiedResponse = SurveyUnitSimplified.builder()
                    .collectionInstrumentId(responsesForSingleInterrId.getFirst().getCollectionInstrumentId())
                    .campaignId(responsesForSingleInterrId.getFirst().getCampaignId())
                    .interrogationId(responsesForSingleInterrId.getFirst().getInterrogationId())
                    .mode(modeWrapped)
                    .variablesUpdate(outputVariables)
                    .externalVariables(outputExternalVariables)
                    .build();
        }

        return simplifiedResponse;
    }
    //========= OPTIMISATIONS PERFS (END) ==========


    @Operation(summary = "Save edited variables",
            description = "Save edited variables document into database")
    @PostMapping(path = "/save-edited")
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<Object> saveEditedVariables(
            @RequestBody SurveyUnitInputDto surveyUnitInputDto
    ) {
        try {
            log.debug("Received in save edited : {}", surveyUnitInputDto.toString());
            //Code quality : we need to put all that logic out of this controller
            //Parse metadata
            //Try to look for DDI first, if no DDI found looks for lunatic components
            List<GenesisError> errors = new ArrayList<>();
            //We need to retrieve campaignId
            Set<String> campaignIds = surveyUnitService.findCampaignIdsFrom(surveyUnitInputDto);
            if (campaignIds.size() != 1) {
                return ResponseEntity.status(500).body("Impossible to assign one campaignId to that response");
            }
            // If the size is equal to 1 we get this campaignId
            String campaignId = campaignIds.iterator().next();
            surveyUnitInputDto.setCampaignId(campaignId);

            MetadataModel metadataModel = metadataService.loadAndSaveIfNotExists(
                    surveyUnitInputDto.getCampaignId(),
                    surveyUnitInputDto.getQuestionnaireId(),
                    surveyUnitInputDto.getMode(),
                    fileUtils,
                    errors);
            if(metadataModel == null){
                throw new GenesisException(404, errors.getLast().getMessage());
            }

            //Check if input edited variables are in metadatas
            List<String> absentCollectedVariableNames =
                    surveyUnitQualityService.checkVariablesPresentInMetadata(surveyUnitInputDto.getCollectedVariables(),
                            metadataModel.getVariables());
            if (!absentCollectedVariableNames.isEmpty()) {
                String absentVariables = String.join("\n", absentCollectedVariableNames);
                return ResponseEntity.badRequest().body(
                        String.format("The following variables are absent in metadatas : %n%s", absentVariables)
                );
            }

            //Fetch user identifier from OIDC token
            String userIdentifier = authUtils.getIDEP();


            //Create surveyUnitModel for each STATE received (Quality tool could send variables with another STATE
            // than EDITED)
            List<SurveyUnitModel> surveyUnitModels;
            surveyUnitModels = surveyUnitService.parseEditedVariables(
                    surveyUnitInputDto,
                    userIdentifier,
                    metadataModel.getVariables()
            );

            //Check data with dataverifier (might create a FORCED document)
            surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, metadataModel.getVariables());

            //Save documents
            surveyUnitService.saveSurveyUnits(surveyUnitModels);
            return ResponseEntity.ok(SUCCESS_MESSAGE);
        } catch (GenesisException ge) {
            return ResponseEntity.status(ge.getStatus()).body(ge.getMessage());
        }
    }



    //Utilities
    /**
     * Process a campaign with a specific mode
     * @param campaignName name of campaign
     * @param mode mode of collected data
     */
    private void processCampaignWithMode(String campaignName, Mode mode, String rootDataFolder)
            throws IOException, ParserConfigurationException, SAXException, XMLStreamException, NoDataException, GenesisException {
        log.info("Starting data import for mode: {}", mode.getModeName());

        String dataFolder = rootDataFolder == null ?
                fileUtils.getDataFolder(campaignName, mode.getFolder(), null)
                : fileUtils.getDataFolder(campaignName, mode.getFolder(), rootDataFolder);
        List<String> dataFiles = fileUtils.listFiles(dataFolder);
        log.info("Number of files to load in folder {} : {}", dataFolder, dataFiles.size());
        if (dataFiles.isEmpty()) {
            throw new NoDataException("No data file found in folder %s".formatted(dataFolder));
        }

        //For each XML data file
        for (String fileName : dataFiles.stream().filter(s -> s.endsWith(".xml")).toList()) {
            processOneXmlFileForCampaign(campaignName, mode, fileName, dataFolder);
        }

        //Create context if not exist
        if(contextService.getContextByCollectionInstrumentId(campaignName) == null){
            contextService.saveContext(campaignName, false);
        }

    }

    private void processOneXmlFileForCampaign(String campaignName,
                                              Mode mode,
                                              String fileName,
                                              String dataFolder) throws IOException, ParserConfigurationException, SAXException, XMLStreamException, GenesisException {
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
            response = processXmlFileWithMemory(filepath, mode, null);
        } else {
            response = processXmlFileSequentially(filepath, mode, null);
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

    private ResponseEntity<Object> processXmlFileWithMemory(Path filepath,
                                                            Mode modeSpecified,
                                                            @Nullable String metadataFilePath) throws IOException,
            ParserConfigurationException, SAXException, GenesisException {
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
        VariablesMap variablesMap = null;
        for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
            if(variablesMap == null){
                variablesMap = getVariablesMap(modeSpecified, su, campaign, metadataFilePath);
            }
            surveyUnitModels.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), modeSpecified));
        }
        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);

        log.debug("Saving {} survey units updates", surveyUnitModels.size());
        surveyUnitService.saveSurveyUnits(surveyUnitModels);
        log.debug("Survey units updates saved");

        log.info("File {} processed with {} survey units", filepath.getFileName(), surveyUnitModels.size());
        return ResponseEntity.ok().build();
    }

    private ResponseEntity<Object> processXmlFileSequentially(Path filepath,
                                                              Mode modeSpecified,
                                                              @Nullable String metadataFilePath
                                                              ) throws IOException,
            XMLStreamException, GenesisException {
        LunaticXmlCampaign campaign;
        //Sequential method
        log.warn("File size > {} MB! Parsing XML file using sequential method...", Constants.MAX_FILE_SIZE_UNTIL_SEQUENTIAL);
        try (final InputStream stream = new FileInputStream(filepath.toFile())) {
            LunaticXmlDataSequentialParser parser = new LunaticXmlDataSequentialParser(filepath, stream);
            int suCount = 0;

            campaign = parser.getCampaign();
            LunaticXmlSurveyUnit su = parser.readNextSurveyUnit();
            contextService.saveContext(campaign.getCampaignId(), false);
            VariablesMap variablesMap = null;
            while (su != null) {
                if(variablesMap == null){
                    variablesMap = getVariablesMap(modeSpecified, su, campaign, metadataFilePath);
                }
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

    private VariablesMap getVariablesMap(Mode modeSpecified,
                                         LunaticXmlSurveyUnit surveyUnit,
                                         LunaticXmlCampaign campaign,
                                         @Nullable String metadataFilePath
                                         ) throws GenesisException {
        if(metadataFilePath != null){
            return getVariablesMapWithPath(metadataFilePath);
        }
        VariablesMap variablesMap;
        List<GenesisError> genesisErrors = new ArrayList<>();
        MetadataModel metadataModel = metadataService.loadAndSaveIfNotExists(
                campaign.getCampaignId(),
                surveyUnit.getQuestionnaireModelId(),
                modeSpecified,
                fileUtils,
                genesisErrors
        );
        if(!genesisErrors.isEmpty()){
            throw new GenesisException(400,genesisErrors.getLast().getMessage());
        }
        variablesMap = metadataModel.getVariables();
        return variablesMap;
    }

    private static VariablesMap getVariablesMapWithPath(String metadataFilePath) throws GenesisException {
        if(metadataFilePath.endsWith(".xml")) {
            //Parse DDI
            log.info("Try to read DDI file : {}", metadataFilePath);
            try {
                InputStream metadataInputStream = new FileInputStream(metadataFilePath);
                return ReaderUtils.getMetadataFromDDIAndLunatic(Path.of(metadataFilePath).toFile().toURI().toURL().toString(),
                        metadataInputStream,metadataInputStream).getVariables();
            } catch (MetadataParserException e) {
                throw new GenesisException(500, e.getMessage());
            } catch (FileNotFoundException fnfe){
                throw new GenesisException(404, fnfe.toString());
            } catch (MalformedURLException mue){
                throw new GenesisException(400, mue.toString());
            }
        }else{
            //Parse Lunatic
            log.info("Try to read lunatic file : {}", metadataFilePath);
            try {
                return LunaticReader.getMetadataFromLunatic(new FileInputStream(metadataFilePath)).getVariables();
            } catch (FileNotFoundException fnfe){
                throw new GenesisException(404, fnfe.toString());
            }
        }
    }

    private static String getSuccessMessage(boolean isAnyDataSaved) {
        return isAnyDataSaved ? SUCCESS_MESSAGE : SUCCESS_NO_DATA_MESSAGE;
    }
}
