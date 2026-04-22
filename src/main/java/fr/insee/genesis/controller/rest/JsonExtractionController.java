package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.LastExtractionRequest;
import fr.insee.genesis.controller.dto.LastExtractionResponseDto;
import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LastJsonExtractionApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping(path = "/collection-instruments")
public class JsonExtractionController {

    LastJsonExtractionApiPort lastJsonExtractionApiPort;

    @Operation(summary = "Record the date of the latest JSON data extraction in Kraftwerk")
    @PutMapping(path = "/{collectionInstrumentId}/extractions/json/last")
    @PreAuthorize("hasAnyRole('USER_KRAFTWERK','SCHEDULER')")
    public ResponseEntity<String> saveLastJsonExtractionDate(
            @PathVariable String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode,
            @RequestBody @Valid LastExtractionRequest request){
        LastJsonExtractionModel extract = LastJsonExtractionModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .mode(mode)
                .lastExtractionDate(request.getLastExtractionDate())
                .build();

        lastJsonExtractionApiPort.recordDate(extract);

        return ResponseEntity.ok().build();
    }



    @Operation(summary = "Get the date of the latest JSON data extraction in Kraftwerk")
    @GetMapping(path = "/{collectionInstrumentId}/extractions/json/last")
    @PreAuthorize("hasAnyRole('USER_KRAFTWERK','SCHEDULER')")
    public ResponseEntity<LastExtractionResponseDto> getLastJsonExtractionDate(
            @PathVariable String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode) throws GenesisException{

        LastJsonExtractionModel lastJsonExtraction = lastJsonExtractionApiPort.getLastExtractionDate(collectionInstrumentId,mode);
        return ResponseEntity.ok(new LastExtractionResponseDto(lastJsonExtraction.getLastExtractionDate()));
    }

    @Operation(summary = "Reset latest JSON data extraction")
    @DeleteMapping(path = "/{collectionInstrumentId}/extractions/json/last")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteJsonExtractionDate(
            @PathVariable String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode) throws GenesisException{

        lastJsonExtractionApiPort.delete(collectionInstrumentId, mode);
        return ResponseEntity.ok().build();
    }

}
