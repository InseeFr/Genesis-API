package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.service.editedprevious.EditedPreviousResponseJsonService;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.EditedPreviousResponsePersistancePortStub;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

class EditedPreviousResponseControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-EDITED-PREVIOUS";
    private static final Path SOURCE_PATH =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("IN").resolve("WEB").resolve(QUESTIONNAIRE_ID);

    private static final EditedPreviousResponsePersistancePortStub stub =
            new EditedPreviousResponsePersistancePortStub();
    private EditedPreviousResponseController editedPreviousResponseController =
            new EditedPreviousResponseController(
                    new EditedPreviousResponseJsonService(stub),
                    new ConfigStub()
            );


    @BeforeEach
    void clean() throws IOException {
        FileSystemUtils.deleteRecursively(SOURCE_PATH);
    }

    @Test
    void readJson() throws IOException {

        //TODO intégrer en base
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH
        );

        //WHEN


    }
    @Test
    void readJson_invalid_syntax(){
        //TODO Erreur 400
    }
    @Test
    void readJson_not_a_json(){
        //TODO Erreur 400
    }
    @Test
    void readJson_override_interrogation_id(){
        //TODO remplacer un existant
    }
    @Test
    void readJson_override_one_interrogation_id(){
        //TODO remplacer un existant parmi une liste de nouveaux
    }
    @Test
    void readJson_no_interrogation_id(){
        //TODO Erreur 400
    }
}