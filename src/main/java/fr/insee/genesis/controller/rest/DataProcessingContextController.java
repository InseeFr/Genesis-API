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
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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
            @Parameter(description = "Allow reviewing")
            @RequestParam(value = "withReview", defaultValue = "false") Boolean withReview
    ) throws GenesisException {
        dataProcessingContextApiPort.saveContextByCollectionInstrumentId(collectionInstrumentId, withReview);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Returns partition review indicator")
    @GetMapping(path = "/contexts/{collectionInstrumentId}/review")
    @PreAuthorize("hasAnyRole('USER_BACK_OFFICE','SCHEDULER','USER_PLATINE')")
    public ResponseEntity<Object> getReviewIndicatorByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ) throws GenesisException {
        boolean withReview = dataProcessingContextApiPort.getReviewByCollectionInstrumentId(collectionInstrumentId);
        return ResponseEntity.ok(withReview);
    }

    @Operation(summary = "Create a Kraftwerk execution schedule V2")
    @PostMapping(path = "/contexts/schedules/v2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> createScheduleV2(
            @Valid @RequestBody ScheduleRequestDto request
    ) throws GenesisException{
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

    }

    @Operation(summary = "Update a Kraftwerk execution schedule V2")
    @PutMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> updateScheduleV2(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId,
            @PathVariable("scheduleUuid") String scheduleUuid,
            @Valid @RequestBody ScheduleRequestDto request
    ) throws GenesisException{

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
    ) throws GenesisException{

        dataProcessingContextApiPort.deleteSchedulesByCollectionInstrumentId(collectionInstrumentId);
        log.info("Schedule deleted for survey {}", collectionInstrumentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete all V2 Kraftwerk execution schedules of a collection instrument id")
    @DeleteMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteSchedulesV2ByCollectionInstrumentId(
            @PathVariable("collectionInstrumentId") String collectionInstrumentId
    ) throws GenesisException{

        dataProcessingContextApiPort.deleteSchedulesV2ByCollectionInstrumentId(collectionInstrumentId);

        log.info("All V2 schedules deleted for collection instrument {}", collectionInstrumentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a V2 Kraftwerk execution schedule")
    @DeleteMapping(path = "/contexts/{collectionInstrumentId}/schedules/v2/{scheduleUuid}")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteScheduleV2(
            @PathVariable(value = "collectionInstrumentId") String collectionInstrumentId,
            @PathVariable(value = "scheduleUuid") String scheduleUuid
    ) throws GenesisException{

        dataProcessingContextApiPort.deleteScheduleV2(collectionInstrumentId, scheduleUuid);

        log.info("V2 schedule {} deleted for collection instrument {}", scheduleUuid, collectionInstrumentId);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete expired schedules")
    @DeleteMapping(path = "/context/schedules/expired-schedules")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> deleteExpiredSchedules() throws GenesisException{

        dataProcessingContextApiPort.deleteExpiredSchedules(fileUtils.getLogFolder());
        log.info("Expired schedules deleted");
        return ResponseEntity.ok().build();
    }
}
