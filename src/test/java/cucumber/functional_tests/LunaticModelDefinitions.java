package cucumber.functional_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.rest.LunaticModelController;
import fr.insee.genesis.controller.rest.responses.QuestionnaireController;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.lunaticmodel.LunaticModelService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.stubs.LunaticModelPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.stream.Stream;

@Slf4j
public class LunaticModelDefinitions {

    private static final String LUNATIC_FILE_PATTERN = "lunatic[\\w,\\s-]+\\.json";

    LunaticModelPersistanceStub lunaticModelPersistanceStub = new LunaticModelPersistanceStub();
    LunaticModelController lunaticModelController = new LunaticModelController(new LunaticModelService(lunaticModelPersistanceStub));

    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    QuestionnaireController questionnaireController = new QuestionnaireController(
            new SurveyUnitService(surveyUnitPersistencePortStub)
    );

    private String baseUrl;
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    //Test variables
    private Path lunaticModelJsonPath;
    private String lunaticModelSaveBody;
    private ResponseEntity<String> lastResponse;

    @Before
    public void init(){
        lunaticModelPersistanceStub.getMongoStub().clear();
        baseUrl = "http://localhost:" + port + "/";
        log.info("rest autowired : {}", rest.getRootUri());
    }


    //GIVENS
    @Given("We have a lunatic model in database with questionnaire id {string} from the json of {string}")
    public void load_lunatic_model_json(
            String questionnaireId,
            String campaignId
    ) throws Exception {
        lunaticModelJsonPath = getLunaticJsonPath(campaignId);

        LunaticModelDocument lunaticModelDocument = LunaticModelDocument.builder()
                .questionnaireId(questionnaireId)
                .lunaticModel(JsonUtils.jsonToMap(Files.readString(lunaticModelJsonPath)))
                .build();
        lunaticModelPersistanceStub.getMongoStub().add(lunaticModelDocument);
    }

    @Given("We have a lunatic model json file in spec folder {string}")
    public void load_lunatic_model_json_file(String specFolderName) throws IOException {
        lunaticModelJsonPath = getLunaticJsonPath(specFolderName);
    }

    @Given("We have a response in database with campaign id {string}, questionnaire id {string} and interrogation id " +
            "{string}")
    public void load_response(String campaignId, String questionnaireId, String interrogationId) {
        surveyUnitPersistencePortStub.getMongoStub().add(
                SurveyUnitModel.builder()
                        .campaignId(campaignId)
                        .questionnaireId(questionnaireId)
                        .interrogationId(interrogationId)
                        .build()
        );
    }


    //WHENS
    @When("We save that lunatic model json file with questionnaire id {string}")
    public void save_lunatic_model(String questionnaireId) throws Exception {
        lunaticModelSaveBody = Files.readString(lunaticModelJsonPath);

        lastResponse = lunaticModelController.saveRawResponsesFromJsonBody(
                questionnaireId,
                JsonUtils.jsonToMap(lunaticModelSaveBody)
        );
    }
    @When("We try to save that lunatic model json file with questionnaire id {string} with Spring context")
    public void save_lunatic_model_spring(String questionnaireId) throws IOException {
        lunaticModelSaveBody = Files.readString(lunaticModelJsonPath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer fake_token");
        String url = String.format("%slunatic-model/save?questionnaireId=%s",
                baseUrl,
                questionnaireId
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(lunaticModelSaveBody,headers);
        lastResponse = rest.exchange(url, HttpMethod.PUT, requestEntity, String.class);
    }

    @When("We get lunatic model for questionnaire {string}")
    public void get_lunatic_model(String questionnaireId) throws JsonProcessingException {
        lastResponse = lunaticModelController.getLunaticModelFromQuestionnaireId(questionnaireId);
    }

    @When("We get questionnaire id for interrogation {string}")
    public void get_questionnaire_id(String interrogationId) {
        lastResponse = questionnaireController.getQuestionnaireByInterrogation(interrogationId);
    }


    //THENS
    @Then("We should have that lunatic model as response")
    public void check_lunatic_model() throws Exception {
        ObjectMapper objectMapper = new ObjectMapper();
        JsonNode originalLunaticModel = objectMapper.readTree(Files.readString(lunaticModelJsonPath));
        JsonNode gotLunaticModel = objectMapper.readTree(lastResponse.getBody());

        Assertions.assertThat(originalLunaticModel).isEqualTo(gotLunaticModel);
    }

    @Then("We should have a document with id {string} and the contents from the body")
    public void check_lunatic_model_document(String documentQuestionnaireId) throws Exception {
        if(!lastResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Got error code {} with body {}",
                    lastResponse.getStatusCode().value(), lastResponse.getBody());
        }

        List<LunaticModelDocument> lunaticModelDocuments = lunaticModelPersistanceStub.getMongoStub().stream().filter(
                document -> document.questionnaireId().equals(documentQuestionnaireId)
        ).toList();

        Assertions.assertThat(lunaticModelDocuments).hasSize(1);
        Assertions.assertThat(lunaticModelDocuments.getFirst().lunaticModel()).isEqualTo(JsonUtils.jsonToMap(lunaticModelSaveBody));
    }

    @Then("We should have {string} as response")
    public void check_response(String expectedResponse) {
        if(!lastResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Got error code {} with body {}",
                    lastResponse.getStatusCode().value(), lastResponse.getBody());
        }

        Assertions.assertThat(lastResponse.getBody()).isEqualTo(expectedResponse);
    }

    @Then("We should have a {int} error code")
    public void check_error_code(int expectedErrorCode) {
        Assertions.assertThat(lastResponse.getStatusCode().value()).isEqualTo(expectedErrorCode);
    }

    //UTILS
    private Path getLunaticJsonPath(String campaignId) throws IOException {
        Path campaignPath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY).resolve("specs").resolve(campaignId);
        return lookForLunaticJsonPath(campaignPath);
    }

    private Path lookForLunaticJsonPath(Path inPath) throws IOException {
        try (Stream<Path> files = Files.find(Path.of(String.valueOf(inPath)), 10,
                (path, basicFileAttributes) -> path.toFile().getName().toLowerCase().matches(LUNATIC_FILE_PATTERN))) {
            return files.findFirst()
                    .orElseThrow(() -> new RuntimeException("No file (%s) found in ".formatted(LUNATIC_FILE_PATTERN) + inPath));
        }
    }
}
