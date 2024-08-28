package cucumber.functional_tests;

import cucumber.TestConstants;
import fr.insee.bpm.exceptions.MetadataParserException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.ddi.DDIReader;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.controller.sources.xml.LunaticXmlDataParser;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.exceptions.GenesisException;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import javax.xml.parsers.ParserConfigurationException;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class MainDefinitions {
    String directory;
    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    final Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);

    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();

    List<SurveyUnit> surveyUnits;

    @Given("We have data in directory {string}")
    public void init(String directory){
        this.directory = directory;
        inDirectory = inDirectory.resolve(directory);
    }

    @When("We create DTOs from file {string} with DDI {string}")
    public void get_dtos(String fileName, String DDIName) throws IOException, ParserConfigurationException, SAXException, GenesisException, MetadataParserException {
        Path filePath = inDirectory.resolve(fileName);
        Path ddiFilePath = ddiDirectory.resolve(directory).resolve(DDIName);

        if(fileName.endsWith(".xml")){
            LunaticXmlDataParser parser = new LunaticXmlDataParser();
            LunaticXmlCampaign campaign;
            campaign = parser.parseDataFile(filePath);
            VariablesMap variablesMap = DDIReader.getMetadataFromDDI(
                    ddiFilePath.toFile().toURI().toURL().toString(),
                    new FileInputStream(ddiFilePath.toFile())
            ).getVariables();
            List<SurveyUnit> suDtos = new ArrayList<>();
            for (LunaticXmlSurveyUnit su : campaign.getSurveyUnits()) {
                suDtos.addAll(LunaticXmlAdapter.convert(su, variablesMap, campaign.getIdCampaign(), Mode.WEB));
            }
            surveyUnitQualityService.verifySurveyUnits(suDtos,variablesMap);
            surveyUnits = suDtos;
        }
    }

    @Then("For SurveyUnit {string} there should be at least one {string} SurveyUnit DTO")
    public void check_expected_datastate_dto(String surveyUnitId, String expectedDataState) {
        Assertions.assertThat(this.surveyUnits).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(expectedDataState)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();
    }

    @Then("For SurveyUnit {string} there shouldn't be a {string} SurveyUnit DTO")
    public void check_unexpected_datastate_dto(String surveyUnitId, String UnexpectedDataState) {
        Assertions.assertThat(this.surveyUnits).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(UnexpectedDataState)
                && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isEmpty();
    }

    @Then("We should have a {string} DTO for survey unit {string} with {string} filled with {string} at index {int}")
    public void check_survey_unit_dto_content(String dataState, String surveyUnitId, String variableName, String expectedValue, int expectedIndex) {
        //Get DTO
        Assertions.assertThat(this.surveyUnits).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().toString().equals(dataState)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnit> concernedDtoOptional = this.surveyUnits.stream().filter(dto ->
                dto.getState().toString().equals(dataState)
                && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedDtoOptional).isPresent();

        SurveyUnit concernedDto = concernedDtoOptional.get();

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
        Assertions.assertThat(this.surveyUnits).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().equals(DataState.COLLECTED)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnit> concernedDtoOptional = this.surveyUnits.stream().filter(dto ->
                dto.getState().equals(DataState.COLLECTED)
                        && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedDtoOptional).isPresent();

        SurveyUnit concernedDto = concernedDtoOptional.get();

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
        Assertions.assertThat(this.surveyUnits).filteredOn(surveyUnitDto ->
                surveyUnitDto.getState().equals(DataState.COLLECTED)
                        && surveyUnitDto.getIdUE().equals(surveyUnitId)
        ).isNotEmpty();

        Optional<SurveyUnit> concernedDtoOptional = this.surveyUnits.stream().filter(dto ->
                dto.getState().equals(DataState.COLLECTED)
                        && dto.getIdUE().equals(surveyUnitId)
        ).findFirst();

        Assertions.assertThat(concernedDtoOptional).isPresent();

        SurveyUnit concernedDto = concernedDtoOptional.get();

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
}
