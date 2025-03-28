package cucumber.functional_tests;

import cucumber.TestConstants;
import cucumber.config.CucumberSpringConfiguration;
import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.controller.rest.responses.ResponseController;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import io.cucumber.spring.CucumberContextConfiguration;
import org.assertj.core.api.Assertions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

@CucumberContextConfiguration
@SpringBootTest(classes = CucumberSpringConfiguration.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-cucumber")
public class MainDefinitions {
    String directory;
    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    final Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);

    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    SurveyUnitPersistencePortStub surveyUnitPersistence = new SurveyUnitPersistencePortStub();
    Config config = new ConfigStub();
    ResponseEntity<List<SurveyUnitModel>> surveyUnitModelResponse;
    ResponseEntity<SurveyUnitQualityToolDto> surveyUnitLatestStatesResponse;

    ResponseController responseController = new ResponseController(
            new SurveyUnitService(surveyUnitPersistence),
            surveyUnitQualityService,
            new FileUtils(config),
            new ControllerUtils(new FileUtils(config)),
            new AuthUtils(config),
            new MetadataService()
        );

    List<SurveyUnitModel> surveyUnitModels;

    //BEFOREs

    @Before
    public void init() {
        this.surveyUnitPersistence.getMongoStub().clear();
    }

    //GIVENs

    @Given("We have data in directory {string}")
    public void init_folder(String directory) throws IOException {
        this.directory = directory;
        inDirectory = inDirectory.resolve(directory);
        Files.createDirectories(inDirectory);
    }

    @Given("We copy data file {string} to that directory")
    public void copy_data_file(String dataFile) throws IOException {
        Path dataFilePath = Paths.get(TestConstants.TEST_RESOURCES_DIRECTORY).resolve(dataFile);
        Files.copy(dataFilePath, this.inDirectory.resolve(dataFilePath.getFileName().toString()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    //WHENs

    @When("We create survey unit models from file {string} with DDI {string}")
    public void get_models(String fileName, String ddiName) throws IOException, ParserConfigurationException,
            SAXException, GenesisException, MetadataParserException {
        Path filePath = inDirectory.resolve(fileName);
        Path ddiFilePath = ddiDirectory.resolve(directory).resolve(ddiName);

        if(fileName.endsWith(".xml")){
            LunaticXmlDataParser parser = new LunaticXmlDataParser();
            LunaticXmlCampaign campaign;
            campaign = parser.parseDataFile(filePath);
            VariablesMap variablesMap = DDIReader.getMetadataFromDDI(
                    ddiFilePath.toFile().toURI().toURL().toString(),
                    new FileInputStream(ddiFilePath.toFile())
            ).getVariables();
            List<SurveyUnitModel> surveyUnitModels1 = new ArrayList<>();
            for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                surveyUnitModels1.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getCampaignId(), Mode.WEB));
            }
            surveyUnitQualityService.verifySurveyUnits(surveyUnitModels1,variablesMap);
            surveyUnitModels = surveyUnitModels1;
        }
    }

    @When("We save data from that directory")
    public void get_su_models_from_folder() throws Exception {
        responseController.saveResponsesFromXmlCampaignFolder(this.inDirectory.getFileName().toString(), null);
    }

    @When("We delete that directory")
    public void delete_directory() throws IOException {
        org.springframework.util.FileSystemUtils.deleteRecursively(inDirectory);
    }

    @When("We extract survey unit data with questionnaireId {string} and interrogationId {string}")
    public void extract_survey_data(String questionnaireId, String interrogationId) {
        this.surveyUnitModelResponse = responseController.getLatestByInterrogation(interrogationId, questionnaireId);
    }

    @When("We extract survey unit latest states with questionnaireId {string} and interrogationId {string}")
    public void extract_survey_unit_latest_states(String questionnaireId, String interrogationId) {
        this.surveyUnitLatestStatesResponse =
                responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId,
                questionnaireId);
    }

    //THENs

    @Then("There should be {int} {string} SurveyUnit in database")
    public void check_surveyunits_by_state(int expectedCount, String expectedStatus) {
        Assertions.assertThat(this.surveyUnitPersistence.getMongoStub().stream().filter(
                surveyUnitModel -> surveyUnitModel.getState().name().equals(expectedStatus)
        ).toList()).hasSize(expectedCount);
    }



    @Then("For SurveyUnit {string} there should be at least one {string} SurveyUnit Model")
    public void check_expected_datastate_model(String interrogationId, String expectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(expectedDataState)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).isNotEmpty();
    }

    @Then("For SurveyUnit {string} there shouldn't be a {string} SurveyUnit Model")
    public void check_unexpected_datastate_model(String interrogationId, String unexpectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(unexpectedDataState)
                && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).isEmpty();
    }

    @Then("We should have a {string} Survey Unit model for survey unit {string} with {string} filled with {string} for iteration " +
            "{int}")
    public void check_survey_unit_model_content(String dataState, String interrogationId, String variableName,
                                              String expectedValue, int iteration) {
        //Get model
       List<SurveyUnitModel> concernedSurveyUnitModels = this.surveyUnitModels.stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(dataState)
                && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(concernedSurveyUnitModels).isNotEmpty();
        SurveyUnitModel concernedSurveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                concernedSurveyUnitModel.getCollectedVariables().stream().filter(variable ->
                    variable.varId().equals(variableName)
                    && variable.iteration().equals(iteration)
        ).toList();

        Assertions.assertThat(concernedCollectedVariables).isNotEmpty().hasSize(1);

        VariableModel concernedCollectedVariable = concernedCollectedVariables.getFirst();

        //Value assertion
        Assertions.assertThat(concernedCollectedVariable.value()).isEqualTo(expectedValue);
    }

    @Then("For collected variable {string} in survey unit {string} we should have {string} and scope {string} for " +
            "iteration {int}")
    public void check_collected_variable_content_in_mongo(
            String collectedVariableName,
            String interrogationId,
            String expectedValue,
            String expectedScope,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedCollectedVariables =
                surveyUnitModel.getCollectedVariables().stream().filter(variableModel ->
                        variableModel.varId().equals(collectedVariableName)
                                && Objects.equals(variableModel.scope(), expectedScope)
                                && variableModel.iteration().equals(iteration)
                ).toList();
        Assertions.assertThat(concernedCollectedVariables).hasSize(1);

        VariableModel variableModel = concernedCollectedVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("For external variable {string} in survey unit {string} we should have {string} and scope {string} for " +
            "iteration {int}")
    public void check_external_variable_content_in_mongo(
            String externalVariableName,
            String interrogationId,
            String expectedValue,
            String expectedScope,
            Integer iteration
    ) {
        //Get SurveyUnitModel
        List<SurveyUnitModel> concernedSurveyUnitModels = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();
        Assertions.assertThat(concernedSurveyUnitModels).hasSize(1);

        SurveyUnitModel surveyUnitModel = concernedSurveyUnitModels.getFirst();

        //Get Variable
        List<VariableModel> concernedExternalVariables =
                surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                variableModel.varId().equals(externalVariableName)
                        && Objects.equals(variableModel.scope(), expectedScope)
                        && variableModel.iteration().equals(iteration)
        ).toList();
        Assertions.assertThat(concernedExternalVariables).hasSize(1);

        VariableModel variableModel = concernedExternalVariables.getFirst();

        //Value content assertion
        Assertions.assertThat(variableModel.value()).isEqualTo(expectedValue);
    }

    @Then("If we get latest states for {string} in collected variable {string}, survey unit {string} we should have {string} for iteration {int}")
    public void check_latest_state_collected(String questionnaireId, String variableName, String interrogationId, String expectedValue, int iteration) {
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId, questionnaireId).getBody();

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getCollectedVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(variableName)
                        && variableQualityToolDto.getIteration().equals(iteration)
        ).toList();

        Assertions.assertThat(variableQualityToolDtos).hasSize(1);

        List<VariableStateDto> variableStateDtoList =
                variableQualityToolDtos.getFirst().getVariableStateDtoList().stream().filter(
                        variableStateDto -> variableStateDto.getState().equals(DataState.COLLECTED)
                ).toList();
        Assertions.assertThat(variableStateDtoList).hasSize(1);

        Assertions.assertThat(variableStateDtoList.getFirst().getValue()).isEqualTo(expectedValue);
    }

    @Then("If we get latest states for {string} in external variable {string}, survey unit {string} we should have {string} for iteration {int}")
    public void check_latest_state_external(String questionnaireId, String variableName, String interrogationId, String expectedValue, int iteration) {
        SurveyUnitQualityToolDto surveyUnitQualityToolDto = responseController.findResponsesByInterrogationAndQuestionnaireLatestStates(interrogationId, questionnaireId).getBody();

        List<VariableQualityToolDto> variableQualityToolDtos = surveyUnitQualityToolDto.getExternalVariables().stream().filter(
                variableQualityToolDto -> variableQualityToolDto.getVariableName().equals(variableName)
                && variableQualityToolDto.getIteration().equals(iteration)
        ).toList();

        Assertions.assertThat(variableQualityToolDtos).hasSize(1);

        List<VariableStateDto> variableStateDtoList =
                variableQualityToolDtos.getFirst().getVariableStateDtoList().stream().filter(
                        variableStateDto -> variableStateDto.getState().equals(DataState.COLLECTED)
                ).toList();
        Assertions.assertThat(variableStateDtoList).hasSize(1);

        Assertions.assertThat(variableStateDtoList.getFirst().getValue()).isEqualTo(expectedValue);
    }

    @Then("The extracted survey unit data response should have a survey unit model with interrogationId {string}")
    public void check_su_model_interrogationId(String interrogationId) {
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitModelResponse.getBody().getFirst().getInterrogationId()).isEqualTo(interrogationId);
    }

    @Then("The extracted survey unit data response should have a survey unit model for interrogationId {string} with " +
            "{int} collected variables")
    public void check_su_model_collected_variables_volumetry(
            String interrogationId,
            int expectedVolumetry
    ){
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull();

        List<SurveyUnitModel> filteredList = surveyUnitModelResponse.getBody().stream().filter(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(filteredList)
                .isNotNull().hasSize(1);

        Assertions.assertThat(filteredList.getFirst().getCollectedVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit data response should have a survey unit model for interrogationId {string} with " +
            "{int} external variables")
    public void check_su_model_external_variables_volumetry(
            String interrogationId,
            int expectedVolumetry
    ){
        Assertions.assertThat(surveyUnitModelResponse).isNotNull();
        Assertions.assertThat(surveyUnitModelResponse.getBody()).isNotNull();

        List<SurveyUnitModel> filteredList = surveyUnitModelResponse.getBody().stream().filter(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
        ).toList();

        Assertions.assertThat(filteredList)
                .isNotNull().hasSize(1);

        Assertions.assertThat(filteredList.getFirst().getExternalVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit latest states response should have a survey unit DTO has interrogationId " +
            "{string}" +
            " with {int} collected variables")
    public void check_su_latest_states_collected_variables_volumetry(String interrogationId, int expectedVolumetry) {
        Assertions.assertThat(surveyUnitLatestStatesResponse).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody()).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody().getCollectedVariables()).hasSize(expectedVolumetry);
    }

    @Then("The extracted survey unit latest states response should have a survey unit DTO has interrogationId " +
            "{string}" +
            " with {int} external variables")
    public void check_su_latest_states_external_variables_volumetry(String interrogationId, int expectedVolumetry) {
        Assertions.assertThat(surveyUnitLatestStatesResponse).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody()).isNotNull();
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitLatestStatesResponse.getBody().getExternalVariables()).hasSize(expectedVolumetry);
    }

    //AFTERs
    @After
    public void clean() throws IOException {
        //Move from DONE to IN
        Path doneDirectory = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, "DONE");
        if (doneDirectory
                .resolve(inDirectory.getParent().getFileName())
                .resolve(inDirectory.getFileName()).toFile().exists()
        ){
            try (Stream<Path> stream = Files.list(doneDirectory
                    .resolve(inDirectory.getParent().getFileName())
                    .resolve(inDirectory.getFileName())
            )){
                for (Path filePath : stream.filter(path -> !path.toFile().isDirectory()).toList()) {
                    Files.copy(filePath, inDirectory.resolve(filePath.getFileName()),
                            StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }

        //Clean DONE test folder
        org.springframework.util.FileSystemUtils.deleteRecursively(doneDirectory);
    }
}
