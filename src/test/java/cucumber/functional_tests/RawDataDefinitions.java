package cucumber.functional_tests;

import cucumber.functional_tests.config.CucumberSpringConfiguration;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.rest.responses.RawResponseController;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
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
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.interfaces.RSAPublicKey;

@Slf4j
@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class RawDataDefinitions {


    @LocalServerPort
    private int port;

    private String BASE_URL;
    @Autowired
    private TestRestTemplate rest;

    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    LunaticJsonRawDataService lunaticJsonRawDataService = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub);
    Config config = new ConfigStub();
    FileUtils fileUtils = new FileUtils(config);
    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    RawResponseController rawResponseController = new RawResponseController(
            lunaticJsonRawDataService
//           , new ControllerUtils(fileUtils),
//            new MetadataService(),
//            new SurveyUnitService(surveyUnitPersistencePortStub),
//            surveyUnitQualityService,
//            fileUtils
    );
    Path rawDataFilePath;
    String rawJsonData;
    ResponseEntity<String> response;
    int nbRawSaved = 0;

    @Before
    public void init(){
        this.lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        log.info("rest autowired : {}", rest.getRootUri());
        BASE_URL = "http://localhost:" + port + "/";

    }

    private RSAPublicKey generateTestKey() {
        try {
            KeyPair keyPair = KeyPairGenerator.getInstance("RSA").generateKeyPair();
            return (RSAPublicKey) keyPair.getPublic();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

    @Given("We have raw data file in {string}")
    public void set_input_file(String rawDataFile) throws IOException {
        this.rawDataFilePath = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,rawDataFile);
        rawJsonData = Files.readString(rawDataFilePath);
    }

    @When("We save that raw data for web campaign {string}, questionnaire {string}, interrogation {string}")
    public void save_raw_data(String campaignId, String questionnaireId, String interrogationId) throws IOException {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer fake_token");
        String url = String.format("%sresponses/raw/lunatic-json/save?campaignName=%s&questionnaireId=%s&interrogationId=%s&surveyUnitId=%s&mode=%s",
                BASE_URL,
                campaignId,
                questionnaireId,
                interrogationId,
                null,
                Mode.WEB
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(rawJsonData.trim(), headers);
        try {
            response = rest.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if(response.getStatusCode().is2xxSuccessful()){nbRawSaved++;}
        } catch (Exception e) {
            response = new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Then("We should have {int} raw data document")
    public void check_document_count(int expectedCount){
        Assertions.assertThat(nbRawSaved).isEqualTo(expectedCount);
    }


    @Then("We should have {int} status code")
    public void check_response_status_code(int expectedStatusCode){
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }




}
