package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.editedresponse.EditedResponseModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.editedresponse.EditedResponseJsonService;
import fr.insee.genesis.domain.service.editedresponse.editedexternal.EditedExternalResponseJsonService;
import fr.insee.genesis.domain.service.editedresponse.editedprevious.EditedPreviousResponseJsonService;
import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.EditedExternalResponsePersistancePortStub;
import fr.insee.genesis.stubs.EditedPreviousResponsePersistancePortStub;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.ResponseEntity;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
class EditedResponseControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-EDITED";
    private static final String INTERROGATION_ID_1 = "TEST1";
    private static final String INTERROGATION_ID_2 = "TEST2";

    private static final String QUESTIONNAIRE_ID_PREVIOUS = "TEST-EDITED-PREVIOUS";
    private static final String QUESTIONNAIRE_ID_EXTERNAL = "TEST-EDITED-EXTERNAL";
    private static final Path SOURCE_PATH_PREVIOUS =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN", Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID_PREVIOUS);
    private static final Path SOURCE_PATH_EXTERNAL =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN", Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID_EXTERNAL);

    private static final EditedPreviousResponsePersistancePortStub previousStub =
            new EditedPreviousResponsePersistancePortStub();
    private static final EditedExternalResponsePersistancePortStub externalStub =
            new EditedExternalResponsePersistancePortStub();

    private final EditedResponseController editedResponseController = new EditedResponseController(
            new EditedPreviousResponseJsonService(previousStub),
            new EditedExternalResponseJsonService(externalStub),
            new EditedResponseJsonService(
                    previousStub,
                    externalStub
            ),
            new ConfigStub()
    );

    @BeforeEach
    void clean() throws IOException {
        FileSystemUtils.deleteRecursively(SOURCE_PATH_PREVIOUS);
        FileSystemUtils.deleteRecursively(SOURCE_PATH_EXTERNAL);
        FileSystemUtils.deleteRecursively(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "DONE"));

        previousStub.getMongoStub().clear();
        externalStub.getMongoStub().clear();

        previousStub.getMongoStub().put(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME, new ArrayList<>());
        externalStub.getMongoStub().put(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME, new ArrayList<>());
    }

    //OK CASES
    //GET
    @Test
    void getEditedResponses_test() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).addAll(
                getEditedPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).addAll(
                getEditedExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID, INTERROGATION_ID_1);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseModel.class);

        EditedPreviousResponseDocument editedPreviousResponseDocument = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).stream().filter(
                editedPreviousResponseDocument1 ->
                        editedPreviousResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && editedPreviousResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
        ).toList().getFirst();
        EditedExternalResponseDocument editedExternalResponseDocument =
                externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).stream().filter(
                        editedExternalResponseDocument1 ->
                                editedExternalResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                        && editedExternalResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
                ).toList().getFirst();
        EditedResponseModel editedResponseModel = (EditedResponseModel) response.getBody();
        Assertions.assertThat(editedResponseModel.interrogationId()).isEqualTo(INTERROGATION_ID_1);
        assertDocumentEqualToDto(editedPreviousResponseDocument, editedResponseModel);
        assertDocumentEqualToDto(editedExternalResponseDocument, editedResponseModel);
    }

    @Test
    void getEditedResponses_test_no_external() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).addAll(
                getEditedPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME).addAll(
                getEditedExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID, INTERROGATION_ID_2);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseModel.class);

        EditedPreviousResponseDocument editedPreviousResponseDocument = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME).stream().filter(
                editedPreviousResponseDocument1 ->
                        editedPreviousResponseDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && editedPreviousResponseDocument1.getInterrogationId().equals(INTERROGATION_ID_2)
        ).toList().getFirst();

        EditedResponseModel editedResponseModel = (EditedResponseModel) response.getBody();
        Assertions.assertThat(editedResponseModel.interrogationId()).isEqualTo(INTERROGATION_ID_2);
        assertDocumentEqualToDto(editedPreviousResponseDocument, editedResponseModel);
        Assertions.assertThat(editedResponseModel.editedExternal()).isNotNull().isEmpty();
    }

    @Test
    void getEditedResponses_test_not_found(){
        //GIVEN
        //Empty stubs from clean()

        //WHEN
        ResponseEntity<Object> response = editedResponseController.getEditedResponses(QUESTIONNAIRE_ID,
                INTERROGATION_ID_1);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isInstanceOf(EditedResponseModel.class);
        EditedResponseModel editedResponseModel = (EditedResponseModel) response.getBody();
        Assertions.assertThat(editedResponseModel.interrogationId()).isEqualTo(INTERROGATION_ID_1);
        Assertions.assertThat(editedResponseModel.editedPrevious()).isNotNull().isEmpty();
        Assertions.assertThat(editedResponseModel.editedExternal()).isNotNull().isEmpty();
    }

    //POST
    @Test
    @SneakyThrows
    void saveEditedResponses_previous_test() {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.saveEditedResponses(QUESTIONNAIRE_ID_PREVIOUS);

        //THEN
        Assertions.assertThat(previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)).hasSize(2);

        List<EditedPreviousResponseDocument> filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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

        filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertThat(filter).hasSize(1);
            Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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
    void saveEditedResponses_external_test() {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.saveEditedResponses(QUESTIONNAIRE_ID_EXTERNAL);

        //THEN
        Assertions.assertThat(externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)).hasSize(2);

        List<EditedExternalResponseDocument> filter = externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO204")).toList();
        Assertions.assertThat(filter).hasSize(1);

        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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

        filter = externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO208")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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

    //PREVIOUS
    @Test
    @SneakyThrows
    void readPreviousJson_no_source(){
        testOKCase(null);
    }

    @ParameterizedTest
    @ValueSource(strings = {"collecté", "edité"})
    @SneakyThrows
    void readPreviousJson_sourceState(String sourceState){
        testOKCase(sourceState);
    }

    private void testOKCase(String sourceState) throws IOException {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, sourceState, "ok.json");

        //THEN
        Assertions.assertThat(previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)).hasSize(2);

        List<EditedPreviousResponseDocument> filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertThat(filter).hasSize(1);
        if(sourceState == null){
            Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        }else{
            Assertions.assertThat(filter.getFirst().getSourceState()).isNotNull().isEqualTo(sourceState);
        }
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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

        filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertThat(filter).hasSize(1);
        if(sourceState == null){
            Assertions.assertThat(filter.getFirst().getSourceState()).isNull();
        }else{
            Assertions.assertThat(filter.getFirst().getSourceState()).isNotNull().isEqualTo(sourceState);
        }
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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
    void readPreviousJson_override_interrogation_id(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, "ok.json");
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve("ok2.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, "ok2.json");

        //THEN
        Assertions.assertThat(previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)).hasSize(2);

        List<EditedPreviousResponseDocument> filter =
                previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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

        filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO200")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_PREVIOUS);

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

        filter = previousStub.getMongoStub().get(Constants.MONGODB_EDITED_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertThat(filter).isEmpty();
    }

    @ParameterizedTest
    @ValueSource(strings = {"ceci est une origine beaucoup trop longue"})
    @SneakyThrows
    void readPreviousJson_sourceState_too_long(String sourceState){
        //GIVEN
        String fileName = "ok.json";
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH_PREVIOUS.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, sourceState,
                fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SneakyThrows
    void readPreviousJson_invalid_syntax(){
        String syntaxErrorFileName = "invalid_syntax.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH_PREVIOUS.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null,
                syntaxErrorFileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
    @Test
    @SneakyThrows
    void readPreviousJson_not_a_json(){
        String syntaxErrorFileName = "not_a_json.xml";
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH_PREVIOUS.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null,
                syntaxErrorFileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }


    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"no_interrogationId.json", "only_one_interrogationId.json", "double_interrogationId.json"})
    @DisplayName("Previous json return 400 if no interrogationId, only one interrogationId, or double interrogationId")
    void readPreviousJson_no_interrogation_id(String fileName){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_previous").resolve(fileName),
                SOURCE_PATH_PREVIOUS.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedResponseController.readEditedPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, fileName);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }


    //EXTERNAL
    @Test
    @SneakyThrows
    void readExternalJson_test(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.readEditedExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok.json");

        //THEN
        Assertions.assertThat(externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)).hasSize(2);

        List<EditedExternalResponseDocument> filter = externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO204")).toList();
        Assertions.assertThat(filter).hasSize(1);

        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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

        filter = externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO208")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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
    void readExternalJson_override_interrogation_id(){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        editedResponseController.readEditedExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok.json");
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve("ok2.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        editedResponseController.readEditedExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok2.json");

        //THEN
        Assertions.assertThat(externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)).hasSize(2);

        List<EditedExternalResponseDocument> filter =
                externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO204")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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

        filter = externalStub.getMongoStub().get(Constants.MONGODB_EDITED_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO200")).toList();
        Assertions.assertThat(filter).hasSize(1);
        Assertions.assertThat(filter.getFirst().getQuestionnaireId()).isEqualTo(QUESTIONNAIRE_ID_EXTERNAL);

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
    void readExternalJson_error_400(String fileName){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("edited_external").resolve(fileName),
                SOURCE_PATH_EXTERNAL.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = editedResponseController.readEditedExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, fileName);
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

    private List<EditedPreviousResponseDocument> getEditedPreviousTestDocuments() {
        List<EditedPreviousResponseDocument> editedPreviousResponseDocumentList = new ArrayList<>();

        EditedPreviousResponseDocument editedPreviousResponseDocument = new EditedPreviousResponseDocument();
        editedPreviousResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedPreviousResponseDocument.setInterrogationId(INTERROGATION_ID_1);
        editedPreviousResponseDocument.setVariables(new HashMap<>());
        editedPreviousResponseDocument.getVariables().put("TEXTECOURT", "");
        editedPreviousResponseDocument.getVariables().put("TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
        editedPreviousResponseDocument.getVariables().put("FLOAT", 50.25d);
        editedPreviousResponseDocument.getVariables().put("INTEGER", null);
        editedPreviousResponseDocument.getVariables().put("BOOLEEN", true);
        editedPreviousResponseDocument.getVariables().put("DROPDOWN", "03");
        editedPreviousResponseDocument.getVariables().put("QCM_B1", true);
        editedPreviousResponseDocument.getVariables().put("QCM_B2", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B4", true);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A11", 200);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A12", 150);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A23", 1000);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A24", null);
        editedPreviousResponseDocument.getVariables().put("TABOFATS1", List.of("AA","","BB","CC"));
        editedPreviousResponseDocument.getVariables().put("TABOFATS3", Arrays.asList(5,null,3));
        editedPreviousResponseDocumentList.add(editedPreviousResponseDocument);

        editedPreviousResponseDocument = new EditedPreviousResponseDocument();
        editedPreviousResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedPreviousResponseDocument.setInterrogationId(INTERROGATION_ID_2);
        editedPreviousResponseDocument.setVariables(new HashMap<>());
        editedPreviousResponseDocument.getVariables().put("TEXTECOURT", "test previous");
        editedPreviousResponseDocument.getVariables().put("TEXTELONG", "");
        editedPreviousResponseDocument.getVariables().put("FLOAT", 12.2d);
        editedPreviousResponseDocument.getVariables().put("BOOLEEN", false);
        editedPreviousResponseDocument.getVariables().put("DROPDOWN", "");
        editedPreviousResponseDocument.getVariables().put("QCM_B1", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B2", false);
        editedPreviousResponseDocument.getVariables().put("QCM_B5", true);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A11", 1);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A12", 2);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A23", 3);
        editedPreviousResponseDocument.getVariables().put("TABLEAU2A24", 4);
        editedPreviousResponseDocument.getVariables().put("TABOFATS1", List.of("BB","BB"));
        editedPreviousResponseDocument.getVariables().put("TABOFATS3", List.of(10,4,0));
        editedPreviousResponseDocumentList.add(editedPreviousResponseDocument);

        return editedPreviousResponseDocumentList;
    }

    private List<EditedExternalResponseDocument> getEditedExternalTestDocuments() {
        List<EditedExternalResponseDocument> editedExternalResponseDocumentList = new ArrayList<>();

        EditedExternalResponseDocument editedExternalResponseDocument = new EditedExternalResponseDocument();
        editedExternalResponseDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        editedExternalResponseDocument.setInterrogationId(INTERROGATION_ID_1);
        editedExternalResponseDocument.setVariables(new HashMap<>());
        editedExternalResponseDocument.getVariables().put("TVA", 302.34d);
        editedExternalResponseDocument.getVariables().put("CA", 22.45d);
        editedExternalResponseDocument.getVariables().put("COM_AUTRE", "blablablabla");
        editedExternalResponseDocument.getVariables().put("SECTEUR", "110110110");
        editedExternalResponseDocument.getVariables().put("CATEGORIE", "");
        editedExternalResponseDocument.getVariables().put("INTERRO_N_1", true);
        editedExternalResponseDocument.getVariables().put("INTERRO_N_2", false);
        editedExternalResponseDocument.getVariables().put("NAF25", "9560Y");
        editedExternalResponseDocument.getVariables().put("POIDS", null);
        editedExternalResponseDocument.getVariables().put("MILLESIME", "2024");
        editedExternalResponseDocument.getVariables().put("NSUBST", true);
        editedExternalResponseDocument.getVariables().put("TAB_EXTNUM", Arrays.asList(50,23,10,null));
        editedExternalResponseDocument.getVariables().put("TAB_EXTCAR", Arrays.asList("A", "", "B"));
        editedExternalResponseDocumentList.add(editedExternalResponseDocument);
        return editedExternalResponseDocumentList;
    }


    private void assertDocumentEqualToDto(EditedPreviousResponseDocument editedPreviousResponseDocument,
                                          EditedResponseModel editedResponseModel) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : editedPreviousResponseDocument.getVariables().entrySet()) {
            //Get edited previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    editedResponseModel.editedPrevious().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    private void assertDocumentEqualToDto(EditedExternalResponseDocument editedExternalResponseDocument,
                                          EditedResponseModel editedResponseModel) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : editedExternalResponseDocument.getVariables().entrySet()) {
            //Get edited previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    editedResponseModel.editedExternal().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    @SuppressWarnings("unchecked")
    private void assertEntryEqualToDto(Map.Entry<String, Object> documentVariable,
                                       List<VariableQualityToolDto> variableQualityToolDtosOfEntry) {
        Assertions.assertThat(variableQualityToolDtosOfEntry).isNotEmpty();

        //If that variable is not a list
        if (!(documentVariable.getValue() instanceof List<?>)) {
            Assertions.assertThat(variableQualityToolDtosOfEntry).hasSize(1);
            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfEntry.getFirst().getVariableStateDtoList();

            Assertions.assertThat(variableStateDtos).hasSize(1); // Only 1 state
            Assertions.assertThat(variableStateDtos.getFirst().getState()).isEqualTo(DataState.COLLECTED);
            Assertions.assertThat(variableStateDtos.getFirst().getValue()).isEqualTo(documentVariable.getValue());
            return;
        }
        int i = 1;
        for (Object documentVariableElement : (List<Object>) documentVariable.getValue()) {
            int finalI = i;
            List<VariableQualityToolDto> variableQualityToolDtosOfIteration =
                    variableQualityToolDtosOfEntry.stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getIteration().equals(finalI)
                    ).toList();
            Assertions.assertThat(variableQualityToolDtosOfIteration).hasSize(1);

            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfIteration.getFirst().getVariableStateDtoList();
            Assertions.assertThat(variableStateDtos).hasSize(1);
            Assertions.assertThat(variableStateDtos.getFirst().getState()).isEqualTo(DataState.COLLECTED);
            Assertions.assertThat(variableStateDtos.getFirst().getValue()).isEqualTo(documentVariableElement);
            i++;
        }
    }

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
