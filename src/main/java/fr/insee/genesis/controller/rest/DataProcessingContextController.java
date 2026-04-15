package fr.insee.genesis.controller.rest;

import fr.insee.genesis.controller.dto.KraftwerkExecutionScheduleInput;
import fr.insee.genesis.controller.dto.ScheduleRequestDto;
import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@AllArgsConstructor
@Slf4j
public class DataProcessingContextController {
    private DataProcessingContextApiPort dataProcessingContextApiPort;
    private final FileUtils fileUtils;

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

    @Operation(summary = "Create a Kraftwerk execution schedule V2")
    @PostMapping(path = "/contexts/schedules/v2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> createScheduleV2(
            @Valid @RequestBody ScheduleRequestDto request
    ) {
        try {
            TrustParameters trustParameters = null;
            if (request.isUseAsymmetricEncryption()) {
                trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(request.getCollectionInstrumentId()),
                        "", // temporary folder for workflow
                        request.getEncryptionVaultPath(),
                        request.isUseSignature()
                );
            }

            KraftwerkExecutionScheduleInput scheduleInput = KraftwerkExecutionScheduleInput.builder()
                    .collectionInstrumentId(request.getCollectionInstrumentId())
                    .exportType(request.getExportType())
                    .frequency(request.getFrequency())
                    .startDate(request.getScheduleBeginDate())
                    .endDate(request.getScheduleEndDate())
                    .mode(request.getMode())
                    .destinationType(request.getDestinationType())
                    .addStates(request.isAddStates())
                    .destinationFolder(request.getDestinationFolder())
                    .trustParameters(trustParameters)
                    .batchSize(request.getBatchSize())
                    .build();

            String scheduleUuid = dataProcessingContextApiPort.createKraftwerkExecutionSchedule(scheduleInput);

            return ResponseEntity.ok(scheduleUuid);

        } catch (GenesisException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
    }

    @Operation(summary = "Update a Kraftwerk execution schedule V2")
    @PutMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> updateScheduleV2(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId,
            @PathVariable("scheduleUuid") String scheduleUuid,
            @Valid @RequestBody ScheduleRequestDto request
    ) {
        try {
            TrustParameters trustParameters = null;
            if (request.isUseAsymmetricEncryption()) {
                trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(collectionInstrumentId),
                        "",
                        request.getEncryptionVaultPath(),
                        request.isUseSignature()
                );
            }

            KraftwerkExecutionScheduleInput scheduleInput = KraftwerkExecutionScheduleInput.builder()
                    .collectionInstrumentId(collectionInstrumentId)
                    .scheduleUuid(scheduleUuid)
                    .exportType(request.getExportType())
                    .frequency(request.getFrequency())
                    .startDate(request.getScheduleBeginDate())
                    .endDate(request.getScheduleEndDate())
                    .mode(request.getMode())
                    .destinationType(request.getDestinationType())
                    .addStates(request.isAddStates())
                    .destinationFolder(request.getDestinationFolder())
                    .useAsymmetricEncryption(request.isUseAsymmetricEncryption())
                    .useSymmetricEncryption(request.isUseSymmetricEncryption())
                    .trustParameters(trustParameters)
                    .batchSize(request.getBatchSize())
                    .build();

            dataProcessingContextApiPort.updateKraftwerkExecutionSchedule(scheduleInput);

        } catch (GenesisException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }

        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Fetch all schedules V2")
    @GetMapping(path = "/contexts/schedules/v2")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getAllSchedulesV2() {
        log.debug("Got GET all schedules V2 request");

        List<ScheduleResponseDto> schedules = dataProcessingContextApiPort.getAllSchedulesV2();

        log.info("Returning {} V2 schedule documents...", schedules.size());
        return ResponseEntity.ok(schedules);
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
    
    @Operation(summary = "Fetch V2 schedules by collection instrument id")
    @GetMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getSchedulesV2ByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ) {
        List<ScheduleResponseDto> schedules =
                dataProcessingContextApiPort.getSchedulesV2ByCollectionInstrumentId(collectionInstrumentId);

        return ResponseEntity.ok(schedules);
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

    @Operation(summary = "Delete all V2 Kraftwerk execution schedules of a collection instrument id")
    @DeleteMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteSchedulesV2ByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ){
        try {
            dataProcessingContextApiPort.deleteSchedulesV2ByCollectionInstrumentId(collectionInstrumentId);
        } catch (GenesisException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        log.info("All V2 schedules deleted for collection instrument {}", collectionInstrumentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a V2 Kraftwerk execution schedule")
    @DeleteMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteScheduleV2(
            @PathVariable(value = "collectionInstrumentId") String collectionInstrumentId,
            @PathVariable(value = "scheduleUuid") String scheduleUuid
    ){
        try {
            dataProcessingContextApiPort.deleteScheduleV2(collectionInstrumentId, scheduleUuid);
        } catch (GenesisException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatusCode.valueOf(e.getStatus()));
        }
        log.info("V2 schedule {} deleted for collection instrument {}", scheduleUuid, collectionInstrumentId);
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
