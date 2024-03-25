package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.exceptions.NotFoundException;
import fr.insee.genesis.infrastructure.model.document.schedule.ScheduleDocument;
import fr.insee.genesis.infrastructure.model.document.schedule.ServiceToCall;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDateTime;
import java.util.List;

@RequestMapping(path = "/schedule")
@Controller
@Slf4j
public class ScheduleController {

    private final ScheduleApiPort scheduleApiPort;

    @Autowired
    public ScheduleController(ScheduleApiPort scheduleApiPort) {
        this.scheduleApiPort = scheduleApiPort;
    }

    @Operation(summary = "Get all Bangles schedules")
    @GetMapping(path = "/all")
    public ResponseEntity<Object> getAllSchedules() {
        log.info("Got GET all schedules request");

        List<ScheduleDocument> scheduleDocuments = scheduleApiPort.getAllSchedules();

        log.info("Returning " + scheduleDocuments.size() + " schedule documents...");

        return new ResponseEntity<>(scheduleDocuments, HttpStatus.OK);
    }

    @Operation(summary = "Create a Bangles schedule")
    @PutMapping(path = "/create")
    public ResponseEntity<Object> addSchedule(
            @RequestParam("surveyName") String surveyName,
            @RequestParam("serviceTocall") ServiceToCall serviceToCall,
            @Parameter(description = "Fréquence sous format spring cron. \n Exemple : 0 0 6 * * *") @RequestParam("frequency") String frequency,
            @Parameter(example = "2023-06-16T12:00:00") @RequestParam("scheduleBeginDate") LocalDateTime scheduleBeginDate,
            @Parameter(example = "2023-06-17T12:00:00") @RequestParam("scheduleEndDate") LocalDateTime scheduleEndDate
    ) {
        scheduleApiPort.addSchedule(surveyName, serviceToCall, frequency, scheduleBeginDate, scheduleEndDate);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @Operation(summary = "Update last execution date to now")
    @PostMapping(path = "/update")
    public ResponseEntity<Object> updateSurveyLastExecution(
            @RequestBody String surveyName
    ) {
        try {
            scheduleApiPort.updateLastExecutionName(surveyName);
        }catch (NotFoundException e){
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
