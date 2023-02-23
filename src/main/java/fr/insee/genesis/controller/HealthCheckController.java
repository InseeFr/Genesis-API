package fr.insee.genesis.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.BuildProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.Optional;

@RequestMapping("/health-check")
@Controller
@ResponseBody
public class HealthCheckController {

        @Autowired(required = false)
        private Optional<BuildProperties> buildProperties;

        @GetMapping("")
        public ResponseEntity<String> healthcheck(){
                return ResponseEntity.ok(
                    """
                         OK
                         
                         Version %s
                         User %s
                    """
                        .formatted(
                            buildProperties.map(BuildProperties::getVersion).orElse("n.a"),
                            SecurityContextHolder.getContext().getAuthentication().getName()
                        )                );
        }

}
