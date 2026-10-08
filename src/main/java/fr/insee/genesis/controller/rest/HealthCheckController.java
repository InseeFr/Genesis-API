package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.healthcheck.FileSystemHealthCheckResult;
import fr.insee.genesis.domain.ports.api.DataProcessingContextApiPort;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.healthcheck.FileSystemHealthCheckService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health-check")
@RestController
@RequiredArgsConstructor
public class HealthCheckController implements CommonApiResponse{
    private final SurveyUnitApiPort surveyUnitApiPort;
    private final DataProcessingContextApiPort dataProcessingContextApiPort;

    private final FileSystemHealthCheckService fileSystemHealthCheckService;

    @Value("${fr.insee.genesis.version}")
    private String projectVersion;

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
                             %s Contexts
                       \s"""
                        .formatted(
                                surveyUnitApiPort.countResponses(),
                                dataProcessingContextApiPort.countContexts()
                        ));
    }

    @GetMapping("fileSystem")
    public ResponseEntity<String> healthcheckFileSystem() {
        FileSystemHealthCheckResult fileSystemHealthCheckResult = fileSystemHealthCheckService.check();
        if(fileSystemHealthCheckResult.isOK()){
            return ResponseEntity.ok(fileSystemHealthCheckService.check().toString());
        }
        return ResponseEntity.internalServerError().body(fileSystemHealthCheckService.check().toString());
    }

}
