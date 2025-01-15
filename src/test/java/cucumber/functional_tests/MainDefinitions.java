package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.rest.responses.ResponseController;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.rawdata.LunaticXmlRawDataService;
import fr.insee.genesis.domain.service.surveymetadata.SurveyMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.LunaticJsonPersistanceStub;
import fr.insee.genesis.stubs.LunaticXmlPersistanceStub;
import fr.insee.genesis.stubs.SurveyMetadataPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Optional;

public class MainDefinitions {
    String directory;
    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    final Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);

    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    SurveyMetadataPersistanceStub surveyMetadataPersistanceStub = new SurveyMetadataPersistanceStub();
    Config config = new ConfigStub();
    SurveyMetadataService surveyMetadataService = new SurveyMetadataService(surveyMetadataPersistanceStub);


    ResponseController responseController = new ResponseController(
            new SurveyUnitService(surveyUnitPersistencePortStub),
            surveyUnitQualityService,
            new LunaticXmlRawDataService(new LunaticXmlPersistanceStub()),
            new LunaticJsonRawDataService(new LunaticJsonPersistanceStub()),
            surveyMetadataService,
            new FileUtils(config),
            new ControllerUtils(new FileUtils(config))
        );

    @Before
    public void init() {
        this.surveyUnitPersistencePortStub.getMongoStub().clear();
    }

    @Given("We have data in directory {string}")
    public void init_folder(String directory) throws IOException {
        this.directory = directory;
        inDirectory = inDirectory.resolve(directory);
        Files.createDirectories(inDirectory);
    }

    @When("We create DTOs from file {string} with DDI {string}")
    public void get_dtos(String fileName, String ddiName) throws Exception {
        Path filePath = inDirectory.resolve(fileName);
        Path ddiFilePath = ddiDirectory.resolve(directory).resolve(ddiName);

        if(fileName.endsWith(".xml")){
            responseController.saveResponsesFromXmlFile(filePath.toString(), ddiFilePath.toString(), null, true);
        }
    }

    @When("We copy data file {string} to that directory")
    public void copy_data_file(String dataFile) throws IOException {
        Path dataFilePath = Paths.get(TestConstants.TEST_RESOURCES_DIRECTORY).resolve(dataFile);
        Files.copy(dataFilePath, this.inDirectory.resolve(dataFilePath.getFileName().toString()),
                StandardCopyOption.REPLACE_EXISTING);
    }

    @When("We save data from that directory")
    public void get_dtos_from_folder() throws Exception {
        responseController.saveResponsesFromXmlCampaignFolder(this.inDirectory.getFileName().toString(), null, true);
    }

    @When("We delete that directory")
    public void delete_directory() throws IOException {
        org.springframework.util.FileSystemUtils.deleteRecursively(inDirectory);
    }

    @Then("There should be {int} {string} SurveyUnit in database")
    public void check_surveyunits_by_state(int expectedCount, String expectedStatus) {
        Assertions.assertThat(this.surveyUnitPersistencePortStub.getMongoStub().stream().filter(
                surveyUnitModel -> surveyUnitModel.getState().name().equals(expectedStatus)
        ).toList()).hasSize(expectedCount);
    }



    @Then("For SurveyUnit {string} there should be at least one {string} SurveyUnit DTO")
    public void check_expected_datastate_dto(String surveyUnitId, String expectedDataState) {
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().toString().equals(expectedDataState)
                        && surveyUnitModel.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();
    }

    @Then("For SurveyUnit {string} there shouldn't be a {string} SurveyUnit DTO")
    public void check_unexpected_datastate_dto(String surveyUnitId, String unexpectedDataState) {
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(unexpectedDataState)
                && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isEmpty();
    }

    @Then("We should have a {string} DTO for survey unit {string} with {string} filled with {string} at index {int}")
    public void check_survey_unit_dto_content(String dataState, String surveyUnitId, String variableName, String expectedValue, int expectedIndex) {
        //Get DTO
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(dataState)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnitModel> concernedDtoOptional = surveyUnitPersistencePortStub.getMongoStub().stream().filter(dto ->
                dto.getState().toString().equals(dataState)
                && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedDtoOptional).isPresent();

        SurveyUnitModel concernedDto = concernedDtoOptional.get();

        //Get Variable
        Assertions.assertThat(concernedDto.getCollectedVariables()).filteredOn(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals(variableName)).isNotEmpty();

        Optional<CollectedVariable> concernedVariableOptional = concernedDto.getCollectedVariables().stream().filter(variable ->
                variable.getIdVar().equals(variableName)
        ).findFirst();

        Assertions.assertThat(concernedVariableOptional).isPresent();

        CollectedVariable concernedVariable = concernedVariableOptional.get();

        //Value assertion
        Assertions.assertThat(concernedVariable.getValues()).hasSizeGreaterThan(expectedIndex);

        Assertions.assertThat(concernedVariable.getValues().get(expectedIndex)).isEqualTo(expectedValue);
    }

    @Then("We should have {int} values for external variable {string} for survey unit {string}")
    public void external_variable_volumetric_check(int expectedNumberOfValues, String externalVariableName, String surveyUnitId) {
        //Get DTO
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnitModel> optionalSurveyUnitModel = surveyUnitPersistencePortStub.getMongoStub().stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(optionalSurveyUnitModel).isPresent();

        SurveyUnitModel concernedDto = optionalSurveyUnitModel.get();

        //Get Variable
        Assertions.assertThat(concernedDto.getExternalVariables()).filteredOn(variableDto ->
                variableDto.getIdVar().equals(externalVariableName)).isNotEmpty();

        Optional<Variable> concernedVariableOptional = concernedDto.getExternalVariables().stream().filter(variable ->
                variable.getIdVar().equals(externalVariableName)
        ).findFirst();

        Assertions.assertThat(concernedVariableOptional).isPresent();

        Variable concernedVariable = concernedVariableOptional.get();

        //Values count assertion
        Assertions.assertThat(concernedVariable.getValues()).hasSize(expectedNumberOfValues);

    }

    @Then("For external variable {string} in survey unit {string} we should have {string} as value number {int}")
    public void external_variable_content_check(String externalVariableName, String surveyUnitId, String expectedValue, int expectedValueIndex) {
        //Get DTO
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().equals(DataState.COLLECTED)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnitModel> concernedDtoOptional = surveyUnitPersistencePortStub.getMongoStub().stream().filter(dto ->
                dto.getState().equals(DataState.COLLECTED)
                        && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedDtoOptional).isPresent();

        SurveyUnitModel concernedDto = concernedDtoOptional.get();

        //Get Variable
        Assertions.assertThat(concernedDto.getExternalVariables()).filteredOn(variableDto ->
                variableDto.getIdVar().equals(externalVariableName)).isNotEmpty();

        Optional<Variable> concernedVariableOptional = concernedDto.getExternalVariables().stream().filter(variable ->
                variable.getIdVar().equals(externalVariableName)
        ).findFirst();

        Assertions.assertThat(concernedVariableOptional).isPresent();

        Variable concernedVariable = concernedVariableOptional.get();

        //Value content assertion
        Assertions.assertThat(concernedVariable.getValues()).hasSizeGreaterThan(expectedValueIndex);
        Assertions.assertThat(concernedVariable.getValues().get(expectedValueIndex)).isEqualTo(expectedValue);
    }

    @After
    public void clean() throws IOException {
        //Clean DONE test folder
        org.springframework.util.FileSystemUtils.deleteRecursively(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,
                "DONE"));
    }
}
