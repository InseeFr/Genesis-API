package fr.insee.genesis.controller.rest;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.support.CronExpression;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
public class DataProcessingContextController {
    private DataProcessingContextApiPort dataProcessingContextApiPort;
    private final FileUtils fileUtils;

    @Deprecated(forRemoval = true)
    @Operation(summary = "Create or update a data processing context")
    @PutMapping(path = "/context/review")
    @PreAuthorize("hasAnyRole('USER_PLATINE', 'USER_BACK_OFFICE', 'SCHEDULER')")
    public ResponseEntity<Object> saveContext(
            @Parameter(description = "Identifier of the partition", required = true) @RequestParam("partitionId") String partitionId,
            @Parameter(description = "Allow reviewing") @RequestParam(value = "withReview", defaultValue = "false") Boolean withReview
    ){
        try {
            withReview = withReview != null && withReview; //False if null
            dataProcessingContextApiPort.saveContext(partitionId, withReview);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Create or update a data processing context")
    @PutMapping(path = "/contexts/{collectionInstrumentId}/review")
    @PreAuthorize("hasAnyRole('USER_PLATINE', 'USER_BACK_OFFICE', 'SCHEDULER')")
    public ResponseEntity<Object> saveContextWithCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId,
            @Parameter(description = "Allow reviewing") @RequestParam(value = "withReview", defaultValue = "false") Boolean withReview
    ){
        try {
            withReview = withReview != null && withReview; //False if null
            dataProcessingContextApiPort.saveContextByCollectionInstrumentId(collectionInstrumentId, withReview);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Returns partition review indicator")
    @GetMapping(path = "/contexts/{collectionInstrumentId}/review")
    @PreAuthorize("hasAnyRole('USER_BACK_OFFICE','SCHEDULER','USER_PLATINE')")
    public ResponseEntity<Object> getReviewIndicatorByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ){
        try {
            boolean withReview = dataProcessingContextApiPort.getReviewByCollectionInstrumentId(collectionInstrumentId);
            return ResponseEntity.ok(withReview);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @Deprecated(forRemoval = true)
    @Operation(summary = "Returns partition review indicator")
    @GetMapping(path = "/context/review")
    @PreAuthorize("hasAnyRole('USER_BACK_OFFICE','SCHEDULER','USER_PLATINE')")
    public ResponseEntity<Object> getReviewIndicator(
            @Parameter(description = "Identifier of the partition", required = true) @RequestParam("partitionId") String partitionId
    ){
        try {
            boolean withReview = dataProcessingContextApiPort.getReviewByPartitionId(partitionId);
            return ResponseEntity.ok(withReview);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @Deprecated(forRemoval = true)
    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/context/schedules")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> saveSchedule(
            @Parameter(description = "Partition identifier to call Kraftwerk on") @RequestParam("partitionId") String partitionId,
            @Parameter(description = "Kraftwerk endpoint") @RequestParam(value = "serviceTocall", defaultValue = Constants.KRAFTWERK_MAIN_ENDPOINT) ServiceToCall serviceToCall,
            @Parameter(description = "Frequency in Spring cron format (6 inputs, go to https://crontab.cronhub.io/ for generator)  \n Example : 0 0 6 * * *") @RequestParam("frequency") String frequency,
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
            //Check frequency
            if(!CronExpression.isValidExpression(frequency)) {
                log.warn("Returned error for wrong frequency : {}", frequency);
                throw new GenesisException(400, "Wrong frequency syntax");
            }

            TrustParameters trustParameters = null;
            if(useEncryption) {
                trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(partitionId),
                        encryptionOutputFolder,
                        encryptionVaultPath,
                        useSignature
                );
            }
            dataProcessingContextApiPort.saveKraftwerkExecutionSchedule(
                    partitionId,
                    serviceToCall == null ? ServiceToCall.MAIN : serviceToCall,
                    frequency,
                    scheduleBeginDate, scheduleEndDate, trustParameters
            );
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    // Should be refactored to make it restfull
    @Operation(summary = "Schedule a Kraftwerk execution using the collection instrument")
    @PutMapping(path = "/contexts/schedules")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> saveScheduleWithCollectionInstrumentId(
            @Parameter(description = "Collection instrument to call Kraftwerk on") @RequestParam("collectionInstrumentId") String collectionInstrumentId,
            @Parameter(description = "Kraftwerk endpoint") @RequestParam(value = "serviceTocall", defaultValue = Constants.KRAFTWERK_MAIN_ENDPOINT) ServiceToCall serviceToCall,
            @Parameter(description = "Frequency in Spring cron format (6 inputs, go to https://crontab.cronhub.io/ for generator)  \n Example : 0 0 6 * * *") @RequestParam("frequency") String frequency,
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
            //Check frequency
            if(!CronExpression.isValidExpression(frequency)) {
                log.warn("Returned error for wrong frequency : {}", frequency);
                throw new GenesisException(400, "Wrong frequency syntax");
            }

            TrustParameters trustParameters = null;
            if(useEncryption) {
                trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(collectionInstrumentId),
                        encryptionOutputFolder,
                        encryptionVaultPath,
                        useSignature
                );
            }
            dataProcessingContextApiPort.saveKraftwerkExecutionScheduleByCollectionInstrumentId(
                    collectionInstrumentId,
                    serviceToCall == null ? ServiceToCall.MAIN : serviceToCall,
                    frequency,
                    scheduleBeginDate, scheduleEndDate, trustParameters
            );
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    @Deprecated(forRemoval = true)
    @Operation(summary = "Fetch all schedules")
    @GetMapping(path = "/context/schedules")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getAllSchedules() {
        log.debug("Got GET all schedules request");

        List<ScheduleDto> surveyScheduleDocumentModels = dataProcessingContextApiPort.getAllSchedules();

        log.info("Returning {} schedule documents...", surveyScheduleDocumentModels.size());
        return ResponseEntity.ok(surveyScheduleDocumentModels);
    }

    //It is just a change of path in the url
    @Operation(summary = "Fetch all schedules")
    @GetMapping(path = "/contexts/schedules")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getAllSchedulesV2() {
        log.debug("Got GET all schedules request");

        List<ScheduleDto> surveyScheduleDocumentModels = dataProcessingContextApiPort.getAllSchedules();

        log.info("Returning {} schedule documents...", surveyScheduleDocumentModels.size());
        return ResponseEntity.ok(surveyScheduleDocumentModels);
    }


    @Deprecated(forRemoval = true)
    @Operation(summary = "Set last execution date of a partition with new date or nothing")
    @PostMapping(path = "/context/schedules/lastExecutionDate")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> setSurveyLastExecution(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestBody String partitionId,
            @Parameter(description = "Date to save as last execution date", example = "2024-01-01T12:00:00") @RequestParam("newDate") LocalDateTime newDate
    ) {
        try {
            dataProcessingContextApiPort.updateLastExecutionDate(partitionId, newDate);
            log.info("{} last execution updated at {} !", partitionId, newDate);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Update the date of the last extraction of data corresponding to a collection instrument")
    @PutMapping(path = "/contexts/{collectionInstrumentId}/lastExecutionDate")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> setSurveyLastExecutionByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") @RequestBody String collectionInstrumentId,
            @Parameter(description = "Date to save as last execution date", example = "2024-01-01T12:00:00") @RequestParam("newDate") LocalDateTime newDate
    ) {
        try {
            dataProcessingContextApiPort.updateLastExecutionDateByCollectionInstrumentId(collectionInstrumentId, newDate);
            log.info("{} last execution updated at {} !", collectionInstrumentId, newDate);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        return ResponseEntity.ok().build();
    }

    @Deprecated(forRemoval = true)
    @Operation(summary = "Delete the Kraftwerk execution schedules of a partition")
    @DeleteMapping(path = "/context/schedules")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteSchedules(
            @Parameter(description = "Survey name of the schedule(s) to delete") @RequestParam("partitionId") String partitionId
    ){
        try {
            dataProcessingContextApiPort.deleteSchedules(partitionId);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        log.info("Schedule deleted for survey {}", partitionId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete the Kraftwerk execution schedules of a collection instrument id")
    @DeleteMapping(path = "/context/{collectionInstrumentId}/schedules")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteSchedulesByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ){
        try {
            dataProcessingContextApiPort.deleteSchedulesByCollectionInstrumentId(collectionInstrumentId);
        }catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        log.info("Schedule deleted for survey {}", collectionInstrumentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete expired schedules")
    @DeleteMapping(path = "/context/schedules/expired-schedules")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> deleteExpiredSchedules(){
        try{
            dataProcessingContextApiPort.deleteExpiredSchedules(fileUtils.getLogFolder());
        } catch (GenesisException e){
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        log.info("Expired schedules deleted");
        return ResponseEntity.ok().build();
    }
}
