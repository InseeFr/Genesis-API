package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.rest.responses.ResponseController;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.utils.AuthUtils;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.rawdata.LunaticXmlRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.service.variabletype.VariableTypeService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.LunaticJsonPersistanceStub;
import fr.insee.genesis.stubs.LunaticXmlPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.VariableTypePersistanceStub;
import io.cucumber.java.After;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
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
import java.util.Optional;

public class MainDefinitions {
    String directory;
    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    final Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);

    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    SurveyUnitPersistencePortStub surveyUnitPersistence = new SurveyUnitPersistencePortStub();
    Config config = new ConfigStub();

    ResponseController responseController = new ResponseController(
            new SurveyUnitService(surveyUnitPersistence),
            surveyUnitQualityService,
            new LunaticXmlRawDataService(new LunaticXmlPersistanceStub()),
            new LunaticJsonRawDataService(new LunaticJsonPersistanceStub()),
            new VariableTypeService(new VariableTypePersistanceStub()),
            new FileUtils(config),
            new ControllerUtils(new FileUtils(config)),
            new AuthUtils(config)
        );

    List<SurveyUnitModel> surveyUnitModels;

    @Before
    public void init() {
        this.surveyUnitPersistence.getMongoStub().clear();
    }

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

    @When("We create DTOs from file {string} with DDI {string}")
    public void get_dtos(String fileName, String ddiName) throws IOException, ParserConfigurationException,
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
                surveyUnitModels1.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), Mode.WEB));
            }
            surveyUnitQualityService.verifySurveyUnits(surveyUnitModels1,variablesMap);
            surveyUnitModels = surveyUnitModels1;
        }
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
        Assertions.assertThat(this.surveyUnitPersistence.getMongoStub().stream().filter(
                surveyUnitModel -> surveyUnitModel.getState().name().equals(expectedStatus)
        ).toList()).hasSize(expectedCount);
    }



    @Then("For SurveyUnit {string} there should be at least one {string} SurveyUnit DTO")
    public void check_expected_datastate_dto(String surveyUnitId, String expectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(expectedDataState)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();
    }

    @Then("For SurveyUnit {string} there shouldn't be a {string} SurveyUnit DTO")
    public void check_unexpected_datastate_dto(String surveyUnitId, String unexpectedDataState) {
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(unexpectedDataState)
                && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isEmpty();
    }

    @Then("We should have a {string} DTO for survey unit {string} with {string} filled with {string} at index {int}")
    public void check_survey_unit_dto_content(String dataState, String surveyUnitId, String variableName, String expectedValue, int expectedIndex) {
        //Get DTO
        Assertions.assertThat(this.surveyUnitModels).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(dataState)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnitModel> concernedSurveyUnitModelOptional = this.surveyUnitModels.stream().filter(dto ->
                dto.getState().toString().equals(dataState)
                && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedSurveyUnitModelOptional).isPresent();

        SurveyUnitModel concernedSurveyUnitModel = concernedSurveyUnitModelOptional.get();

        //Get Variable
        Assertions.assertThat(concernedSurveyUnitModel.getCollectedVariables()).filteredOn(collectedvariableModel ->
                collectedvariableModel.idVar().equals(variableName)).isNotEmpty();

        Optional<VariableModel> concernedCollectedVariableOptional = concernedSurveyUnitModel.getCollectedVariables().stream().filter(variable ->
                variable.idVar().equals(variableName)
        ).findFirst();

        Assertions.assertThat(concernedCollectedVariableOptional).isPresent();

        VariableModel concernedCollectedVariable = concernedCollectedVariableOptional.get();

        //Value assertion
        Assertions.assertThat(concernedCollectedVariable.values()).hasSizeGreaterThan(expectedIndex);

        Assertions.assertThat(concernedCollectedVariable.values().get(expectedIndex)).isEqualTo(expectedValue);
    }

    @Then("We should have {int} values for external variable {string} for survey unit {string}")
    public void external_variable_volumetric_check(int expectedNumberOfValues, String externalVariableName,
                                                   String interrogationId) {
        //Get SurveyUnitModel
        Assertions.assertThat(surveyUnitPersistence.getMongoStub()).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getIdUE().equals(interrogationId)
        ).isNotEmpty().hasSize(1);

        SurveyUnitModel surveyUnitModel = surveyUnitPersistence.getMongoStub().stream().filter(surveyUnitModel1 ->
                surveyUnitModel1.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel1.getIdUE().equals(interrogationId)
        ).toList().getFirst();

        //Get Variable
        Assertions.assertThat(surveyUnitModel.getExternalVariables()).filteredOn(variableModel ->
                variableModel.idVar().equals(externalVariableName)).isNotEmpty();

        Optional<VariableModel> concernedVariableOptional = surveyUnitModel.getExternalVariables().stream().filter(variable ->
                variable.idVar().equals(externalVariableName)
        ).findFirst();

        Assertions.assertThat(concernedVariableOptional).isPresent();

        VariableModel concernedVariable = concernedVariableOptional.get();

        //Values count assertion
        Assertions.assertThat(concernedVariable.values()).hasSize(expectedNumberOfValues);

    }

    @And("For external variable {string} in survey unit {string} we should have {string} and loopId {string}")
    public void forExternalVariableInSurveyUnitWeShouldHaveForLoop(String externalVariableName,
                                                                   String interrogationId,
                                                                   String expectedValue,
                                                                   String expectedLoopId) {
        //Get SurveyUnitModel
        Assertions.assertThat(surveyUnitPersistence.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().equals(DataState.COLLECTED)
                        && surveyUnitDto.getIdUE().equals(interrogationId)
        ).isNotEmpty().hasSize(1);

        SurveyUnitModel surveyUnitModel = surveyUnitPersistence.getMongoStub().stream().filter(dto ->
                dto.getState().equals(DataState.COLLECTED)
                        && dto.getIdUE().equals(interrogationId)
        ).toList().getFirst();

        //Get Variable
        Assertions.assertThat(surveyUnitModel.getExternalVariables()).filteredOn(variableModel ->
                variableModel.idVar().equals(externalVariableName)
                        && Objects.equals(variableModel.idLoop(), expectedLoopId)
        ).isNotEmpty().hasSize(1);

        VariableModel concernedExternalVariable = surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                variableModel.idVar().equals(externalVariableName)
                        && Objects.equals(variableModel.idLoop(), expectedLoopId)
        ).toList().getFirst();

        //Value content assertion
        Assertions.assertThat(concernedExternalVariable.values()).hasSize(1);
        Assertions.assertThat(concernedExternalVariable.values().getFirst()).isEqualTo(expectedValue);
        Assertions.assertThat(concernedExternalVariable.idLoop()).isNotNull().isEqualTo(expectedLoopId);
    }

    @Then("For external variable {string} in survey unit {string} we should have {string} as idLoop and {string} as first " +
            "value")
    public void check_idLoop_and_value(String externalVariableName, String interrogationId, String expectedLoopId,
                             String expectedValue) {
        //Get SurveyUnitModel
        Assertions.assertThat(surveyUnitPersistence.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().equals(DataState.COLLECTED)
                        && surveyUnitDto.getIdUE().equals(interrogationId)
        ).isNotEmpty().hasSize(1);

        SurveyUnitModel surveyUnitModel = surveyUnitPersistence.getMongoStub().stream().filter(dto ->
                dto.getState().equals(DataState.COLLECTED)
                        && dto.getIdUE().equals(interrogationId)
        ).toList().getFirst();

        //Get Variable
        Assertions.assertThat(surveyUnitModel.getExternalVariables()).filteredOn(variableModel ->
                variableModel.idVar().equals(externalVariableName)
                && Objects.equals(variableModel.idLoop(), expectedLoopId)
        ).isNotEmpty().hasSize(1);

        VariableModel concernedExternalVariable = surveyUnitModel.getExternalVariables().stream().filter(variableModel ->
                        variableModel.idVar().equals(externalVariableName)
                        && Objects.equals(variableModel.idLoop(), expectedLoopId)
                ).toList().getFirst();

        //Value content assertion
        Assertions.assertThat(concernedExternalVariable.idLoop()).isNotNull().isEqualTo(expectedLoopId);
        Assertions.assertThat(concernedExternalVariable.values()).hasSize(1);
        Assertions.assertThat(concernedExternalVariable.values().getFirst()).isEqualTo(expectedValue);
    }

    @After
    public void clean() throws IOException {
        //Clean DONE test folder
        org.springframework.util.FileSystemUtils.deleteRecursively(Path.of(TestConstants.TEST_RESOURCES_DIRECTORY,
                "DONE"));
    }
}
