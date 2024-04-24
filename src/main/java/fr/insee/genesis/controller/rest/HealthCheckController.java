package fr.insee.genesis.controller.rest;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/health-check")
@RestController
public class HealthCheckController {
    @Value("${fr.insee.genesis.version}")
    private String projectVersion;

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

}
