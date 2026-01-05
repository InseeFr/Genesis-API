package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.domain.service.rawdata.CombinedRawDataService;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping(path = "/combined-raw-data" )
@RequiredArgsConstructor
public class CombinedRawDataController {

    private static final String INTERROGATION_ID = "interrogationId";
    private final CombinedRawDataService combinedRawDataService;


    @Operation(summary = "Retrieve combined raw responses and Lunatic raw data for a given interrogationId")
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'USER_PLATINE')")
    public ResponseEntity<CombinedRawDataDto> getCombinetRawData(
            @RequestParam(INTERROGATION_ID) String interrogationId
    ){
        CombinedRawDataDto data = combinedRawDataService.getCombinedRawDataByInterrogationId(interrogationId);

        if (data.rawResponseModels().isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        return ResponseEntity.ok(data);
    }

    }
