package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.editedresponse.editedprevious.EditedPreviousResponseJsonService;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.EditedPreviousResponsePersistancePortStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

class EditedPreviousResponseControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-EDITED-PREVIOUS";
    private static final Path SOURCE_PATH =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN",Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID);

    private static final EditedPreviousResponsePersistancePortStub stub =
            new EditedPreviousResponsePersistancePortStub();
    private final EditedPreviousResponseController editedPreviousResponseController =
            new EditedPreviousResponseController(
                    new EditedPreviousResponseJsonService(stub),
                    new ConfigStub()
            );


    @BeforeEach
    void clean() throws IOException {
        FileSystemUtils.deleteRecursively(SOURCE_PATH);
        FileSystemUtils.deleteRecursively(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "DONE"));
    }

    @Test
    @SneakyThrows
    void readJson_no_source(){
        testOKCase(null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"collecté", "edité"})
    @SneakyThrows
    void readJson_sourceState(String sourceState){
        testOKCase(sourceState);
    }

    private void testOKCase(String sourceState) throws IOException {
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, sourceState, "ok.json");

        //THEN
        Assertions.assertThat(stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)).hasSize(2);

        List<EditedPreviousResponseDocument> filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertThat(filter).hasSize(1);
        if(sourceState == null){
            Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        }else{
            Assertions.assertThat(filter.getFirst().getSourceState()).isNotNull().isEqualTo(sourceState);
        }
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(15);
        assertVariable(filter.getFirst(), "TEXTECOURT", "");
        assertVariable(filter.getFirst(), "TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
        assertVariable(filter.getFirst(), "FLOAT", 50.25d);
        assertVariableNull(filter.getFirst(), "INTEGER");
        assertVariable(filter.getFirst(), "BOOLEEN", true);
        assertVariable(filter.getFirst(), "DROPDOWN", "03");
        assertVariable(filter.getFirst(), "QCM_B1", true);
        assertVariable(filter.getFirst(), "QCM_B2", false);
        assertVariable(filter.getFirst(), "QCM_B4", true);
        assertVariable(filter.getFirst(), "TABLEAU2A11", 200);
        assertVariable(filter.getFirst(), "TABLEAU2A12", 150);
        assertVariable(filter.getFirst(), "TABLEAU2A23", 1000);
        assertVariableNull(filter.getFirst(), "TABLEAU2A24");
        assertVariable(filter.getFirst(), "TABOFATS1",0, "AA");
        assertVariable(filter.getFirst(), "TABOFATS1",1, "");
        assertVariable(filter.getFirst(), "TABOFATS1",2, "BB");
        assertVariable(filter.getFirst(), "TABOFATS1",3, "CC");
        assertVariable(filter.getFirst(), "TABOFATS3",0, 5);
        assertVariableNull(filter.getFirst(), "TABOFATS3",1);
        assertVariable(filter.getFirst(), "TABOFATS3",2, 3);

        filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertThat(filter).hasSize(1);
        if(sourceState == null){
            Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        }else{
            Assertions.assertThat(filter.getFirst().getSourceState()).isNotNull().isEqualTo(sourceState);
        }
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(14);
        assertVariable(filter.getFirst(), "TEXTECOURT", "test previous");
        assertVariable(filter.getFirst(), "TEXTELONG", "");
        assertVariable(filter.getFirst(), "FLOAT", 12.2d);
        assertVariable(filter.getFirst(), "BOOLEEN", false);
        assertVariable(filter.getFirst(), "DROPDOWN", "");
        assertVariable(filter.getFirst(), "QCM_B1", false);
        assertVariable(filter.getFirst(), "QCM_B2", false);
        assertVariable(filter.getFirst(), "QCM_B5", true);
        assertVariable(filter.getFirst(), "TABLEAU2A11", 1);
        assertVariable(filter.getFirst(), "TABLEAU2A12", 2);
        assertVariable(filter.getFirst(), "TABLEAU2A23", 3);
        assertVariable(filter.getFirst(), "TABLEAU2A24",4);
        assertVariable(filter.getFirst(), "TABOFATS1",0, "BB");
        assertVariable(filter.getFirst(), "TABOFATS1",1, "BB");
        assertVariable(filter.getFirst(), "TABOFATS3",0, 10);
        assertVariable(filter.getFirst(), "TABOFATS3",1, 4);
        assertVariable(filter.getFirst(), "TABOFATS3",2, 0);
    }

    @Test
    @SneakyThrows
    void readJson_override_interrogation_id(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, "ok.json");
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok2.json"),
                SOURCE_PATH.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, "ok2.json");

        //THEN
        Assertions.assertThat(stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)).hasSize(2);

        List<EditedPreviousResponseDocument> filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(15);
        assertVariable(filter.getFirst(), "TEXTECOURT", "");
        assertVariable(filter.getFirst(), "TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
        assertVariable(filter.getFirst(), "FLOAT", 50.25d);
        assertVariableNull(filter.getFirst(), "INTEGER");
        assertVariable(filter.getFirst(), "BOOLEEN", true);
        assertVariable(filter.getFirst(), "DROPDOWN", "03");
        assertVariable(filter.getFirst(), "QCM_B1", true);
        assertVariable(filter.getFirst(), "QCM_B2", false);
        assertVariable(filter.getFirst(), "QCM_B4", true);
        assertVariable(filter.getFirst(), "TABLEAU2A11", 200);
        assertVariable(filter.getFirst(), "TABLEAU2A12", 150);
        assertVariable(filter.getFirst(), "TABLEAU2A23", 1000);
        assertVariableNull(filter.getFirst(), "TABLEAU2A24");
        assertVariable(filter.getFirst(), "TABOFATS1",0, "AA");
        assertVariable(filter.getFirst(), "TABOFATS1",1, "");
        assertVariable(filter.getFirst(), "TABOFATS1",2, "BB");
        assertVariable(filter.getFirst(), "TABOFATS1",3, "CC");
        assertVariable(filter.getFirst(), "TABOFATS3",0, 5);
        assertVariableNull(filter.getFirst(), "TABOFATS3",1);
        assertVariable(filter.getFirst(), "TABOFATS3",2, 3);

        filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO200")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(14);
        assertVariable(filter.getFirst(), "TEXTECOURT", "test previous");
        assertVariable(filter.getFirst(), "TEXTELONG", "");
        assertVariable(filter.getFirst(), "FLOAT", 12.2d);
        assertVariable(filter.getFirst(), "BOOLEEN", false);
        assertVariable(filter.getFirst(), "DROPDOWN", "");
        assertVariable(filter.getFirst(), "QCM_B1", false);
        assertVariable(filter.getFirst(), "QCM_B2", false);
        assertVariable(filter.getFirst(), "QCM_B5", true);
        assertVariable(filter.getFirst(), "TABLEAU2A11", 1);
        assertVariable(filter.getFirst(), "TABLEAU2A12", 2);
        assertVariable(filter.getFirst(), "TABLEAU2A23", 3);
        assertVariable(filter.getFirst(), "TABLEAU2A24",4);
        assertVariable(filter.getFirst(), "TABOFATS1",0, "BB");
        assertVariable(filter.getFirst(), "TABOFATS1",1, "BB");
        assertVariable(filter.getFirst(), "TABOFATS3",0, 10);
        assertVariable(filter.getFirst(), "TABOFATS3",1, 4);
        assertVariable(filter.getFirst(), "TABOFATS3",2, 0);

        filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertThat(filter).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ceci est une origine beaucoup trop longue"})
    @SneakyThrows
    void readJson_sourceState_too_long(String sourceState){
        //GIVEN
        String fileName = "ok.json";
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, sourceState,
                fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SneakyThrows
    void readJson_invalid_syntax(){
        String syntaxErrorFileName = "invalid_syntax.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, syntaxErrorFileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
    @Test
    @SneakyThrows
    void readJson_not_a_json(){
        String syntaxErrorFileName = "not_a_json.xml";
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, syntaxErrorFileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SneakyThrows
    void readJson_no_interrogation_id(){
        String fileName = "no_interrogationId.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
    @Test
    @SneakyThrows
    void readJson_only_one_interrogation_id(){
        String fileName = "only_one_interrogationId.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SneakyThrows
    void readJson_double_interrogation_id(){
        String fileName = "double_interrogationId.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedPreviousResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, null, fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    //UTILS
    //THEN
    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String variableName,
                                       String expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String variableName,
                                       double expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Double.class).isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String variableName,
                                       boolean expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Boolean.class).isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String variableName,
                                       int expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Integer.class).isEqualTo(expectedValue);
    }

    private static void assertVariableNull(EditedPreviousResponseDocument document,
                                           String variableName
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNull();
    }

    @SuppressWarnings("unchecked")
    private static void assertVariableNull(EditedPreviousResponseDocument document,
                                           String arrayVariableName,
                                           int index
    ) {
        Assertions.assertThat((List<Object>)document.getVariables().get(arrayVariableName)).hasSizeGreaterThan(index);
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertThat(list).hasSizeGreaterThan(index);
        Assertions.assertThat(list.get(index)).isNull();
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String arrayVariableName,
                                       int index,
                                       String expectedValue
    ) throws ClassCastException{
        Assertions.assertThat((List<Object>)document.getVariables().get(arrayVariableName)).hasSizeGreaterThan(index);

        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertThat(list).hasSizeGreaterThan(index);
        Assertions.assertThat(list.get(index)).isInstanceOf(String.class);
        Assertions.assertThat((String)list.get(index)).isEqualTo(expectedValue);
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(EditedPreviousResponseDocument document,
                                       String arrayVariableName,
                                       int index,
                                       int expectedValue
    ) throws ClassCastException{
        Assertions.assertThat((List<Object>)document.getVariables().get(arrayVariableName)).hasSizeGreaterThan(index);

        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertThat(list).hasSizeGreaterThan(index);
        Assertions.assertThat(list.get(index)).isInstanceOf(Integer.class);
        Assertions.assertThat((Integer)list.get(index)).isInstanceOf(Integer.class).isEqualTo(expectedValue);
    }
}