package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health-check")
@RestController
public class HealthCheckController implements CommonApiResponse{
    private final SurveyUnitApiPort surveyUnitApiPort;
    private final DataProcessingContextApiPort dataProcessingContextApiPort;
    @Value("${fr.insee.genesis.version}")
    private String projectVersion;


    public HealthCheckController(SurveyUnitApiPort surveyUnitApiPort, DataProcessingContextApiPort scheduleApiPort) {
        this.surveyUnitApiPort = surveyUnitApiPort;
        this.dataProcessingContextApiPort = scheduleApiPort;
    }

    @GetMapping("")
    public ResponseEntity<String> healthcheck() {
        return ResponseEntity.ok(
                """
                             OK
                            \s
                             Version %s
                             User %s
                       \s"""
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
                            \s
                             %s Responses
                             %s Schedules
                       \s"""
                        .formatted(
                                surveyUnitApiPort.countResponses(),
                                dataProcessingContextApiPort.countSchedules()
                        ));
    }

}
