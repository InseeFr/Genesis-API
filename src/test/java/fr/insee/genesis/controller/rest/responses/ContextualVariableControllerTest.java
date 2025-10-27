package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.contextualvariable.ContextualVariableJsonService;
import fr.insee.genesis.domain.service.contextualvariable.external.ContextualExternalVariableJsonService;
import fr.insee.genesis.domain.service.contextualvariable.previous.ContextualPreviousVariableJsonService;
import fr.insee.genesis.infrastructure.document.contextualexternal.ContextualExternalVariableDocument;
import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.ContextualExternalVariablePersistancePortStub;
import fr.insee.genesis.stubs.ContextualPreviousVariablePersistancePortStub;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
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
import java.util.function.Consumer;
import java.util.stream.Stream;

@Slf4j
class ContextualVariableControllerTest {

    private static final String QUESTIONNAIRE_ID = "TEST-CONTEXTUAL";
    private static final String INTERROGATION_ID_1 = "TEST1";
    private static final String INTERROGATION_ID_2 = "TEST2";

    private static final String QUESTIONNAIRE_ID_PREVIOUS = "TEST-CONTEXTUAL-PREVIOUS";
    private static final String QUESTIONNAIRE_ID_EXTERNAL = "TEST-CONTEXTUAL-EXTERNAL";
    private static final Path SOURCE_PATH_PREVIOUS =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN", Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID_PREVIOUS);
    private static final Path SOURCE_PATH_EXTERNAL =
            Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,"IN", Mode.WEB.getFolder()).resolve(QUESTIONNAIRE_ID_EXTERNAL);

    private static final ContextualPreviousVariablePersistancePortStub previousStub =
            new ContextualPreviousVariablePersistancePortStub();
    private static final ContextualExternalVariablePersistancePortStub externalStub =
            new ContextualExternalVariablePersistancePortStub();

    private final ContextualVariableController contextualVariableController = new ContextualVariableController(
            new ContextualPreviousVariableJsonService(previousStub),
            new ContextualExternalVariableJsonService(externalStub),
            new ContextualVariableJsonService(
                    new ContextualPreviousVariableJsonService(previousStub),
                    new ContextualExternalVariableJsonService(externalStub)
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

        previousStub.getMongoStub().put(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME, new ArrayList<>());
        externalStub.getMongoStub().put(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME, new ArrayList<>());
    }

    //OK CASES
    //GET
    @Test
    void getContextualVariables_test() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).addAll(
                getContextualPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).addAll(
                getContextualExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.getContextualVariables(QUESTIONNAIRE_ID, INTERROGATION_ID_1);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertInstanceOf(ContextualVariableModel.class,response.getBody());

        ContextualPreviousVariableDocument contextualPreviousVariableDocument = previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).stream().filter(
                contextualPreviousVariableDocument1 ->
                        contextualPreviousVariableDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && contextualPreviousVariableDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
        ).toList().getFirst();
        ContextualExternalVariableDocument contextualExternalVariableDocument =
                externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).stream().filter(
                        contextualExternalVariableDocument1 ->
                                contextualExternalVariableDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                        && contextualExternalVariableDocument1.getInterrogationId().equals(INTERROGATION_ID_1)
                ).toList().getFirst();
        ContextualVariableModel contextualVariableModel = (ContextualVariableModel) response.getBody();
        Assertions.assertNotNull(contextualVariableModel);
        Assertions.assertEquals(INTERROGATION_ID_1,contextualVariableModel.interrogationId());
        assertDocumentEqualToDto(contextualPreviousVariableDocument, contextualVariableModel);
        assertDocumentEqualToDto(contextualExternalVariableDocument, contextualVariableModel);
    }

    @Test
    void getContextualVariables_test_no_external() {
        //GIVEN
        previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).addAll(
                getContextualPreviousTestDocuments()
        );
        externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).addAll(
                getContextualExternalTestDocuments()
        );

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.getContextualVariables(QUESTIONNAIRE_ID, INTERROGATION_ID_2);

        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error((String)response.getBody());
        }
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertInstanceOf(ContextualVariableModel.class,response.getBody());

        ContextualPreviousVariableDocument contextualPreviousVariableDocument = previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).stream().filter(
                contextualPreviousVariableDocument1 ->
                        contextualPreviousVariableDocument1.getQuestionnaireId().equals(QUESTIONNAIRE_ID)
                                && contextualPreviousVariableDocument1.getInterrogationId().equals(INTERROGATION_ID_2)
        ).toList().getFirst();

        ContextualVariableModel contextualVariableModel = (ContextualVariableModel) response.getBody();
        Assertions.assertEquals(INTERROGATION_ID_2,contextualVariableModel.interrogationId());
        assertDocumentEqualToDto(contextualPreviousVariableDocument, contextualVariableModel);
        Assertions.assertNotNull(contextualVariableModel.contextualExternal());
        Assertions.assertTrue(contextualVariableModel.contextualExternal().isEmpty());
    }

    @Test
    void getContextualVariables_test_not_found(){
        //GIVEN
        //Empty stubs from clean()

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.getContextualVariables(QUESTIONNAIRE_ID,
                INTERROGATION_ID_1);

        //THEN
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertInstanceOf(ContextualVariableModel.class,response.getBody());
        ContextualVariableModel contextualVariableModel = (ContextualVariableModel) response.getBody();
        Assertions.assertEquals(INTERROGATION_ID_1,contextualVariableModel.interrogationId());
        Assertions.assertNotNull(contextualVariableModel.contextualPrevious());
        Assertions.assertTrue(contextualVariableModel.contextualPrevious().isEmpty());
        Assertions.assertNotNull(contextualVariableModel.contextualExternal());
        Assertions.assertTrue(contextualVariableModel.contextualExternal().isEmpty());
    }

    //POST ALL FILES OF QUESTIONNAIRE
    @SneakyThrows
    @ParameterizedTest(name = "[{index}] saveContextualVariables - previous {0}")
    @MethodSource("casesPrevious")
    void saveContextualVariables_previous_test(
            String interrogationId,
            int expectedCollectionSize,
            int expectedVarCount,
            Consumer<ContextualPreviousVariableDocument> assertionsForDoc
    ) {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                        .resolve("contextual_previous")
                        .resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.saveContextualVariables(QUESTIONNAIRE_ID_PREVIOUS);

        //THEN
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        List<ContextualPreviousVariableDocument> previousVarDocumentList = previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME);
        Assertions.assertEquals(expectedCollectionSize,previousVarDocumentList.size());

        List<ContextualPreviousVariableDocument> filter =
                previousVarDocumentList
                        .stream()
                        .filter(doc -> doc.getInterrogationId().equals(interrogationId)).toList();
        Assertions.assertEquals(1,filter.size());

        ContextualPreviousVariableDocument firstDocument = filter.getFirst();
        Assertions.assertNull(firstDocument.getSourceState());
        Assertions.assertEquals(QUESTIONNAIRE_ID_PREVIOUS,firstDocument.getQuestionnaireId());

        Assertions.assertEquals(expectedVarCount,firstDocument.getVariables().size());

        // Assertions spécifiques au cas
        assertionsForDoc.accept(firstDocument);
    }
    static Stream<Arguments> casesPrevious() {
        return Stream.of(
                Arguments.of(
                        "AUTO104",
                        /* expectedCollectionSize */ 2,
                        /* expectedVarCount */ 15,
                        (Consumer<ContextualPreviousVariableDocument>) doc -> {
                            assertVariable(doc, "TEXTECOURT", "");
                            assertVariable(doc, "TEXTELONG",
                                    "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
                            assertVariable(doc, "FLOAT", 50.25d);
                            assertVariableNull(doc, "INTEGER");
                            assertVariable(doc, "BOOLEEN", true);
                            assertVariable(doc, "DROPDOWN", "03");
                            assertVariable(doc, "QCM_B1", true);
                            assertVariable(doc, "QCM_B2", false);
                            assertVariable(doc, "QCM_B4", true);
                            assertVariable(doc, "TABLEAU2A11", 200);
                            assertVariable(doc, "TABLEAU2A12", 150);
                            assertVariable(doc, "TABLEAU2A23", 1000);
                            assertVariableNull(doc, "TABLEAU2A24");
                            assertVariable(doc, "TABOFATS1", 0, "AA");
                            assertVariable(doc, "TABOFATS1", 1, "");
                            assertVariable(doc, "TABOFATS1", 2, "BB");
                            assertVariable(doc, "TABOFATS1", 3, "CC");
                            assertVariable(doc, "TABOFATS3", 0, 5);
                            assertVariableNull(doc, "TABOFATS3", 1);
                            assertVariable(doc, "TABOFATS3", 2, 3);
                        }
                ),
                Arguments.of(
                        "AUTO108",
                        /* expectedCollectionSize */ 2,
                        /* expectedVarCount */ 14,
                        (Consumer<ContextualPreviousVariableDocument>) doc -> {
                            assertVariable(doc, "TEXTECOURT", "test previous");
                            assertVariable(doc, "TEXTELONG", "");
                            assertVariable(doc, "FLOAT", 12.2d);
                            assertVariable(doc, "BOOLEEN", false);
                            assertVariable(doc, "DROPDOWN", "");
                            assertVariable(doc, "QCM_B1", false);
                            assertVariable(doc, "QCM_B2", false);
                            assertVariable(doc, "QCM_B5", true);
                            assertVariable(doc, "TABLEAU2A11", 1);
                            assertVariable(doc, "TABLEAU2A12", 2);
                            assertVariable(doc, "TABLEAU2A23", 3);
                            assertVariable(doc, "TABLEAU2A24", 4);
                            assertVariable(doc, "TABOFATS1", 0, "BB");
                            assertVariable(doc, "TABOFATS1", 1, "BB");
                            assertVariable(doc, "TABOFATS3", 0, 10);
                            assertVariable(doc, "TABOFATS3", 1, 4);
                            assertVariable(doc, "TABOFATS3", 2, 0);
                        }
                )
        );
    }

    @SneakyThrows
    @ParameterizedTest(name = "[{index}] saveContextualVariables_external - cas {0}")
    @MethodSource("casesExternal")
    void saveContextualVariables_external_test( String interrogationId,
                                                int expectedCollectionSize,
                                                int expectedVarCount,
                                                Consumer<ContextualExternalVariableDocument> assertionsForDoc) {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY)
                        .resolve("contextual_external")
                        .resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.saveContextualVariables(QUESTIONNAIRE_ID_EXTERNAL);

        //THEN
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertEquals(expectedCollectionSize,externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).size());

        List<ContextualExternalVariableDocument> filter = externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals(interrogationId)).toList();
        Assertions.assertEquals(1,filter.size());

        ContextualExternalVariableDocument firstDoc = filter.getFirst();
        Assertions.assertEquals(QUESTIONNAIRE_ID_EXTERNAL, firstDoc.getQuestionnaireId());
        Assertions.assertEquals(expectedVarCount, firstDoc.getVariables().size());
        assertionsForDoc.accept(firstDoc);

    }

    static Stream<Arguments> casesExternal() {
        return Stream.of(
                Arguments.of(
                        "AUTO204",
                        /* expectedCollectionSize */ 2,
                        /* expectedVarCount */ 12,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", 302.34d);
                            assertVariable(doc, "CA", 22.45d);
                            assertVariable(doc, "COM_AUTRE", "blablablabla");
                            assertVariable(doc, "INTERRO_N_1", true);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "9560Y");
                            assertVariable(doc, "POIDS", 1.25);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", true);

                            assertVariable(doc, "TAB_EXTNUM", 0, 50);
                            assertVariable(doc, "TAB_EXTNUM", 1, 23);
                            assertVariable(doc, "TAB_EXTNUM", 2, 10);
                            assertVariableNull(doc, "TAB_EXTNUM", 3);

                            assertVariable(doc, "TAB_EXTCAR", 0, "A");
                            assertVariable(doc, "TAB_EXTCAR", 1, "");
                            assertVariable(doc, "TAB_EXTCAR", 2, "B");
                        }
                ),
                Arguments.of(
                        "AUTO208",
                        /* expectedCollectionSize */ 2,
                        /* expectedVarCount */ 11,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", "");
                            assertVariable(doc, "COM_AUTRE", "");
                            assertVariable(doc, "SECTEUR", "123456789");
                            assertVariable(doc, "INTERRO_N_1", false);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "1014Z");
                            assertVariable(doc, "POIDS", 12);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", false);

                            assertVariable(doc, "TAB_EXTNUM", 0, 10);
                            assertVariable(doc, "TAB_EXTCAR", 0, "C");
                            assertVariable(doc, "TAB_EXTCAR", 1, "C");
                        }
                )
        );
    }


    @Test
    @SneakyThrows
    void saveContextualVariables_random_json_test() {
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_external").resolve("random_json.json"),
                SOURCE_PATH_EXTERNAL.resolve("random_json.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        ResponseEntity<Object> response = contextualVariableController.saveContextualVariables(QUESTIONNAIRE_ID_EXTERNAL);

        //THEN
        Assertions.assertTrue(response.getStatusCode().is2xxSuccessful());
        Assertions.assertTrue(
                externalStub.getMongoStub()
                        .get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME)
                        .isEmpty(),
                "MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME should be empty"
        );    }

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
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_previous").resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, sourceState, "ok.json");

        //THEN
        Assertions.assertEquals(2,previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).size());

        List<ContextualPreviousVariableDocument> filter = previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO104")).toList();
        Assertions.assertEquals(1,filter.size());
        if(sourceState == null){
            Assertions.assertNull(filter.getFirst().getSourceState());
        }else{
            Assertions.assertNotNull(filter.getFirst().getSourceState());
            Assertions.assertEquals(sourceState, filter.getFirst().getSourceState());
        }
        Assertions.assertEquals(QUESTIONNAIRE_ID_PREVIOUS,filter.getFirst().getQuestionnaireId());

        Assertions.assertEquals(15,filter.getFirst().getVariables().size());
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

        filter = previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals("AUTO108")).toList();
        Assertions.assertEquals(1,filter.size());
        if(sourceState == null){
            Assertions.assertNull(filter.getFirst().getSourceState());
        }else{
            Assertions.assertNotNull(filter.getFirst().getSourceState());
            Assertions.assertEquals(sourceState, filter.getFirst().getSourceState());
        }
        Assertions.assertEquals(QUESTIONNAIRE_ID_PREVIOUS,filter.getFirst().getQuestionnaireId());

        Assertions.assertEquals(14,filter.getFirst().getVariables().size());
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

    @ParameterizedTest(name = "[{index}] readPreviousJson_override_interrogation_id — {0}")
    @MethodSource("overridePreviousCases")
    @SneakyThrows
    void readPreviousJson_override_interrogation_id( String interrogationId,
                                                     int expectedVarCount,
                                                     Consumer<ContextualPreviousVariableDocument> assertionsForDoc){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        Files.copy(

                path.resolve("contextual_previous").resolve("ok.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, "ok.json");
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                path.resolve("contextual_previous").resolve("ok2.json"),
                SOURCE_PATH_PREVIOUS.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, "ok2.json");

        //THEN
        Assertions.assertEquals(2,previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).size());

        List<ContextualPreviousVariableDocument> filter =
                previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals(interrogationId)).toList();
        Assertions.assertEquals(1, filter.size());
        ContextualPreviousVariableDocument firstDoc = filter.getFirst();
        Assertions.assertEquals(QUESTIONNAIRE_ID_PREVIOUS, firstDoc.getQuestionnaireId());

        Assertions.assertAll(interrogationId + " metadata",
                () -> Assertions.assertEquals(QUESTIONNAIRE_ID_PREVIOUS, firstDoc.getQuestionnaireId()),
                () -> Assertions.assertEquals(expectedVarCount, firstDoc.getVariables().size())
        );

        assertionsForDoc.accept(firstDoc);
        Assertions.assertTrue(
                previousStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_PREVIOUS_COLLECTION_NAME).stream()
                        .noneMatch(d -> "AUTO108".equals(d.getInterrogationId())),
                "AUTO108 ne devrait plus être présent après override"
        );
    }


    static Stream<Arguments> overridePreviousCases() {
        return Stream.of(
                Arguments.of(
                        "AUTO104",
                        /* expectedVarCount */ 15,
                        (Consumer<ContextualPreviousVariableDocument>) doc -> {
                            assertVariable(doc, "TEXTECOURT", "");
                            assertVariable(doc, "TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
                            assertVariable(doc, "FLOAT", 50.25d);
                            assertVariableNull(doc, "INTEGER");
                            assertVariable(doc, "BOOLEEN", true);
                            assertVariable(doc, "DROPDOWN", "03");
                            assertVariable(doc, "QCM_B1", true);
                            assertVariable(doc, "QCM_B2", false);
                            assertVariable(doc, "QCM_B4", true);
                            assertVariable(doc, "TABLEAU2A11", 200);
                            assertVariable(doc, "TABLEAU2A12", 150);
                            assertVariable(doc, "TABLEAU2A23", 1000);
                            assertVariableNull(doc, "TABLEAU2A24");
                            assertVariable(doc, "TABOFATS1", 0, "AA");
                            assertVariable(doc, "TABOFATS1", 1, "");
                            assertVariable(doc, "TABOFATS1", 2, "BB");
                            assertVariable(doc, "TABOFATS1", 3, "CC");
                            assertVariable(doc, "TABOFATS3", 0, 5);
                            assertVariableNull(doc, "TABOFATS3", 1);
                            assertVariable(doc, "TABOFATS3", 2, 3);
                        }
                ),
                Arguments.of(
                        "AUTO200",
                        /* expectedVarCount */ 14,
                        (Consumer<ContextualPreviousVariableDocument>) doc -> {
                            assertVariable(doc, "TEXTECOURT", "test previous");
                            assertVariable(doc, "TEXTELONG", "");
                            assertVariable(doc, "FLOAT", 12.2d);
                            assertVariable(doc, "BOOLEEN", false);
                            assertVariable(doc, "DROPDOWN", "");
                            assertVariable(doc, "QCM_B1", false);
                            assertVariable(doc, "QCM_B2", false);
                            assertVariable(doc, "QCM_B5", true);
                            assertVariable(doc, "TABLEAU2A11", 1);
                            assertVariable(doc, "TABLEAU2A12", 2);
                            assertVariable(doc, "TABLEAU2A23", 3);
                            assertVariable(doc, "TABLEAU2A24", 4);
                            assertVariable(doc, "TABOFATS1", 0, "BB");
                            assertVariable(doc, "TABOFATS1", 1, "BB");
                            assertVariable(doc, "TABOFATS3", 0, 10);
                            assertVariable(doc, "TABOFATS3", 1, 4);
                            assertVariable(doc, "TABOFATS3", 2, 0);
                        }
                )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = {"ceci est une origine beaucoup trop longue"})
    @SneakyThrows
    void readPreviousJson_sourceState_too_long(String sourceState){
        //GIVEN
        String fileName = "ok.json";
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_previous").resolve(fileName),
                SOURCE_PATH_PREVIOUS.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, sourceState,
                fileName);
        Assertions.assertEquals(400,response.getStatusCode().value());
    }

    @Test
    @SneakyThrows
    void readPreviousJson_invalid_syntax(){
        String syntaxErrorFileName = "invalid_syntax.json";
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH_PREVIOUS.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null,
                syntaxErrorFileName);
        Assertions.assertEquals(400,response.getStatusCode().value());
    }
    @Test
    @SneakyThrows
    void readPreviousJson_not_a_json(){
        String syntaxErrorFileName = "not_a_json.xml";
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_previous").resolve(syntaxErrorFileName),
                SOURCE_PATH_PREVIOUS.resolve(syntaxErrorFileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null,
                syntaxErrorFileName);
        Assertions.assertEquals(400,response.getStatusCode().value());
    }


    @SneakyThrows
    @ParameterizedTest
    @ValueSource(strings = {"no_interrogationId.json", "only_one_interrogationId.json", "double_interrogationId.json"})
    @DisplayName("Previous json return 400 if no interrogationId, only one interrogationId, or double interrogationId")
    void readPreviousJson_no_interrogation_id(String fileName){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_PREVIOUS);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_previous").resolve(fileName),
                SOURCE_PATH_PREVIOUS.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = contextualVariableController.readContextualPreviousJson(QUESTIONNAIRE_ID_PREVIOUS, Mode.WEB, null, fileName);
        Assertions.assertEquals(400,response.getStatusCode().value());
    }


    //EXTERNAL
    @ParameterizedTest(name = "[{index}] readExternalJson — {0}")
    @MethodSource("readExternalCases")    @SneakyThrows
    void readExternalJson_test( String interrogationId,
                                int expectedVarCount,
                                Consumer<ContextualExternalVariableDocument> assertionsForDoc){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_external").resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        contextualVariableController.readContextualExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok.json");

        //THEN
        Assertions.assertEquals(2,externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).size());

        List<ContextualExternalVariableDocument> filter = externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals(interrogationId)).toList();
        Assertions.assertEquals(1,filter.size());

        ContextualExternalVariableDocument firstDoc = filter.getFirst();
        Assertions.assertEquals(QUESTIONNAIRE_ID_EXTERNAL, firstDoc.getQuestionnaireId());
        Assertions.assertEquals(expectedVarCount, firstDoc.getVariables().size());
        assertionsForDoc.accept(firstDoc);
    }


    static Stream<Arguments> readExternalCases() {
        return Stream.of(
                Arguments.of(
                        "AUTO204",
                        /* expectedVarCount */ 12,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", 302.34d);
                            assertVariable(doc, "CA", 22.45d);
                            assertVariable(doc, "COM_AUTRE", "blablablabla");
                            assertVariable(doc, "INTERRO_N_1", true);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "9560Y");
                            assertVariable(doc, "POIDS", 1.25);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", true);

                            assertVariable(doc, "TAB_EXTNUM", 0, 50);
                            assertVariable(doc, "TAB_EXTNUM", 1, 23);
                            assertVariable(doc, "TAB_EXTNUM", 2, 10);
                            assertVariableNull(doc, "TAB_EXTNUM", 3);

                            assertVariable(doc, "TAB_EXTCAR", 0, "A");
                            assertVariable(doc, "TAB_EXTCAR", 1, "");
                            assertVariable(doc, "TAB_EXTCAR", 2, "B");
                        }
                ),
                Arguments.of(
                        "AUTO208",
                        /* expectedVarCount */ 11,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", "");
                            assertVariable(doc, "COM_AUTRE", "");
                            assertVariable(doc, "SECTEUR", "123456789");
                            assertVariable(doc, "INTERRO_N_1", false);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "1014Z");
                            assertVariable(doc, "POIDS", 12);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", false);

                            assertVariable(doc, "TAB_EXTNUM", 0, 10);
                            assertVariable(doc, "TAB_EXTCAR", 0, "C");
                            assertVariable(doc, "TAB_EXTCAR", 1, "C");
                        }
                )
        );
    }

    @ParameterizedTest(name = "[{index}] readExternalJson_override_interrogation_id — {0}")
    @MethodSource("overrideExternalCases")
    @SneakyThrows
    void readExternalJson_override_interrogation_id(    String interrogationId,
                                                        int expectedVarCount,
                                                       Consumer<ContextualExternalVariableDocument> assertionsForDoc
    ){
        //GIVEN
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Path path = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY);
        Files.copy(
                path.resolve("contextual_external").resolve("ok.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok.json"),
                StandardCopyOption.REPLACE_EXISTING
        );
        contextualVariableController.readContextualExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok.json");
        Files.createDirectories(SOURCE_PATH_EXTERNAL);
        Files.copy(
                path.resolve("contextual_external").resolve("ok2.json"),
                SOURCE_PATH_EXTERNAL.resolve("ok2.json"),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN
        contextualVariableController.readContextualExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, "ok2.json");

        //THEN
        Assertions.assertEquals(2,externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME).size());

        List<ContextualExternalVariableDocument> filter =
                externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME)
                .stream().filter(doc -> doc.getInterrogationId().equals(interrogationId)).toList();
        Assertions.assertEquals(1,filter.size());
        Assertions.assertEquals(QUESTIONNAIRE_ID_EXTERNAL,filter.getFirst().getQuestionnaireId());
        Assertions.assertEquals(expectedVarCount,filter.getFirst().getVariables().size());

        assertionsForDoc.accept(filter.getFirst());

        // L'ancien AUTO208 ne doit plus être présent
        Assertions.assertTrue(
                externalStub.getMongoStub().get(Constants.MONGODB_CONTEXTUAL_EXTERNAL_COLLECTION_NAME)
                        .stream().noneMatch(d -> "AUTO208".equals(d.getInterrogationId())),
                "AUTO208 ne devrait plus être présent après override"
        );
    }


    static Stream<Arguments> overrideExternalCases() {
        return Stream.of(
                Arguments.of(
                        "AUTO204",
                        /* expectedVarCount */ 12,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", 302.34d);
                            assertVariable(doc, "CA", 22.45d);
                            assertVariable(doc, "COM_AUTRE", "blablablabla");
                            assertVariable(doc, "INTERRO_N_1", true);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "9560Y");
                            assertVariable(doc, "POIDS", 1.25);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", true);

                            assertVariable(doc, "TAB_EXTNUM", 0, 50);
                            assertVariable(doc, "TAB_EXTNUM", 1, 23);
                            assertVariable(doc, "TAB_EXTNUM", 2, 10);
                            assertVariableNull(doc, "TAB_EXTNUM", 3);

                            assertVariable(doc, "TAB_EXTCAR", 0, "A");
                            assertVariable(doc, "TAB_EXTCAR", 1, "");
                            assertVariable(doc, "TAB_EXTCAR", 2, "B");
                        }
                ),
                Arguments.of(
                        "AUTO200",
                        /* expectedVarCount */ 11,
                        (Consumer<ContextualExternalVariableDocument>) doc -> {
                            assertVariable(doc, "TVA", "");
                            assertVariable(doc, "COM_AUTRE", "");
                            assertVariable(doc, "SECTEUR", "123456789");
                            assertVariable(doc, "INTERRO_N_1", false);
                            assertVariable(doc, "INTERRO_N_2", false);
                            assertVariable(doc, "NAF25", "1014Z");
                            assertVariable(doc, "POIDS", 12);
                            assertVariable(doc, "MILLESIME", "2024");
                            assertVariable(doc, "NSUBST", false);

                            assertVariable(doc, "TAB_EXTNUM", 0, 10);
                            assertVariable(doc, "TAB_EXTCAR", 0, "C");
                            assertVariable(doc, "TAB_EXTCAR", 1, "C");
                        }
                )
        );
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
                Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("contextual_external").resolve(fileName),
                SOURCE_PATH_EXTERNAL.resolve(fileName),
                StandardCopyOption.REPLACE_EXISTING
        );

        //WHEN + THEN
        ResponseEntity<Object> response = contextualVariableController.readContextualExternalJson(QUESTIONNAIRE_ID_EXTERNAL, Mode.WEB, fileName);
        Assertions.assertEquals(400,response.getStatusCode().value());
    }

    //UTILS
    //THEN
    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String variableName,
                                       String expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));
    }

    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String variableName,
                                       double expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Double.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));
    }

    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String variableName,
                                       boolean expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Boolean.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));    }

    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String variableName,
                                       int expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Integer.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));    }

    private static void assertVariableNull(ContextualPreviousVariableDocument document,
                                           String variableName
    ) {
        Assertions.assertNull(document.getVariables().get(variableName));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariableNull(ContextualPreviousVariableDocument document,
                                           String arrayVariableName,
                                           int index    ) {
        var list = (List<Object>) document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertNull(list.get(index));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String arrayVariableName,
                                       int index,
                                       String expectedValue
    ) throws ClassCastException{
        var list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertInstanceOf(String.class,list.get(index));
        Assertions.assertEquals(expectedValue,list.get(index));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(ContextualPreviousVariableDocument document,
                                       String arrayVariableName,
                                       int index,
                                       int expectedValue
    ) throws ClassCastException{
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertInstanceOf(Integer.class,list.get(index));
        Assertions.assertEquals(expectedValue,(Integer)list.get(index));
    }

    private List<ContextualPreviousVariableDocument> getContextualPreviousTestDocuments() {
        List<ContextualPreviousVariableDocument> contextualPreviousVariableDocumentList = new ArrayList<>();

        ContextualPreviousVariableDocument contextualPreviousVariableDocument = new ContextualPreviousVariableDocument();
        contextualPreviousVariableDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        contextualPreviousVariableDocument.setInterrogationId(INTERROGATION_ID_1);
        contextualPreviousVariableDocument.setVariables(new HashMap<>());
        contextualPreviousVariableDocument.getVariables().put("TEXTECOURT", "");
        contextualPreviousVariableDocument.getVariables().put("TEXTELONG", "test d'une donnée antérieure sur un texte long pour voir comment ça marche");
        contextualPreviousVariableDocument.getVariables().put("FLOAT", 50.25d);
        contextualPreviousVariableDocument.getVariables().put("INTEGER", null);
        contextualPreviousVariableDocument.getVariables().put("BOOLEEN", true);
        contextualPreviousVariableDocument.getVariables().put("DROPDOWN", "03");
        contextualPreviousVariableDocument.getVariables().put("QCM_B1", true);
        contextualPreviousVariableDocument.getVariables().put("QCM_B2", false);
        contextualPreviousVariableDocument.getVariables().put("QCM_B4", true);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A11", 200);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A12", 150);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A23", 1000);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A24", null);
        contextualPreviousVariableDocument.getVariables().put("TABOFATS1", List.of("AA","","BB","CC"));
        contextualPreviousVariableDocument.getVariables().put("TABOFATS3", Arrays.asList(5,null,3));
        contextualPreviousVariableDocumentList.add(contextualPreviousVariableDocument);

        contextualPreviousVariableDocument = new ContextualPreviousVariableDocument();
        contextualPreviousVariableDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        contextualPreviousVariableDocument.setInterrogationId(INTERROGATION_ID_2);
        contextualPreviousVariableDocument.setVariables(new HashMap<>());
        contextualPreviousVariableDocument.getVariables().put("TEXTECOURT", "test previous");
        contextualPreviousVariableDocument.getVariables().put("TEXTELONG", "");
        contextualPreviousVariableDocument.getVariables().put("FLOAT", 12.2d);
        contextualPreviousVariableDocument.getVariables().put("BOOLEEN", false);
        contextualPreviousVariableDocument.getVariables().put("DROPDOWN", "");
        contextualPreviousVariableDocument.getVariables().put("QCM_B1", false);
        contextualPreviousVariableDocument.getVariables().put("QCM_B2", false);
        contextualPreviousVariableDocument.getVariables().put("QCM_B5", true);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A11", 1);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A12", 2);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A23", 3);
        contextualPreviousVariableDocument.getVariables().put("TABLEAU2A24", 4);
        contextualPreviousVariableDocument.getVariables().put("TABOFATS1", List.of("BB","BB"));
        contextualPreviousVariableDocument.getVariables().put("TABOFATS3", List.of(10,4,0));
        contextualPreviousVariableDocumentList.add(contextualPreviousVariableDocument);

        return contextualPreviousVariableDocumentList;
    }

    private List<ContextualExternalVariableDocument> getContextualExternalTestDocuments() {
        List<ContextualExternalVariableDocument> contextualExternalVariableDocumentList = new ArrayList<>();

        ContextualExternalVariableDocument contextualExternalVariableDocument = new ContextualExternalVariableDocument();
        contextualExternalVariableDocument.setQuestionnaireId(QUESTIONNAIRE_ID);
        contextualExternalVariableDocument.setInterrogationId(INTERROGATION_ID_1);
        contextualExternalVariableDocument.setVariables(new HashMap<>());
        contextualExternalVariableDocument.getVariables().put("TVA", 302.34d);
        contextualExternalVariableDocument.getVariables().put("CA", 22.45d);
        contextualExternalVariableDocument.getVariables().put("COM_AUTRE", "blablablabla");
        contextualExternalVariableDocument.getVariables().put("SECTEUR", "110110110");
        contextualExternalVariableDocument.getVariables().put("CATEGORIE", "");
        contextualExternalVariableDocument.getVariables().put("INTERRO_N_1", true);
        contextualExternalVariableDocument.getVariables().put("INTERRO_N_2", false);
        contextualExternalVariableDocument.getVariables().put("NAF25", "9560Y");
        contextualExternalVariableDocument.getVariables().put("POIDS", null);
        contextualExternalVariableDocument.getVariables().put("MILLESIME", "2024");
        contextualExternalVariableDocument.getVariables().put("NSUBST", true);
        contextualExternalVariableDocument.getVariables().put("TAB_EXTNUM", Arrays.asList(50,23,10,null));
        contextualExternalVariableDocument.getVariables().put("TAB_EXTCAR", Arrays.asList("A", "", "B"));
        contextualExternalVariableDocumentList.add(contextualExternalVariableDocument);
        return contextualExternalVariableDocumentList;
    }


    private void assertDocumentEqualToDto(ContextualPreviousVariableDocument contextualPreviousVariableDocument,
                                          ContextualVariableModel contextualVariableModel) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : contextualPreviousVariableDocument.getVariables().entrySet()) {
            //Get contextual previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    contextualVariableModel.contextualPrevious().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    private void assertDocumentEqualToDto(ContextualExternalVariableDocument contextualExternalVariableDocument,
                                          ContextualVariableModel contextualVariableModel) {
        //For each variable of document
        for (Map.Entry<String, Object> documentVariable : contextualExternalVariableDocument.getVariables().entrySet()) {
            //Get contextual previous dtos of that variable (1 per iteration)
            List<VariableQualityToolDto> variableQualityToolDtosOfEntry =
                    contextualVariableModel.contextualExternal().stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(documentVariable.getKey())
                    ).toList();
            assertEntryEqualToDto(documentVariable, variableQualityToolDtosOfEntry);
        }
    }

    @SuppressWarnings("unchecked")
    private void assertEntryEqualToDto(Map.Entry<String, Object> documentVariable,
                                       List<VariableQualityToolDto> variableQualityToolDtosOfEntry) {
        Assertions.assertFalse(variableQualityToolDtosOfEntry.isEmpty());

        //If that variable is not a list
        if (!(documentVariable.getValue() instanceof List<?>)) {
            Assertions.assertEquals(1,variableQualityToolDtosOfEntry.size());
            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfEntry.getFirst().getVariableStateDtoList();

            Assertions.assertEquals(1,variableStateDtos.size()); // Only 1 state
            Assertions.assertEquals(DataState.COLLECTED,variableStateDtos.getFirst().getState());
            Assertions.assertEquals(documentVariable.getValue(),variableStateDtos.getFirst().getValue());
            return;
        }
        int i = 1;
        for (Object documentVariableElement : (List<Object>) documentVariable.getValue()) {
            int finalI = i;
            List<VariableQualityToolDto> variableQualityToolDtosOfIteration =
                    variableQualityToolDtosOfEntry.stream().filter(
                            variableQualityToolDto -> variableQualityToolDto.getIteration().equals(finalI)
                    ).toList();
            Assertions.assertEquals(1,variableQualityToolDtosOfIteration.size());

            List<VariableStateDto> variableStateDtos =
                    variableQualityToolDtosOfIteration.getFirst().getVariableStateDtoList();
            Assertions.assertEquals(1,variableStateDtos.size());
            Assertions.assertEquals(DataState.COLLECTED,variableStateDtos.getFirst().getState());
            Assertions.assertEquals(documentVariableElement,variableStateDtos.getFirst().getValue());
            i++;
        }
    }

    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String variableName,
                                       String expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));
    }

    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String variableName,
                                       double expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Double.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));
    }

    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String variableName,
                                       boolean expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Boolean.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));        }

    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String variableName,
                                       int expectedValue
    ) {
        Assertions.assertNotNull(document.getVariables().get(variableName));
        Assertions.assertInstanceOf(Integer.class,document.getVariables().get(variableName));
        Assertions.assertEquals(expectedValue,document.getVariables().get(variableName));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariableNull(ContextualExternalVariableDocument document,
                                           String arrayVariableName,
                                           int index
    ) {
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertNull(list.get(index));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String arrayVariableName,
                                       int index,
                                       String expectedValue
    ) throws ClassCastException{
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertInstanceOf(String.class,list.get(index));
        Assertions.assertEquals(expectedValue,list.get(index));
    }

    @SuppressWarnings("unchecked")
    private static void assertVariable(ContextualExternalVariableDocument document,
                                       String arrayVariableName,
                                       int index,
                                       int expectedValue
    ) throws ClassCastException{
        List<Object> list = (List<Object>)document.getVariables().get(arrayVariableName);
        Assertions.assertTrue(list.size()>index);
        Assertions.assertInstanceOf(Integer.class,list.get(index));
        Assertions.assertEquals(expectedValue,(Integer)list.get(index));

    }
}
