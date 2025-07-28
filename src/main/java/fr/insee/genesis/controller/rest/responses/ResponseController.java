package fr.insee.genesis.controller.rest.responses;

import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplified;
import fr.insee.genesis.controller.rest.CommonApiResponse;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequestMapping(path = "/responses" )
@Controller
@Tag(name = "Response services", description = "A **response** is considered the entire set of data associated with an interrogation (survey unit x questionnaireId). \n\n These data may have different state (collected, edited, external, ...) ")
@Slf4j
public class ResponseController implements CommonApiResponse {

    public static final String CAMPAIGN_ERROR = "Error for campaign {}: {}";
    private static final String SUCCESS_NO_DATA_MESSAGE = "No data has been saved";
    public static final String SUCCESS_MESSAGE = "Data saved";
    private final SurveyUnitApiPort surveyUnitService;

    public ResponseController(SurveyUnitApiPort surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
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
        try {
            surveyUnitService.saveResponsesFromXmlFile(xmlFile, metadataFilePath, modeSpecified, withDDI);
            return ResponseEntity.ok().build();
        } catch(MetadataParserException |
                IOException |
                ParserConfigurationException |
                SAXException |
                XMLStreamException ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ex.getMessage());
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Save multiple files to Genesis Database from the campaign root folder")
    @PutMapping(path = "/lunatic-xml/save-folder")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveResponsesFromXmlCampaignFolder(@RequestParam("campaignName") String campaignName,
                                                                     @RequestParam(value = "mode", required = false) Mode modeSpecified
    )throws Exception {
        try {
            boolean isAnyDataSaved = surveyUnitService.saveResponsesFromXmlCampaignFolder(campaignName, modeSpecified);
            return ResponseEntity.ok(getSuccessMessage(isAnyDataSaved));
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        } catch(Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
        }
    }

    //SAVE ALL
    @Operation(summary = "Save all files to Genesis Database (differential data folder only), regardless of the campaign")
    @PutMapping(path = "/lunatic-xml/save-all-campaigns")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> saveResponsesFromAllCampaignFolders(){
        try {
            surveyUnitService.saveResponsesFromAllCampaignFolders();
            return ResponseEntity.ok(ResponseController.SUCCESS_MESSAGE);
        } catch(NoDataException e) {
            return ResponseEntity.ok(e.getMessage());
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
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
    @GetMapping(path = "/by-ue-and-questionnaire/latest-states",
                produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<Object> findResponsesByInterrogationAndQuestionnaireLatestStates(
            @RequestParam("interrogationId") String interrogationId,
            @RequestParam("questionnaireId") String questionnaireId) throws GenesisException {
        try {
            SurveyUnitQualityToolDto responseQualityToolDto =  surveyUnitService.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId, questionnaireId);
            return ResponseEntity.ok(responseQualityToolDto);
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
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
        List<InterrogationId> interrogationIdsSubList = null;

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
                    .questionnaireId(responsesForSingleInterrId.getFirst().getQuestionnaireId())
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
    ){
        try {
            surveyUnitService.saveEditedVariables(surveyUnitInputDto);
            return ResponseEntity.ok(ResponseController.SUCCESS_MESSAGE);
        } catch(GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }


    private static String getSuccessMessage(boolean isAnyDataSaved) {
        return isAnyDataSaved ? SUCCESS_MESSAGE : SUCCESS_NO_DATA_MESSAGE;
    }

}
