package fr.insee.genesis.controller.rest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ScheduleModel;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.InvalidCronExpressionException;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
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

@RequestMapping(path = "/schedule")
@Controller
@Slf4j
public class ScheduleController implements CommonApiResponse{

    private final ScheduleApiPort scheduleApiPort;
    private final FileUtils fileUtils;

    public ScheduleController(ScheduleApiPort scheduleApiPort, FileUtils fileUtils) {
        this.scheduleApiPort = scheduleApiPort;
        this.fileUtils = fileUtils;
    }

    @Operation(summary = "Fetch all schedules")
    @GetMapping(path = "/all")
    @PreAuthorize("hasAnyRole('SCHEDULER','READER')")
    public ResponseEntity<Object> getAllSchedules() {
        log.debug("Got GET all schedules request");

        List<ScheduleModel> surveyScheduleDocumentModels = scheduleApiPort.getAllSchedules();

        log.info("Returning {} schedule documents...", surveyScheduleDocumentModels.size());
        return ResponseEntity.ok(surveyScheduleDocumentModels);
    }

    @Operation(summary = "Schedule a Kraftwerk execution")
    @PutMapping(path = "/create")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> addSchedule(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestParam("surveyName") String surveyName,
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
    ){
        try {
            if(useEncryption){
                TrustParameters trustParameters = new TrustParameters(
                        fileUtils.getKraftwerkOutFolder(surveyName),
                        encryptionOutputFolder,
                        encryptionVaultPath,
                        useSignature
                );
                scheduleApiPort.addSchedule(surveyName,
                        serviceToCall == null ? ServiceToCall.MAIN : serviceToCall,
                        frequency,
                        scheduleBeginDate,
                        scheduleEndDate,
                        trustParameters);
            }else{
                scheduleApiPort.addSchedule(surveyName,
                        serviceToCall == null ? ServiceToCall.MAIN : serviceToCall,
                        frequency,
                        scheduleBeginDate,
                        scheduleEndDate,
                        null);
            }

        }catch (InvalidCronExpressionException e){
            log.warn("Returned error for wrong frequency : {}", frequency);
            return ResponseEntity.badRequest().body("Wrong frequency syntax");
        }
        log.info("New schedule created for survey {}", surveyName);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete a Kraftwerk execution schedule(s) by its survey name")
    @DeleteMapping(path = "/delete")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> deleteSchedule(
            @Parameter(description = "Survey name of the schedule(s) to delete") @RequestParam("surveyName") String surveyName
    ){
        try {
            scheduleApiPort.deleteSchedule(surveyName);
        }catch (NotFoundException e){
            log.warn("Survey {} not found for deletion !", surveyName);
            return ResponseEntity.notFound().build();
        }
        log.info("Schedule deleted for survey {}", surveyName);
        return ResponseEntity.ok().build();
    }


    @Operation(summary = "Set last execution date with new date or empty")
    @PostMapping(path = "/setLastExecutionDate")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> setSurveyLastExecution(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestBody String surveyName,
            @Parameter(description = "Date to save as last execution date", example = "2024-01-01T12:00:00") @RequestParam("newDate") LocalDateTime newDate
            ) {
        try {
            scheduleApiPort.updateLastExecutionName(surveyName, newDate);
            log.info("{} last execution updated at {} !", surveyName, newDate);
        }catch (NotFoundException e){
            log.warn("Survey {} not found for setting last execution !", surveyName);
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Delete expired schedules")
    @DeleteMapping(path = "/delete/expired-schedules")
    @PreAuthorize("hasRole('SCHEDULER')")
    public ResponseEntity<Object> deleteExpiredSchedules() throws NotFoundException, IOException {
        Set<String> storedSurveySchedulesNames = new HashSet<>();
        for(ScheduleModel scheduleModel : scheduleApiPort.getAllSchedules()){
            storedSurveySchedulesNames.add(scheduleModel.getSurveyName());
        }
        for (String surveyScheduleName : storedSurveySchedulesNames) {
            List<KraftwerkExecutionSchedule> deletedKraftwerkExecutionSchedules = scheduleApiPort.deleteExpiredSchedules(surveyScheduleName);
            //Save in JSON log
            if(!deletedKraftwerkExecutionSchedules.isEmpty()) {
                Path jsonLogPath = Path.of(fileUtils.getLogFolder(), Constants.SCHEDULE_ARCHIVE_FOLDER_NAME,
                        surveyScheduleName + ".json");
                ObjectMapper objectMapper = new ObjectMapper();
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
