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

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.stream.Stream;

@RequestMapping(path = "/contextual-variable")
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
            int fileCount = 0;

            for (Mode mode : Mode.values()) {
                try (Stream<Path> filePaths = Files.list(Path.of(fileUtils.getDataFolder(questionnaireId,
                        mode.getFolder()
                        , null)))) {
                    Iterator<Path> it = filePaths
                            .filter(path -> path.toString().endsWith(".json"))
                            .iterator();
                    while (it.hasNext()) {
                        Path jsonFilePath = it.next();
                        if (processContextualFile(questionnaireId, jsonFilePath)) {
                            //If the file is indeed a contextual variables file and had been processed
                            moveFile(questionnaireId, mode, fileUtils, jsonFilePath.toString());
                            fileCount++;
                        }
                    }
                } catch (NoSuchFileException nsfe) {
                    log.debug(nsfe.toString());
                } catch (IOException ioe) {
                    log.warn(ioe.toString());
                }
            }
            return ResponseEntity.ok("%d file(s) processed for questionnaire %s !".formatted(fileCount, questionnaireId));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    /**
     * @return true if any contextual variable part found in file, false otherwise
     */
    private boolean processContextualFile(String questionnaireId, Path jsonFilePath) throws GenesisException {
        return readContextualPreviousFile(questionnaireId.toUpperCase(), null, jsonFilePath.toString())
                || readContextualExternalFile(questionnaireId.toUpperCase(), jsonFilePath.toString());
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
            readContextualPreviousFile(questionnaireId.toUpperCase(), sourceState, filePath);
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
            readContextualExternalFile(questionnaireId.toUpperCase(), filePath);
            moveFile(questionnaireId, mode, fileUtils, filePath);
            return ResponseEntity.ok("Contextual external variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    private boolean readContextualPreviousFile(String questionnaireId, String sourceState, String filePath) throws GenesisException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return contextualPreviousVariableApiPort.readContextualPreviousFile(inputStream, questionnaireId, sourceState);
        } catch (FileNotFoundException e) {
            throw new GenesisException(404, "File %s not found".formatted(filePath));
        } catch (IOException e) {
            throw new GenesisException(500, e.toString());
        }
    }

    private boolean readContextualExternalFile(String questionnaireId, String filePath) throws GenesisException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            return contextualExternalVariableApiPort.readContextualExternalFile(inputStream, questionnaireId);
        } catch (FileNotFoundException e) {
            throw new GenesisException(404, "File %s not found".formatted(filePath));
        } catch (IOException e) {
            throw new GenesisException(500, e.toString());
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
