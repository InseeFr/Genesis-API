package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.api.RundeckExecutionApiPort;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@RequestMapping(path = "/rundeck-execution")
@Controller
@Slf4j
public class RundeckExecutionController {

    private final RundeckExecutionApiPort rundeckExecutionApiPort;

    @Autowired
    public RundeckExecutionController(RundeckExecutionApiPort rundeckExecutionApiPort) {
        this.rundeckExecutionApiPort = rundeckExecutionApiPort;
    }

    @Operation(summary = "Register a Rundeck execution")
    @PostMapping(path = "/save")
    public ResponseEntity<Object> addRundeckExecution(
            @Parameter(description = "Survey name to call Kraftwerk on") @RequestBody RundeckExecution rundeckExecution
    ){
        try{
            rundeckExecutionApiPort.addExecution(rundeckExecution);
            log.info("{} job saved", rundeckExecution.getJob().getName());
        } catch(Exception e){
            log.info("Rundeck execution was not saved in database");
            return ResponseEntity.internalServerError().build();
        }
        return ResponseEntity.ok().build();
    }

}
