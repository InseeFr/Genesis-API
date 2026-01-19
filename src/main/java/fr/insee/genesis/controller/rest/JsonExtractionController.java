package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.LastExtractionResponseDto;
import fr.insee.genesis.domain.model.extraction.json.LastJsonExtractionModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LastJsonExtractionApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@Slf4j
@Controller
@AllArgsConstructor
@RequestMapping(path = "/extractions")
public class JsonExtractionController {

    LastJsonExtractionApiPort lastJsonExtractionApiPort;

    @Operation(summary = "Record the date of the latest JSON data extraction in Kraftwerk")
    @PutMapping(path = "/json")
    @PreAuthorize("hasAnyRole('USER_KRAFTWERK','SCHEDULER')")
    public ResponseEntity<String> saveLastJsonExtractionDate(
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode){
        LocalDateTime extractDate = LocalDateTime.now();
        LastJsonExtractionModel extract = LastJsonExtractionModel.builder()
                .collectionInstrumentId(collectionInstrumentId)
                .mode(mode)
                .lastExtractionDate(extractDate)
                .build();
        lastJsonExtractionApiPort.recordDate(extract);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Get the date of the latest JSON data extraction in Kraftwerk")
    @GetMapping(path = "/json")
    @PreAuthorize("hasAnyRole('USER_KRAFTWERK','SCHEDULER')")
    public ResponseEntity<LastExtractionResponseDto> getLastJsonExtractionDate(
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode){
        try{
            LastJsonExtractionModel lastJsonExtraction = lastJsonExtractionApiPort.getLastExtractionDate(collectionInstrumentId,mode);
            return ResponseEntity.ok(new LastExtractionResponseDto(lastJsonExtraction.getLastExtractionDate()));
        } catch (GenesisException e){
            return ResponseEntity.notFound().build();
        }
    }

    @Operation(summary = "Reset latest JSON data extraction")
    @DeleteMapping(path = "/json")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> deleteJsonExtractionDate(
            @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @RequestParam(value = "mode", required = false) Mode mode){
        try {
            lastJsonExtractionApiPort.delete(collectionInstrumentId, mode);
            return ResponseEntity.ok().build();
        } catch (GenesisException e){
            return ResponseEntity.status(e.getStatus()).build();
        }
    }

}
