package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
import fr.insee.modelefiliere.RawResponseDto;
import io.swagger.v3.oas.annotations.Hidden;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
@Controller
@RequiredArgsConstructor
public class RawResponseController {

    private static final String SUCCESS_MESSAGE = "Interrogation %s saved";
    private static final String INTERROGATION_ID = "interrogationId";

    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort;
    private final RawResponseApiPort rawResponseApiPort;
    private final RawResponseInputRepository rawRepository;

    @Operation(summary = "Save lunatic json data from one interrogation in Genesis Database")
    @PutMapping(path = "/responses/raw/lunatic-json/save")
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
    @PostMapping(path="/raw-responses")
    @PreAuthorize("hasRole('COLLECT_PLATFORM')")
    public ResponseEntity<String> saveRawResponsesFromRawResponseDto(
            @Valid @RequestBody RawResponseDto dto
    ) {
        rawRepository.saveAsRawJson(dto);
        return ResponseEntity.status(201).body(String.format(SUCCESS_MESSAGE, dto.getInterrogationId()));
    }

    //PROCESS
    @Operation(summary = "Process raw data for a list of interrogations")
    @PostMapping(path = "/raw-responses/process")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<String> processRawResponses(
            @Parameter(
                    description = "Id of the collection instrument (old questionnaireId)",
                    example = "ENQTEST2025X00"
            )
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestBody List<String> interrogationIdList
    ) {
        log.info("Try to process raw responses for collectionInstrumentId {} and {} interrogationIds", collectionInstrumentId, interrogationIdList.size());
        List<GenesisError> errors = new ArrayList<>();
        try {
            DataProcessResult result = rawResponseApiPort.processRawResponses(collectionInstrumentId, interrogationIdList, errors);
            return ResponseEntity.ok(result.message(collectionInstrumentId));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Process raw data for all data of an collection instrument")
    @PostMapping(path = "/raw-responses/{collectionInstrumentId}/process")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<String> processRawResponsesByCollectionInstrumentId(
            @Parameter(
                    description = "Id of the collection instrument (old questionnaireId)",
                    example = "ENQTEST2025X00"
            )
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ) {
        log.info("Try to process raw responses for collectionInstrumentId {}", collectionInstrumentId);
        try {
            DataProcessResult result = rawResponseApiPort.processRawResponses(collectionInstrumentId);
            return ResponseEntity.ok(result.message(collectionInstrumentId));
        } catch (GenesisException e) {
            return ResponseEntity.status(e.getStatus()).body(e.getMessage());
        }
    }

    @Operation(summary = "Get the list of collection instruments containing unprocessed interrogations")
    @GetMapping(path = "/raw-responses/unprocessed/collection-instrument-ids")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<List<String>> getUnprocessedCollectionInstrument(){
        log.info("Try to get collection instruments containing unprocessed interrogations...");
        return ResponseEntity.ok(rawResponseApiPort.getUnprocessedCollectionInstrumentIds());
    }

    //GET unprocessed
    @Operation(summary = "Get campaign id and interrogationId from all unprocessed raw json data")
    @GetMapping(path = "/responses/raw/lunatic-json/get/unprocessed")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<List<LunaticJsonRawDataUnprocessedDto>> getUnprocessedJsonRawData() {
        log.info("Try to get unprocessed raw JSON datas...");
        return ResponseEntity.ok(lunaticJsonRawDataApiPort.getUnprocessedDataIds());
    }

    @Operation(summary = "Get campaign id and interrogationId from all unprocessed raw json data")
    @GetMapping(path = "/responses/raw/lunatic-json/get/unprocessed/questionnaireIds")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Set<String>> getUnprocessedJsonRawDataQuestionnairesIds() {
        log.info("Try to get unprocessed raw JSON datas questionniares...");
        return ResponseEntity.ok(lunaticJsonRawDataApiPort.getUnprocessedDataQuestionnaireIds());
    }

    @Hidden
    @GetMapping(path = "/responses/raw/lunatic-json/get/by-interrogation-mode-and-campaign")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LunaticJsonRawDataModel> getJsonRawData(
            @RequestParam(INTERROGATION_ID) String interrogationId,
            @RequestParam("campaignName") String campaignName,
            @RequestParam(value = "mode") Mode modeSpecified
    ) {
        List<LunaticJsonRawDataModel> data = lunaticJsonRawDataApiPort.getRawDataByQuestionnaireId(campaignName, modeSpecified, List.of(interrogationId));
        return ResponseEntity.ok(data.getFirst());
    }

    //PROCESS
    @Operation(summary = "Process raw data of a campaign")
    @PostMapping(path = "/responses/raw/lunatic-json/process")
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

    @Operation(summary = "Process raw data of a questionnaire (old raw model)")
    @PostMapping(path = "/responses/raw/lunatic-json/{questionnaireId}/process")
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
    @GetMapping(path = "/responses/raw/lunatic-json/processed/ids")
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
    @GetMapping(path = "/responses/raw/lunatic-json/{campaignId}")
    @PreAuthorize("hasRole('USER_BATCH_GENERIC')")
    public ResponseEntity<PagedModel<LunaticJsonRawDataModel>> getLunaticJsonRawDataModelFromJsonBody(
            @PathVariable String campaignId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1000") int size
    ) {
        log.info("Try to read raw JSONs for campaign {}, with startDate={} and endDate={} - page={} - size={}", campaignId, startDate, endDate,page,size);
        Pageable pageable = PageRequest.of(page, size);
        Page<LunaticJsonRawDataModel> rawResponses = lunaticJsonRawDataApiPort.findRawDataByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
        log.info("rawResponses, lunatic-json for campaign {}, with startDate={} and endDate={} ={}", campaignId, startDate, endDate,rawResponses.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(rawResponses));
    }

    @Operation(summary = "Get lunatic JSON data from one questionnaire in Genesis Database")
    @GetMapping(path = "/responses/raw/lunatic-json/by-questionnaire/{questionnaireId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<LunaticJsonRawDataModel>> getLunaticJsonRawDataModelFromQuestionnaire(
            @PathVariable String questionnaireId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1000") int size
    ) {
        log.info("Try to read raw lunatic JSONs for questionnaire {} - page={} - size={}", questionnaireId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<LunaticJsonRawDataModel> rawResponses = lunaticJsonRawDataApiPort.findRawDataByQuestionnaireId(questionnaireId, pageable);
        log.info("rawResponses for questionnaire {} = {}",questionnaireId, rawResponses.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(rawResponses));
    }

    @Operation(summary = "Check existence of an interrogation")
    @RequestMapping(value = "/responses/raw/lunatic-json/{interrogationId}", method = RequestMethod.HEAD)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> existsLunaticJsonByInterrogationId(@PathVariable String interrogationId) {
        if (lunaticJsonRawDataApiPort.existsByInterrogationId(interrogationId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }

    @Operation(summary = "Get rawResponse JSON data from one campaign in Genesis Database, filtered by start and end dates")
    @GetMapping(path = "/raw-responses/{campaignId}")
    @PreAuthorize("hasRole('USER_BATCH_GENERIC')")
    public ResponseEntity<PagedModel<RawResponseModel>> getRawResponsesFromJsonBody(
            @PathVariable String campaignId,
            @RequestParam(value = "startDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant startDate,
            @RequestParam(value = "endDate", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) Instant endDate,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1000") int size
    ) {
        log.info("Try to read raw lunatic JSONs for campaign {}, with startDate={} and endDate={} - page={} - size={}", campaignId, startDate, endDate,page,size);
        Pageable pageable = PageRequest.of(page, size);
        Page<RawResponseModel> rawResponses = rawResponseApiPort.findRawResponseDataByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
        log.info("rawResponses for campaign {}, with startDate={} and endDate={} ={}",campaignId, startDate, endDate, rawResponses.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(rawResponses));
    }

    @Operation(summary = "Get rawResponse JSON data from one collection instrument in Genesis Database")
    @GetMapping(path = "/raw-responses/by-collection-instrument/{collectionInstrumentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PagedModel<RawResponseModel>> getRawResponsesFromCollectionInstrumentId(
            @PathVariable String collectionInstrumentId,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "1000") int size
    ) {
        log.info("Try to read raw JSONs for collectionInstrument {} - page={} - size={}", collectionInstrumentId, page, size);
        Pageable pageable = PageRequest.of(page, size);
        Page<RawResponseModel> rawResponses = rawResponseApiPort.findRawResponseDataByCollectionInstrumentId(collectionInstrumentId, pageable);
        log.info("rawResponses={}", rawResponses.getContent().size());
        return ResponseEntity.status(HttpStatus.OK).body(new PagedModel<>(rawResponses));
    }

    @Operation(summary = "Check existence of an interrogation")
    @RequestMapping(value = "/raw-responses/{interrogationId}", method = RequestMethod.HEAD)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> exists(@PathVariable String interrogationId) {
        if (rawResponseApiPort.existsByInterrogationId(interrogationId)) {
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}
