package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequestMapping(path = "/responses/raw" )
public class RawResponseController {

    private static final String SUCCESS_MESSAGE = "Interrogation %s saved";
    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    
    public RawResponseController(LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort) {
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

    @Operation(summary = "Save lunatic json data from one interrogation in Genesis Database")
    @PutMapping(path = "/lunatic-json/with-validation")
    @PreAuthorize("hasRole('COLLECT_PLATFORM')")
    public ResponseEntity<String> saveRawResponsesFromJsonBodyWithValidation(
            @RequestBody Map<String, Object> body
    ) {
        JsonSchema jsonSchema = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V7).getSchema(
                RawResponseController.class.getResourceAsStream("/json_schemas/RawResponse.json")
        );
        try{
            Set<ValidationMessage> errors = jsonSchema.validate(
                    new ObjectMapper().readTree(
                            new ObjectMapper().writeValueAsString(body)
                    )
            );
            if(!errors.isEmpty()){
                throw new GenesisException(400, "Input data json is not valid \n%s".formatted(
                        Arrays.toString(errors.toArray()))
                );
            }

            //Check required ids
            checkRequiredIds(body);
        }catch (JsonProcessingException jpe){
            return ResponseEntity.status(400).body(jpe.toString());
        }catch (GenesisException ge){
            return ResponseEntity.status(ge.getStatus()).body(ge.getMessage());
        }

        return saveRawResponsesFromJsonBody(
                body.get("partitionId").toString(), //TODO Maybe adapt automatically to new models ?
                body.get("questionnaireModelId").toString(),
                body.get("interrogationId").toString(),
                body.get("surveyUnitId").toString(),
                Mode.getEnumFromJsonName(body.get("mode").toString()),
                body
        );
    }

    private void checkRequiredIds(Map<String, Object> body) throws GenesisException {
        for(String requiredKey : List.of(
                "partitionId",
                "questionnaireModelId",
                "interrogationId",
                "surveyUnitId",
                "mode"
        )){
            if(body.get(requiredKey) == null){
                throw new GenesisException(400, "No %s found in json".formatted(requiredKey));
            }
        }
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
    @Operation(summary = "Process raw data of a campaign")
    @PostMapping(path = "/lunatic-json/process")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<String> processJsonRawData(
            @RequestParam("campaignName") String campaignName,
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestBody List<String> interrogationIdList
    ){
        log.info("Try to process raw JSON datas for campaign {} and {} interrogationIds", campaignName, interrogationIdList.size());
        List<GenesisError> errors = new ArrayList<>();

        try {
            DataProcessResult result = lunaticJsonRawDataApiPort.processRawData(campaignName, interrogationIdList, errors);
            return result.formattedDataCount() == 0 ?
                    ResponseEntity.ok("%d document(s) processed".formatted(result.dataCount()))
                    : ResponseEntity.ok("%d document(s) processed, including %d FORMATTED after data verification"
                    .formatted(result.dataCount(), result.formattedDataCount()));
        }catch (GenesisException e){
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

}
