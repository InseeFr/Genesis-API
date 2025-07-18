package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.EditedExternalResponseApiPort;
import fr.insee.genesis.domain.ports.api.EditedPreviousResponseApiPort;
import fr.insee.genesis.domain.ports.api.EditedResponseApiPort;
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
import java.nio.file.Path;

@RequestMapping(path = "/edited")
@Controller
@Slf4j
@AllArgsConstructor
public class EditedResponseController {

    private final EditedExternalResponseApiPort editedExternalResponseApiPort;
    private final EditedPreviousResponseApiPort editedPreviousResponseApiPort;
    private final EditedResponseApiPort editedResponseApiPort;
    private final Config config;

    @Operation(summary = "Get edited variables (edited and previous)")
    @GetMapping(path = "/")
    @PreAuthorize("hasRole('USER_PLATINE')")
    public ResponseEntity<Object> getEditedResponses(
            @RequestParam("questionnaireId") String questionnaireId,
            @RequestParam("interrogationId") String interrogationId
    ){
        return ResponseEntity.ok().body(
                editedResponseApiPort.getEditedResponse(questionnaireId, interrogationId)
        );
    }

    @Operation(summary = "Add edited previous json file")
    @PostMapping(path = "previous/json")
    @PreAuthorize("hasAnyRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> readEditedPreviousJson(
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
            readEditedPreviousFile(questionnaireId, sourceState, filePath);
            moveFiles(questionnaireId, mode, fileUtils, filePath);
            return ResponseEntity.ok("Edited previous variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    @Operation(summary = "Add edited external json file")
    @PostMapping(path = "/external/json")
    @PreAuthorize("hasRole('USER_PLATINE','SCHEDULER')")
    public ResponseEntity<Object> readEditedExternalJson(
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
            readEditedExternalFile(questionnaireId, filePath);
            moveFiles(questionnaireId, mode, fileUtils, filePath);
            return ResponseEntity.ok("Edited external variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }

    private void readEditedPreviousFile(String questionnaireId, String sourceState, String filePath) throws GenesisException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            editedPreviousResponseApiPort.readEditedPreviousFile(inputStream, questionnaireId, sourceState);
        } catch (FileNotFoundException e) {
            throw new GenesisException(404, "File %s not found".formatted(filePath));
        } catch (IOException e) {
            throw new GenesisException(500, e.toString());
        }
    }

    private void readEditedExternalFile(String questionnaireId, String filePath) throws GenesisException {
        try (InputStream inputStream = new FileInputStream(filePath)) {
            editedExternalResponseApiPort.readEditedExternalFile(inputStream, questionnaireId);
        } catch (FileNotFoundException e) {
            throw new GenesisException(404, "File %s not found".formatted(filePath));
        } catch (IOException e) {
            throw new GenesisException(500, e.toString());
        }
    }

    private static void moveFiles(String questionnaireId, Mode mode, FileUtils fileUtils, String filePath) throws GenesisException {
        try {
            fileUtils.moveFiles(Path.of(filePath), fileUtils.getDoneFolder(questionnaireId, mode.getFolder()));
        } catch (IOException e) {
            throw new GenesisException(500, "Error while moving file to done : %s".formatted(e.toString()));
        }
    }
}
