package cucumber.functional_tests;

import cucumber.functional_tests.config.CucumberSpringConfiguration;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.rest.responses.RawResponseController;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitQualityToolPerretAdapterStub;
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
import java.util.Collections;
import java.util.List;

@Slf4j
@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class RawDataDefinitions {


    @LocalServerPort
    private int port;

    private String baseUrl;
    @Autowired
    private TestRestTemplate rest;

    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    static QuestionnaireMetadataPersistancePortStub questionnaireMetadataPersistancePortStub = new QuestionnaireMetadataPersistancePortStub();
    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    DataProcessingContextPersistancePortStub contextStub = new DataProcessingContextPersistancePortStub();
    Config config = new ConfigStub();
    FileUtils fileUtils = new FileUtils(config);
    SurveyUnitService surveyUnitService = new SurveyUnitService(surveyUnitPersistencePortStub, new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub), fileUtils);
    ControllerUtils controllerUtils = new ControllerUtils(fileUtils);
    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub =
            new DataProcessingContextPersistancePortStub();
    SurveyUnitQualityToolPerretAdapterStub surveyUnitQualityToolPerretAdapterStub = new SurveyUnitQualityToolPerretAdapterStub();
    LunaticJsonRawDataService lunaticJsonRawDataService =
            new LunaticJsonRawDataService(
                    lunaticJsonRawDataPersistanceStub,
                    controllerUtils,
                    new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub),
                    surveyUnitService,
                    surveyUnitQualityService,
                    fileUtils,
                    new DataProcessingContextService(
                            dataProcessingContextPersistancePortStub,
                            surveyUnitPersistencePortStub),
                    surveyUnitQualityToolPerretAdapterStub,
                    config,
                    dataProcessingContextPersistancePortStub);

    RawResponseController rawResponseController = new RawResponseController(
            lunaticJsonRawDataService
    );
    Path rawDataFilePath;
    String rawJsonData;
    ResponseEntity<String> response;
    int nbRawSaved = 0;

    @Before
    public void init(){
        this.lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        log.info("rest autowired : {}", rest.getRootUri());
        baseUrl = "http://localhost:" + port + "/";

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

    @When("We call save raw data endpoint for web campaign {string}, questionnaire {string}, interrogation {string}")
    public void save_raw_data_spring(String campaignId, String questionnaireId, String interrogationId)  {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer fake_token");
        String url = String.format("%sresponses/raw/lunatic-json/save?campaignName=%s&questionnaireId=%s&interrogationId=%s&surveyUnitId=%s&mode=%s",
                baseUrl,
                campaignId,
                questionnaireId,
                interrogationId,
                null,
                Mode.WEB
        );

        HttpEntity<String> requestEntity = new HttpEntity<>(rawJsonData.trim(), headers);
        try {
            response = rest.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if(response.getStatusCode().is2xxSuccessful()){
                nbRawSaved++;
                return;
            }
            log.error(response.getBody());
        } catch (Exception e) {
            log.error(e.toString());
            response = new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @When("We call save raw data endpoint with validation")
    public void save_raw_data_validation_spring() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer fake_token");
        String url = "%sresponses/raw/lunatic-json".formatted(baseUrl);

        HttpEntity<String> requestEntity = new HttpEntity<>(rawJsonData.trim(), headers);
        try {
            response = rest.exchange(url, HttpMethod.PUT, requestEntity, String.class);
            if(response.getStatusCode().is2xxSuccessful()){
                nbRawSaved++;
                return;
            }
            log.error(response.getBody());
        } catch (Exception e) {
            log.error(e.toString());
            response = new ResponseEntity<>("Unexpected error", HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @When("We save that raw data for web campaign {string}, questionnaire {string}, interrogation {string}")
    public void save_raw_data(String campaignId, String questionnaireId, String interrogationId) throws IOException {
        if(rawDataFilePath == null){
            throw new RuntimeException("Raw data file path is null !");
        }

        response = rawResponseController.saveRawResponsesFromJsonBody(
                campaignId,
                questionnaireId,
                interrogationId,
                null,
                Mode.WEB,
                JsonUtils.jsonToMap(Files.readString(rawDataFilePath))
        );
    }

    @When("We save that raw data with validation")
    public void save_raw_data_with_validation() throws IOException {
        if(rawDataFilePath == null){
            throw new RuntimeException("Raw data file path is null !");
        }

        response = rawResponseController.saveRawResponsesFromJsonBodyWithValidation(
                JsonUtils.jsonToMap(Files.readString(rawDataFilePath))
        );
    }

    @When("We process raw data for campaign {string}, questionnaire {string} and interrogation {string}")
    public void process_raw_data(
            String campaignId,
            String questionnaireId,
            String interrogationId

    ) {
        List<String> interrogationIdList = Collections.singletonList(interrogationId);

        response = rawResponseController.processJsonRawData(
                campaignId,
                questionnaireId,
                interrogationIdList
        );
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }

    @Then("We should have {int} raw data document")
    public void check_document_count(int expectedCount){
        Assertions.assertThat(nbRawSaved).isEqualTo(expectedCount);
    }


    @Then("We should have {int} status code")
    public void check_response_status_code(int expectedStatusCode){
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    @Then("For collected variable {string} in survey unit {string} we should have {string} for " +
            "iteration {int}")
    public void check_collected_variable_content_in_mongo(
            String collectedVariableName,
            String interrogationId,
            String expectedValue,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                surveyUnitModel.getCollectedVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(collectedVariableName)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedCollectedVariables).hasSize(1);

        VariableModel variableModel = concernedCollectedVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("For external variable {string} in survey unit {string} we should have {string} for " +
            "iteration {int}")
    public void check_external_variable_content_in_mongo(
            String externalVariableName,
            String interrogationId,
            String expectedValue,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedExternalVariables =
                surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(externalVariableName)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedExternalVariables).hasSize(1);

        VariableModel variableModel = concernedExternalVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }


    @Then("In surveyUnit {string} of the campaign {string} we must have {string} as contextualId, " +
            "isCapturedIndirectly to {string} and validationDate null")
    public void check_optional_values(String interrogationId, String campaignId, String expectedContextualId,
                                       String expectedCapturedIndirectly) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getCampaignId().equals(campaignId)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        Assertions.assertThat(concernedSurveyUnitModels.getFirst().getContextualId()).isEqualTo(expectedContextualId);
        Assertions.assertThat(concernedSurveyUnitModels.getFirst().getIsCapturedIndirectly()).isEqualTo(Boolean.parseBoolean(expectedCapturedIndirectly));
        Assertions.assertThat(concernedSurveyUnitModels.getFirst().getValidationDate()).isNull();
    }
}
