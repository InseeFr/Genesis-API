package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RequestMapping(path = "/context" )
@Controller
@AllArgsConstructor
@Slf4j
public class DataProcessingContextController {
    private DataProcessingContextApiPort dataProcessingContextApiPort;
    private final FileUtils fileUtils;

    @Operation(summary = "Create or update a data processing context")
    @PutMapping(path = "/review")
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

    @Operation(summary = "Returns partition review indicator")
    @GetMapping(path = "/review")
    @PreAuthorize("hasAnyRole('USER_BACK_OFFICE','SCHEDULER')")
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

    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/schedules")
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

    @Operation(summary = "Fetch all schedules")
    @GetMapping(path = "/schedules")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getAllSchedules() {
        log.debug("Got GET all schedules request");

        List<ScheduleDto> surveyScheduleDocumentModels = dataProcessingContextApiPort.getAllSchedules();

        log.info("Returning {} schedule documents...", surveyScheduleDocumentModels.size());
        return ResponseEntity.ok(surveyScheduleDocumentModels);
    }

    @Operation(summary = "Set last execution date of a partition with new date or nothing")
    @PostMapping(path = "/schedules/lastExecutionDate")
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

    @Operation(summary = "Delete the Kraftwerk execution schedules of a partition")
    @DeleteMapping(path = "/schedules")
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

    @Operation(summary = "Delete expired schedules")
    @DeleteMapping(path = "/schedules/expired-schedules")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> deleteExpiredSchedules() throws GenesisException, IOException {
        Set<String> storedSurveySchedulesNames = new HashSet<>();
        for(ScheduleDto scheduleDto : dataProcessingContextApiPort.getAllSchedules()){
            storedSurveySchedulesNames.add(scheduleDto.surveyName());
        }
        for (String surveyScheduleName : storedSurveySchedulesNames) {
            List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = dataProcessingContextApiPort.deleteExpiredSchedules(surveyScheduleName);
            //Save in JSON log
            if(!deletedKraftwerkExecutionSchedules.isEmpty()) {
                Path jsonLogPath = Path.of(fileUtils.getLogFolder(), Constants.SCHEDULE_ARCHIVE_FOLDER_NAME,
                        surveyScheduleName + ".json");
                ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
                objectMapper.registerModule(new JavaTimeModule());
                String jsonToWrite = objectMapper.writeValueAsString(deletedKraftwerkExecutionSchedules);
                if(Files.exists(jsonLogPath)){
                    //Remove last ] and append survey
                    StringBuilder content = new StringBuilder(Files.readString(jsonLogPath));
                    content.setCharAt(content.length()-1, ',');
                    content.append(jsonToWrite, 1, jsonToWrite.length()-1);
                    content.append(']');
                    Files.write(jsonLogPath, content.toString().getBytes(), StandardOpenOption.TRUNCATE_EXISTING);
                }else {
                    Files.createDirectories(jsonLogPath.getParent());
                    Files.write(jsonLogPath, jsonToWrite.getBytes());
                }
            }
        }
        return ResponseEntity.ok().build();
    }
}
