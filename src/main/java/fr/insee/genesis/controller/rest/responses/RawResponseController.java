package fr.insee.genesis.controller.rest.responses;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.Error;
import com.networknt.schema.Schema;
import com.networknt.schema.SchemaRegistry;
import com.networknt.schema.dialect.Dialects;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Controller
@RequestMapping(path = "/responses/raw")
public class RawResponseController {

    private static final String SUCCESS_MESSAGE = "Interrogation %s saved";
    private static final String PARTITION_ID = "partitionId";
    private static final String INTERROGATION_ID = "interrogationId";
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
            @RequestParam(INTERROGATION_ID) String interrogationId,
            @RequestParam(value = "surveyUnitId", required = false) String idUE,
            @RequestParam(value = "mode") Mode modeSpecified,
            @RequestBody Map<String, Object> dataJson
    ) {
        log.info("Try to save interrogationId {} for campaign {}", interrogationId, campaignName);
        LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                .campaignId(campaignName)
                .questionnaireId(questionnaireId.toUpperCase())
                .interrogationId(interrogationId)
                .idUE(idUE)
                .mode(modeSpecified)
                .data(dataJson)
                .recordDate(LocalDateTime.now())
                .build();
        try {
            lunaticJsonRawDataApiPort.save(rawData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error");
        }
        log.info("Data saved for interrogationId {} and campaign {}", interrogationId, campaignName);
        // Collect platform prefer code 201 in case of success
        return ResponseEntity.status(201).body(String.format(SUCCESS_MESSAGE, interrogationId));
    }

    @Operation(summary = "Save lunatic json data from one interrogation in Genesis Database (with json " +
            "schema validation)")
    @PutMapping(path = "/lunatic-json")
    @PreAuthorize("hasRole('COLLECT_PLATFORM')")
    public ResponseEntity<String> saveRawResponsesFromJsonBodyWithValidation(
            @RequestBody Map<String, Object> body
    ) {
        SchemaRegistry schemaRegistry = SchemaRegistry.withDialect(Dialects.getDraft202012(), SchemaRegistry.Builder::build);
        Schema jsonSchema = schemaRegistry
                .getSchema(RawResponseController.class.getResourceAsStream("/jsonSchemas/RawResponse.json")
        );
        try {
            if (jsonSchema == null) {
                throw new GenesisException(500, "No RawResponse json schema has been found");
            }
            List<Error> errors = jsonSchema.validate(
                    new ObjectMapper().readTree(
                            new ObjectMapper().writeValueAsString(body)
                    )
            );
            // Throw Genesis exception if errors are present
            validate(errors);
            //Check required ids
            checkRequiredIds(body);
        } catch (JsonProcessingException jpe) {
            return ResponseEntity.status(400).body(jpe.toString());
        } catch (GenesisException ge) {
            return ResponseEntity.status(ge.getStatus()).body(ge.getMessage());
        }

        LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                .campaignId(body.get(PARTITION_ID).toString())
                .questionnaireId(body.get("questionnaireModelId").toString().toUpperCase())
                .interrogationId(body.get(INTERROGATION_ID).toString())
                .idUE(body.get("surveyUnitId").toString())
                .mode(Mode.getEnumFromJsonName(body.get("mode").toString()))
                .data(body)
                .recordDate(LocalDateTime.now())
                .build();
        try {
            lunaticJsonRawDataApiPort.save(rawData);
        } catch (Exception e) {
            return ResponseEntity.status(500).body("Unexpected error");
        }

        log.info("Data saved for interrogationId {} and partition {}", body.get(INTERROGATION_ID).toString(),
                body.get(PARTITION_ID).toString());
        return ResponseEntity.status(201).body(String.format(SUCCESS_MESSAGE, body.get(INTERROGATION_ID).toString()));
    }

    //GET unprocessed
    @Operation(summary = "Get campaign id and interrogationId from all unprocessed raw json data")
    @GetMapping(path = "/lunatic-json/get/unprocessed")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<List<LunaticJsonRawDataUnprocessedDto>> getUnproccessedJsonRawData() {
        log.info("Try to get unprocessed raw JSON datas...");
        return ResponseEntity.ok(lunaticJsonRawDataApiPort.getUnprocessedDataIds());
    }

    @Hidden
    @GetMapping(path = "/lunatic-json/get/by-interrogation-mode-and-campaign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LunaticJsonRawDataModel> getJsonRawData(
            @RequestParam(INTERROGATION_ID) String interrogationId,
            @RequestParam("campaignName") String campaignName,
            @RequestParam(value = "mode") Mode modeSpecified
    ) {
        List<LunaticJsonRawDataModel> data = lunaticJsonRawDataApiPort.getRawData(campaignName, modeSpecified, List.of(interrogationId));
        return ResponseEntity.ok(data.getFirst());
    }

    //PROCESS
    @Operation(summary = "Process raw data of a campaign")
    @PostMapping(path = "/lunatic-json/process")
    @PreAuthorize("hasRole('SCHEDULER')")
    @Deprecated(since = "1.13.0")
    public ResponseEntity<String> processJsonRawData(
            @RequestParam("campaignName") String campaignName,
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestBody List<String> interrogationIdList
    ) {
        log.info("Try to process raw JSON datas for campaign {} and {} interrogationIds", campaignName, interrogationIdList.size());
        List<GenesisError> errors = new ArrayList<>();

        try {
            DataProcessResult result = lunaticJsonRawDataApiPort.processRawData(campaignName, interrogationIdList, errors);
            return result.formattedDataCount() == 0 ?
                    ResponseEntity.ok("%d document(s) processed".formatted(result.dataCount()))
                    : ResponseEntity.ok("%d document(s) processed, including %d FORMATTED after data verification"
                    .formatted(result.dataCount(), result.formattedDataCount()));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Process raw data of a questionnaire")
    @PostMapping(path = "/lunatic-json/{questionnaireId}/process")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<String> processJsonRawData(
            @PathVariable String questionnaireId
    ) {
        log.info("Try to process raw JSON datas for questionnaire {}",questionnaireId);
        try {
            DataProcessResult result = lunaticJsonRawDataApiPort.processRawData(questionnaireId);
            return result.formattedDataCount() == 0 ?
                    ResponseEntity.ok("%d document(s) processed".formatted(result.dataCount()))
                    : ResponseEntity.ok("%d document(s) processed, including %d FORMATTED after data verification"
                    .formatted(result.dataCount(), result.formattedDataCount()));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Get processed data ids from last n hours (default 24h)")
    @GetMapping(path = "/lunatic-json/processed/ids")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, List<String>>> getProcessedDataIdsSinceHours(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam(name = "sinceHours", defaultValue = "24") int hours
    ) {
        log.info("Retrieve ids of data processed in last {}h", hours);
        Map<String, List<String>> result = lunaticJsonRawDataApiPort.findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime.now().minusHours(hours).minusMinutes(10));
        return ResponseEntity.ok(result);
    }

    @Operation(summary = "Get lunatic JSON data from one campaign in Genesis Database, filtered by start and end dates")
    @GetMapping(path = "/lunatic-json/{campaignId}")
    @PreAuthorize("hasRole('USER_BATCH_GENERIC')")
    public ResponseEntity<PagedModel<LunaticJsonRawDataModel>> getRawResponsesFromJsonBody(
            @PathVariable String campaignId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1000") int size
    ) {
        log.info("Try to read raw JSONs for campaign {}, with startDate={} and endDate={} - page={} - size={}", campaignId, startDate, endDate,page,size);
        Pageable pageable = PageRequest.of(page, size);
        Page<LunaticJsonRawDataModel> rawResponses = lunaticJsonRawDataApiPort.findRawDataByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
        log.info("rawResponses={}", rawResponses.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(rawResponses));
    }

    private void validate(List<Error> errors) throws GenesisException {
        if (!errors.isEmpty()) {
            String errorMessage = errors.stream()
                    .map(Error::getMessage)
                    .collect(Collectors.joining(System.lineSeparator() + " - "));

            throw new GenesisException(
                    400,
                    "Input data JSON is not valid: %n - %s".formatted(errorMessage)
            );
        }
    }

    private void checkRequiredIds(Map<String, Object> body) throws GenesisException {
        for (String requiredKey : List.of(
                PARTITION_ID,
                "questionnaireModelId",
                INTERROGATION_ID,
                "surveyUnitId",
                "mode"
        )) {
            if (body.get(requiredKey) == null) {
                throw new GenesisException(400, "No %s found in body".formatted(requiredKey));
            }
        }
    }

}
