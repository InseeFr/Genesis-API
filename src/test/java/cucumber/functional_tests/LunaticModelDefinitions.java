package cucumber.functional_tests;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticModelMapper;
import fr.insee.genesis.stubs.LunaticModelPersistanceStub;
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

    private String baseUrl;
    @LocalServerPort
    int port;

    @Autowired
    TestRestTemplate rest;

    //Test variables
    private String questionnaireId;
    private Path lunaticModelJsonPath;
    private String lunaticModelSaveBody;
    private ResponseEntity<String> lastResponse;

    @Before
    public void init(){
        lunaticModelPersistanceStub.getMongoStub().clear();
        baseUrl = "http://localhost:" + port + "/";
        log.info("rest autowired : {}", rest.getRootUri());

        //Clean variables
//        questionnaireId = null;
//        lunaticModelJsonPath = null;
//        lunaticModelSaveBody = null;
//        lastResponse = null;
    }


    //GIVENS
    @Given("We have a lunatic model in database with questionnaire id {string} from the json of {string}")
    public void load_lunatic_model_json(
            String questionnaireId,
            String campaignId
    ) throws Exception {
        this.questionnaireId = questionnaireId;
        Path jsonPath = getLunaticJsonPath(campaignId);

        LunaticModelDocument lunaticModelDocument = LunaticModelDocument.builder()
                .questionnaireId(questionnaireId)
                .lunaticModel(JsonUtils.jsonToMap(Files.readString(jsonPath)))
                .build();
        lunaticModelPersistanceStub.getMongoStub().add(lunaticModelDocument);
    }

    @Given("We have a lunatic model json file in spec folder {string}")
    public void load_lunatic_model_json_file(String specFolderName) throws IOException {
        lunaticModelJsonPath = getLunaticJsonPath(specFolderName);
    }


    //WHENS
    @When("We save that lunatic model json file with questionnaire id {string}")
    public void save_lunatic_model(String questionnaireId) throws IOException {
        this.questionnaireId = questionnaireId;
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
    public void get_lunatic_model(String questionnaireId){
        this.questionnaireId = questionnaireId;
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer fake_token");
        String url = String.format("%slunatic-model/get?questionnaireId=%s",
                baseUrl,
                questionnaireId
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        lastResponse = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
    }

    @When("We get questionnaire id for interrogation {string}")
    public void get_questionnaire_id(String interrogationId) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer fake_token");
        String url = String.format("%squestionnaires/by-interrogation?interrogationId=%s",
                baseUrl,
                interrogationId
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(headers);
        lastResponse = rest.exchange(url, HttpMethod.GET, requestEntity, String.class);
    }


    //THENS
    @Then("We should have that lunatic model as response")
    public void check_lunatic_model() throws Exception {
        if(!lastResponse.getStatusCode().is2xxSuccessful()) {
            log.error("Got error code {} with body {}",
                    lastResponse.getStatusCode().value(), lastResponse.getBody());
        }

        Assertions.assertThat(lastResponse.getStatusCode().is2xxSuccessful()).isTrue();
        LunaticModelModel lunaticModelModel = LunaticModelModel.builder()
                .questionnaireId(questionnaireId)
                .lunaticModel(JsonUtils.jsonToMap(lastResponse.getBody()))
                .build();

        LunaticModelModel inDatabaseLunaticModel =
                LunaticModelMapper.INSTANCE.documentToModel(lunaticModelPersistanceStub.getMongoStub().getFirst());

        Assertions.assertThat(inDatabaseLunaticModel.lunaticModel().equals(lunaticModelModel.lunaticModel())).isTrue();
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
        Assertions.assertThat(lunaticModelDocuments.getFirst().lunaticModel()
                .equals(JsonUtils.jsonToMap(lunaticModelSaveBody))
        ).isTrue();
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
