package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.controller.utils.AuthUtils;
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
import fr.insee.genesis.exceptions.ReviewDisabledException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/responses" )
@Controller
@Tag(name = "Response services", description = "A **response** is considered the entire set of data associated with an interrogation (survey unit x collecton Intrument Id [ex questionnaire]). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class ResponseController implements CommonApiResponse {

    private static final String SUCCESS_MESSAGE = "Data saved";

    private final SurveyUnitApiPort surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final DataProcessingContextApiPort contextService;
    private final FileUtils fileUtils;
    private final AuthUtils authUtils;
    private final QuestionnaireMetadataService metadataService;

    public ResponseController(SurveyUnitApiPort surveyUnitService,
                              SurveyUnitQualityService surveyUnitQualityService,
                              FileUtils fileUtils,
                              AuthUtils authUtils,
                              QuestionnaireMetadataService metadataService,
                              DataProcessingContextApiPort contextService
    ) {
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
        this.authUtils = authUtils;
        this.metadataService = metadataService;
        this.contextService = contextService;
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

    //GET
    @Operation(summary = "Retrieve responses for an interrogation, using usualSurveyUnitId and collectionInstrumentId (formerly questionnaireId) from Genesis Database")
    @GetMapping(path = "/by-usual-survey-unit-and-collection-instrument")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SurveyUnitModel>> findResponsesByUsualSurveyUnitAndCollectionInstrument(
            @RequestParam("usualSurveyUnitId") String usualSurveyUnitId,
            @RequestParam("collectionInstrumentId") String collectionInstrumentId)
    {
        List<SurveyUnitModel> responses = surveyUnitService.findByIdsUsualSurveyUnitAndCollectionInstrument(usualSurveyUnitId, collectionInstrumentId);
        return ResponseEntity.ok(responses);
    }

    @Operation(summary = "Retrieve the latest available values for each variable state for a given interrogation and collection instrument (formerly questionnaire).")
    @GetMapping(path = "/by-interrogation-and-collection-instrument/latest-states",
            produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> findResponsesByInterrogationAndCollectionInstrumentLatestStates(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("collectionInstrumentId") String collectionInstrumentId) throws GenesisException {
        //TODO move logic to service
        DataProcessingContextModel dataProcessingContextModel;
        //Check context
        dataProcessingContextModel = contextService.getContext(interrogationId);


        if(dataProcessingContextModel == null || !dataProcessingContextModel.isWithReview()){
            throw new ReviewDisabledException();
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

    @Operation(summary = "Returns the response with the latest variables for a collectionInstrument, mode and " +
            "interrogation")
    @GetMapping(path = "/{collectionInstrumentId}/{mode}/{interrogationId}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<SurveyUnitSimplifiedDto> getResponseByCollectionInstrumentAndInterrogation(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId,
            @PathVariable("interrogationId") String interrogationId,
            @PathVariable("mode") Mode mode) throws NoDataException {
        return ResponseEntity.ok(
                surveyUnitService.findSimplified(
                        collectionInstrumentId,
                        interrogationId,
                        mode,
                        null
                )
        );
    }

    @Operation(summary = "Retrieve all responses for a collection instrument and a list of interrogations",
            description = "Return the latest state for each variable for the given interrogationIds and a given collection instrument (formerly questionnaire).<br>" +
                    "For a given id, the endpoint returns a document by collection mode (if there is more than one)<br>" +
                    "If the 'recordedBefore' parameter is provided, only responses recorded before this timestamp will be returned.")
    @PostMapping(path = "/{collectionInstrumentId}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<List<SurveyUnitSimplifiedDto>> searchResponses(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId,
            @Parameter(
                    description = "Filter responses to those recorded before or at the same time of the given timestamp (ISO-8601 UTC format).",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T00:00:00Z")
            )
            @RequestParam(value = "recordedBefore", required = false) Instant recordedBefore,
            @RequestBody List<InterrogationId> interrogationIds)
    {
        return ResponseEntity.ok(
                surveyUnitService.findSimplifiedList(
                        collectionInstrumentId,
                        interrogationIds,
                        recordedBefore
                )
        );
    }

    @Operation(summary = "Save edited variables",
            description = "Save edited variables document into database")
    @PostMapping(path = "/save-edited")
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<Object> saveEditedVariables(
            @RequestBody SurveyUnitInputDto surveyUnitInputDto
    ) throws GenesisException {
            log.debug("Received in save edited : {}", surveyUnitInputDto.toString());
            //TODO Code quality : we need to put all that logic out of this controller
            //Parse metadata
            //Try to look for DDI first, if no DDI found looks for lunatic components
            List<GenesisError> errors = new ArrayList<>();

            MetadataModel metadataModel = metadataService.loadAndSaveIfNotExists(
                    null,
                    surveyUnitInputDto.getQuestionnaireId(),
                    surveyUnitInputDto.getMode(),
                    fileUtils,
                    errors);
            if(metadataModel == null){
                    String msg = errors.isEmpty()
                            ? "Empty metadataModel for questionnaireId=%s, mode=%s"
                            .formatted(surveyUnitInputDto.getQuestionnaireId(), surveyUnitInputDto.getMode())
                            : errors.getLast().getMessage();

                    throw new GenesisException(HttpStatus.NOT_FOUND, msg);
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
    }

    //SPRING/SUMMER 2025
    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    //TODO Unused for now, reuse code for optimizations, also move it to service
    @Deprecated
    @Hidden
    @Operation(summary = "Retrieve all responses for a questionnaire and a list of UE",
            description = "Return the latest state for each variable for the given ids and a given questionnaire.<br>" +
                    "For a given id, the endpoint returns a document by collection mode (if there is more than one).")
    @PostMapping(path = "/simplified/by-list-interrogation-and-questionnaire/latestV2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<List<SurveyUnitSimplifiedDto>> getLatestForInterrogationListV2(@RequestParam("questionnaireId") String questionnaireId,
                                                                                         @RequestParam List<String> modes,
                                                                                         @RequestBody List<InterrogationId> interrogationIds) {
        List<SurveyUnitSimplifiedDto> results = new ArrayList<>();

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
                    SurveyUnitSimplifiedDto simplifiedResponse = fusionWithLastUpdated(responsesForSingleInterrId, mode);
                    if(simplifiedResponse != null) {
                        results.add(simplifiedResponse);
                    }
                });

                offset = offset + SUBBLOCK_SIZE;
            }
        }

        return ResponseEntity.ok(results);
    }


    private SurveyUnitSimplifiedDto fusionWithLastUpdated(List<SurveyUnitModel> responsesForSingleInterrId, String mode) {
        //NOTE : 1) "responses" in input here corresponds to all collected responses versions of a given "InterrogationId",
        //       in which ONLY THE LATEST VERSION of each variable is kept.

        //return simplifiedResponse
        SurveyUnitSimplifiedDto simplifiedResponse = null;

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

            simplifiedResponse = SurveyUnitSimplifiedDto.builder()
                    .collectionInstrumentId(responsesForSingleInterrId.getFirst().getCollectionInstrumentId())
                    .interrogationId(responsesForSingleInterrId.getFirst().getInterrogationId())
                    .mode(modeWrapped)
                    .validationDate(responsesForSingleInterrId.getFirst().getValidationDate())
                    .questionnaireState(responsesForSingleInterrId.getFirst().getQuestionnaireState())
                    .variablesUpdate(outputVariables)
                    .externalVariables(outputExternalVariables)
                    .build();
        }

        return simplifiedResponse;
    }
    //========= OPTIMISATIONS PERFS (END) ==========

}
