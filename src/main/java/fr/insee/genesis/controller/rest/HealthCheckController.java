package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.ScheduleApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health-check")
@RestController
public class HealthCheckController {
    private final SurveyUnitUpdateApiPort surveyUnitUpdateApiPort;
    private final ScheduleApiPort scheduleApiPort;
    @Value("${fr.insee.genesis.version}")
    private String projectVersion;

    @Autowired
    public HealthCheckController(SurveyUnitUpdateApiPort surveyUnitUpdateApiPort, ScheduleApiPort scheduleApiPort) {
        this.surveyUnitUpdateApiPort = surveyUnitUpdateApiPort;
        this.scheduleApiPort = scheduleApiPort;
    }

    @GetMapping("")
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok(
                """
                             OK
                             
                             Version %s
                             User %s
                        """
                        .formatted(
                                projectVersion,
                                SecurityContextHolder.getContext().getAuthentication().getName()
                        ));
    }

    @GetMapping("mongoDb")
    public ResponseEntity<String> healthcheckMongo() {
        return ResponseEntity.ok(
                """
                             MongoDB OK
                             
                             %s Responses
                             %s Schedules
                        """
                        .formatted(
                                surveyUnitUpdateApiPort.countResponses(),
                                scheduleApiPort.countSchedules()
                        ));
    }

}
