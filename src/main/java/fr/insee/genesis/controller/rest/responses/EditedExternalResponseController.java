package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.EditedExternalResponseApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import io.swagger.v3.oas.annotations.Operation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;

@RequestMapping(path = "/edited-external" )
@Controller
@Slf4j
public class EditedExternalResponseController {
    private final EditedExternalResponseApiPort editedExternalResponseApiPort;

    private final Config config;

    @Autowired
    public EditedExternalResponseController(EditedExternalResponseApiPort editedExternalResponseApiPort, Config config) {
        this.editedExternalResponseApiPort = editedExternalResponseApiPort;
        this.config = config;
    }

    @Operation(summary = "Add edited external json file")
    @PostMapping(path = "/json")
    @PreAuthorize("hasRole('USER_KRAFTWERK')")
    public ResponseEntity<Object> readJson(
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
            try (InputStream inputStream = new FileInputStream(filePath)) {
                editedExternalResponseApiPort.readEditedExternalFile(inputStream, questionnaireId);
            } catch (FileNotFoundException e) {
                throw new GenesisException(404, "File %s not found".formatted(filePath));
            } catch (IOException e) {
                throw new GenesisException(500, e.toString());
            }
            try {
                fileUtils.moveFiles(Path.of(filePath), fileUtils.getDoneFolder(questionnaireId, mode.getFolder()));
            } catch (IOException e) {
                throw new GenesisException(500, "Error while moving file to done : %s".formatted(e.toString()));
            }
            return ResponseEntity.ok("Edited external variable file %s saved !".formatted(filePath));
        }catch (GenesisException ge){
            return ResponseEntity.status(HttpStatusCode.valueOf(ge.getStatus())).body(ge.getMessage());
        }
    }
}
