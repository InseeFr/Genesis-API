package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.editedexternal.EditedExternalResponseJsonService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.EditedExternalResponsePersistancePortStub;
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

class EditedExternalResponseControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-EDITED-EXTERNAL";
    private static final Path SOURCE_PATH =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN",Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID);

    private static final EditedExternalResponsePersistancePortStub stub =
            new EditedExternalResponsePersistancePortStub();
    private final EditedExternalResponseController editedExternalResponseController =
            new EditedExternalResponseController(
                    new EditedExternalResponseJsonService(stub),
                    new ConfigStub()
            );


    @BeforeEach
    void clean() throws IOException {
        FileSystemUtils.deleteRecursively(SOURCE_PATH);
        FileSystemUtils.deleteRecursively(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "DONE"));
    }

    @Test
    @SneakyThrows
    void readJson_test(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok.json"),
                SOURCE_PATH.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedExternalResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, "ok.json");

        //THEN
        Assertions.assertThat(stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)).hasSize(2);

        List<EditedExternalResponseDocument> filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO204")).toList();
        Assertions.assertThat(filter).hasSize(1);

        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(12);
        assertVariable(filter.getFirst(), "TVA", 302.34d);
        assertVariable(filter.getFirst(), "CA", 22.45d);
        assertVariable(filter.getFirst(), "COM_AUTRE", "blablablabla");
        assertVariable(filter.getFirst(), "INTERRO_N_1", true);
        assertVariable(filter.getFirst(), "INTERRO_N_2", false);
        assertVariable(filter.getFirst(), "NAF25", "9560Y");
        assertVariable(filter.getFirst(), "POIDS", 1.25);
        assertVariable(filter.getFirst(), "MILLESIME", "2024");
        assertVariable(filter.getFirst(), "NSUBST", true);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",0, 50);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",1, 23);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",2, 10);
        assertVariableNull(filter.getFirst(), "TAB_EXTNUM",3);
        assertVariable(filter.getFirst(), "TAB_EXTCAR",0, "A");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",1, "");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",2, "B");

        filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO208")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(11);
        assertVariable(filter.getFirst(), "TVA", "");
        assertVariable(filter.getFirst(), "COM_AUTRE", "");
        assertVariable(filter.getFirst(), "SECTEUR", "123456789");
        assertVariable(filter.getFirst(), "INTERRO_N_1", false);
        assertVariable(filter.getFirst(), "INTERRO_N_2", false);
        assertVariable(filter.getFirst(), "NAF25", "1014Z");
        assertVariable(filter.getFirst(), "POIDS", 12);
        assertVariable(filter.getFirst(), "MILLESIME", "2024");
        assertVariable(filter.getFirst(), "NSUBST", false);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",0, 10);
        assertVariable(filter.getFirst(), "TAB_EXTCAR",0, "C");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",1, "C");
    }

    @Test
    @SneakyThrows
    void readJson_override_interrogation_id(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok.json"),
                SOURCE_PATH.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        editedExternalResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, "ok.json");
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok2.json"),
                SOURCE_PATH.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedExternalResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, "ok2.json");

        //THEN
        Assertions.assertThat(stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)).hasSize(2);

        List<EditedExternalResponseDocument> filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO204")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(12);
        assertVariable(filter.getFirst(), "TVA", 302.34d);
        assertVariable(filter.getFirst(), "CA", 22.45d);
        assertVariable(filter.getFirst(), "COM_AUTRE", "blablablabla");
        assertVariable(filter.getFirst(), "INTERRO_N_1", true);
        assertVariable(filter.getFirst(), "INTERRO_N_2", false);
        assertVariable(filter.getFirst(), "NAF25", "9560Y");
        assertVariable(filter.getFirst(), "POIDS", 1.25);
        assertVariable(filter.getFirst(), "MILLESIME", "2024");
        assertVariable(filter.getFirst(), "NSUBST", true);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",0, 50);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",1, 23);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",2, 10);
        assertVariableNull(filter.getFirst(), "TAB_EXTNUM",3);
        assertVariable(filter.getFirst(), "TAB_EXTCAR",0, "A");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",1, "");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",2, "B");

        filter = stub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO200")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID);

        Assertions.assertThat(filter.getFirst().getVariables()).hasSize(11);
        assertVariable(filter.getFirst(), "TVA", "");
        assertVariable(filter.getFirst(), "COM_AUTRE", "");
        assertVariable(filter.getFirst(), "SECTEUR", "123456789");
        assertVariable(filter.getFirst(), "INTERRO_N_1", false);
        assertVariable(filter.getFirst(), "INTERRO_N_2", false);
        assertVariable(filter.getFirst(), "NAF25", "1014Z");
        assertVariable(filter.getFirst(), "POIDS", 12);
        assertVariable(filter.getFirst(), "MILLESIME", "2024");
        assertVariable(filter.getFirst(), "NSUBST", false);
        assertVariable(filter.getFirst(), "TAB_EXTNUM",0, 10);
        assertVariable(filter.getFirst(), "TAB_EXTCAR",0, "C");
        assertVariable(filter.getFirst(), "TAB_EXTCAR",1, "C");
    }

    @ParameterizedTest
    @ValueSource(strings = {"invalid_syntax.json",
            "not_a_json.xml",
            "no_interrogationId.json",
            "only_one_interrogationId.json",
            "double_interrogationId.json"}
    )
    @SneakyThrows
    void readJson_error_400(String fileName){
        //GIVEN
        Files.createDirectories(SOURCE_PATH);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve(fileName),
                SOURCE_PATH.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedExternalResponseController.readJson(QUESTIONNAIRE_ID, Mode.WEB, fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    //UTILS
    //THEN
    private static void assertVariable(EditedExternalResponseDocument document,
                                       String variableName,
                                       String expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedExternalResponseDocument document,
                                       String variableName,
                                       double expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Double.class).isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedExternalResponseDocument document,
                                       String variableName,
                                       boolean expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Boolean.class).isEqualTo(expectedValue);
    }

    private static void assertVariable(EditedExternalResponseDocument document,
                                       String variableName,
                                       int expectedValue
    ) {
        Assertions.assertThat(document.getVariables().get(variableName)).isNotNull().isInstanceOf(Integer.class).isEqualTo(expectedValue);
    }

    @SuppressWarnings("unchecked")
    private static void assertVariableNull(EditedExternalResponseDocument document,
                                           String arrayVariableName,
                                           int index
    ) {
        Assertions.assertThat((List<Object>)document.getVariables().get(arrayVariableName)).hasSizeGreaterThan(index);
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertThat(list).hasSizeGreaterThan(index);
        Assertions.assertThat(list.get(index)).isNull();
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(EditedExternalResponseDocument document,
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
    private static void assertVariable(EditedExternalResponseDocument document,
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