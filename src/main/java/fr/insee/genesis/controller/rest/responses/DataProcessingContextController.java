package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;

@RequestMapping(path = "/contexts" )
@Controller
@AllArgsConstructor
@Slf4j
public class DataProcessingContextController {
    private DataProcessingContextApiPort dataProcessingContextApiPort;
    private final FileUtils fileUtils;

    @Operation(summary = "Create or update a data processing context")
    @PutMapping(path = "/create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Object> saveContext(
            @Parameter(description = "Identifier of the partition", required = true) @RequestParam("partitionId") String partitionId,
            @Parameter(description = "Allow reviewing") @RequestParam(value = "withReview", defaultValue = "false") Boolean withReview
    ) {
        try {
            withReview = withReview != null && withReview; //False if null
            dataProcessingContextApiPort.saveContext(partitionId, withReview);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/schedule/create")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> saveSchedule(
            @Parameter(description = "Partition identifier to call Kraftwerk on") @RequestParam("partitionId") String partitionId,
            @Parameter(description = "Frequency in Spring cron format (6 inputs, go to https://crontab.cronhub.io/ for generator)  \n Example : 0 0 6 * * *") @RequestParam("frequency") String frequency,
            @Parameter(description = "Kraftwerk endpoint") @RequestParam(value = "serviceTocall", defaultValue = Constants.KRAFTWERK_MAIN_ENDPOINT) ServiceToCall serviceToCall,
            @Parameter(description = "Schedule effective date and time", example = "2024-01-01T12:00:00") @RequestParam("scheduleBeginDate") LocalDateTime scheduleBeginDate,
            @Parameter(description = "Schedule end date and time", example = "2024-01-01T12:00:00") @RequestParam("scheduleEndDate") LocalDateTime scheduleEndDate,
            @Parameter(description = "Encrypt after process ? Ignore next parameters if false") @RequestParam(value =
                    "useEncryption",
                    defaultValue = "false") boolean useEncryption,
            @Parameter(description = "(Encryption) vault path") @RequestParam(value = "encryptionVaultPath", defaultValue = "") String encryptionVaultPath,
            @Parameter(description = "(Encryption) output folder") @RequestParam(value = "encryptionOutputFolder",
                    defaultValue = "") String encryptionOutputFolder,
            @Parameter(description = "(Encryption) Use signature system") @RequestParam(value = "useSignature", defaultValue = "false") boolean useSignature
    ) {
        try {
            TrustParameters trustParameters = null;
            if(useEncryption) {
                trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(partitionId),
                        encryptionOutputFolder,
                        encryptionVaultPath,
                        useSignature
                );
            }
            dataProcessingContextApiPort.saveKraftwerkExecutionSchedule(partitionId, frequency, serviceToCall,
                    scheduleBeginDate, scheduleEndDate, trustParameters);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }
}
