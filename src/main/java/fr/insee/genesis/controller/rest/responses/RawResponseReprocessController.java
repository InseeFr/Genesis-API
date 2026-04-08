package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.api.ReprocessRawResponseApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Controller
@RequiredArgsConstructor
@Slf4j
public class RawResponseReprocessController {

    private final ReprocessRawResponseApiPort reprocessRawResponseApiPort;

    @Operation(summary = "Reprocess raw response of a collection instrument.")
    @PostMapping(path = "/raw-responses/{collectionInstrumentId}/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reProcessRawResponsesByCollectionInstrumentId(
            @Parameter(
                    description = "Id of the collection instrument (old questionnaireId)",
                    example = "ENQTEST2025X00")
            @PathVariable("collectionInstrumentId")
            String collectionInstrumentId,

            @Parameter(description = "Extract since",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T00:00:00"))
            @RequestParam(value = "sinceDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime sinceDate,

            @Parameter(description = "Extract until",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-02-02T00:00:00"))
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) throws GenesisException {

        DataProcessResult result = reprocessRawResponseApiPort.reprocessRawResponses(
                RawDataModelType.FILIERE,
                collectionInstrumentId,
                sinceDate,
                endDate);

        return ResponseEntity.ok(result.message(collectionInstrumentId));
    }

    @Operation(summary = "Reprocess Lunatic raw data for a questionnaire model. " +
            "**Note**: Lunatic raw data is the legacy format of raw responses.")
    @PostMapping(path = "/responses/raw/lunatic-json/{questionnaireId}/reprocess")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> reProcessJsonRawDataByQuestionnaireId(
            @Parameter(
                    description = "Questionnaire model id (old name for collection instrument id).",
                    example = "ENQTEST2025X00")
            @PathVariable("questionnaireId")
            String collectionInstrumentId, // 'questionnaireId' is the legacy name for 'collectionInstrumentId'

            @Parameter(description = "Extract since",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-01-01T00:00:00"))
            @RequestParam(value = "sinceDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime sinceDate,

            @Parameter(description = "Extract until",
                    schema = @Schema(type = "string", format = "date-time", example = "2026-02-02T00:00:00"))
            @RequestParam(value = "endDate", required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime endDate
    ) throws GenesisException {

        DataProcessResult result = reprocessRawResponseApiPort.reprocessRawResponses(
                RawDataModelType.LEGACY,
                collectionInstrumentId,
                sinceDate,
                endDate);

        return ResponseEntity.ok(result.message(collectionInstrumentId));
    }

}
