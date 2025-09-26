package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualVariableApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.nio.file.Path;

@RequestMapping(path = "/contextual-variables")
@Controller
@Slf4j
@AllArgsConstructor
public class ContextualVariableController {

    private final ContextualPreviousVariableApiPort contextualPreviousVariableApiPort;
    private final ContextualExternalVariableApiPort contextualExternalVariableApiPort;
    private final ContextualVariableApiPort contextualVariableApiPort;
    private final Config config;

    @Operation(summary = "Get contextual variables (contextual and previous)")
    @GetMapping(path = "/")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> getContextualVariables(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("interrogationId") String interrogationId
    ){
        return ResponseEntity.ok().body(
                contextualVariableApiPort.getContextualVariable(questionnaireId, interrogationId)
        );
    }

    @Operation(summary = "Save all contextual variables json files (contextual and previous)")
    @PostMapping(path = "/json")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> saveContextualVariables(
            @RequestParam("questionnaireId") String questionnaireId
    ){
        try {
            FileUtils fileUtils = new FileUtils(config);
            int fileCount = contextualVariableApiPort.saveContextualVariableFiles(questionnaireId, fileUtils);
            return ResponseEntity.ok("%d file(s) processed for questionnaire %s !".formatted(fileCount, questionnaireId));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    @Operation(summary = "Add contextual previous json file")
    @PostMapping(path = "previous/json")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER','USER_BACK_OFFICE')")
    public ResponseEntity<Object> readContextualPreviousJson(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode,
            @RequestParam(value = "sourceState", required = false) String sourceState,
            @RequestParam(value = "jsonFileName") String jsonFileName
    ){
        try {
            FileUtils fileUtils = new FileUtils(config);

            String filePath = "%s/%s".formatted(
                    fileUtils.getDataFolder(questionnaireId, mode.getFolder(), null),
                    jsonFileName
            );
            if (!jsonFileName.toLowerCase().endsWith(".json")) {
                throw new GenesisException(400, "File must be a JSON file !");
            }
            contextualPreviousVariableApiPort.readContextualPreviousFile(questionnaireId.toUpperCase(), sourceState, filePath);
            moveFile(questionnaireId, mode, fileUtils, filePath);
            return ResponseEntity.ok("Contextual previous variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    @Operation(summary = "Add contextual external json file")
    @PostMapping(path = "/external/json")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER','USER_BACK_OFFICE')")
    public ResponseEntity<Object> readContextualExternalJson(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("mode") Mode mode,
            @RequestParam(value = "jsonFileName") String jsonFileName
    ){
        try {
            FileUtils fileUtils = new FileUtils(config);

            String filePath = "%s/%s".formatted(
                    fileUtils.getDataFolder(questionnaireId, mode.getFolder(), null),
                    jsonFileName
            );
            if (!jsonFileName.toLowerCase().endsWith(".json")) {
                throw new GenesisException(400, "File must be a JSON file !");
            }
            contextualExternalVariableApiPort.readContextualExternalFile(questionnaireId, filePath);
            moveFile(questionnaireId, mode, fileUtils, filePath);
            return ResponseEntity.ok("Contextual external variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    private static void moveFile(String questionnaireId, Mode mode, FileUtils fileUtils, String filePath) throws GenesisException {
        try {
            fileUtils.moveFiles(Path.of(filePath), fileUtils.getDoneFolder(questionnaireId, mode.getFolder()));
        } catch (IOException e) {
            throw new GenesisException(500, "Error while moving file to done : %s".formatted(e.toString()));
        }
    }
}
