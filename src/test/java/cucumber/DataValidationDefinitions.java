package cucumber;

import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateMongoDBRepository;
import io.cucumber.java.BeforeAll;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;


import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@ContextConfiguration(classes = CucumberSpringConfiguration.class)
public class DataValidationDefinitions {

    @Autowired
    static SurveyUnitUpdateMongoDBRepository repository;

    Path inDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_WEB_DIRECTORY);
    Path ddiDirectory = Paths.get(TestConstants.FUNCTIONAL_TESTS_DDI_DIRECTORY);
    final static String idQuestionnaire = "TestValidation1";

    @BeforeAll
    public static void clean(){
        assertThat(repository).isNotNull();
        repository.deleteByidQuestionnaire("TestValidation1");
    }

    //TODO Move the two methods to MainDefinitions to create generic definitions
    @Given("We have data in directory {string}")
    public void init(String directory){
        inDirectory = inDirectory.resolve(directory);
    }

    @When("We save responses from the file {string} with DDI {string}")
    public void save_responses_from_file(String fileName, String DDIName) {
        Path filePath = inDirectory.resolve(fileName);
        Path ddiFilePath = ddiDirectory.resolve(DDIName);
        String url = TestConstants.FUNCTIONAL_TESTS_API_URL + "/response/save/lunatic-xml/one-file";

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(url)
                .queryParam("pathLunaticXml", filePath)
                .queryParam("DDI", ddiFilePath);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<?> request = new HttpEntity<>(headers);

        RestTemplate restTemplate = new RestTemplate();

        ResponseEntity<String> response = restTemplate.exchange(builder.toUriString(), HttpMethod.PUT, request, String.class);

        assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }


    @Then("For SurveyUnit {string} there shouldn't be a FORCED copy of the SurveyUnit")
    public void check_no_forced(String surveyUnitId) {
        assertThat(repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire))
                .filteredOn(
                        surveyUnitUpdateDocument -> surveyUnitUpdateDocument.getState().equals(DataState.FORCED.toString())
                ).isEmpty();
    }

    @Then("There is a FORCED copy of SurveyUnit {string} without update variable {string}")
    public void check_forced_update_variable_removed(String surveyUnitId, String variableName){
        assertThat(repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire))
                .filteredOn(surveyUnitUpdateDocument ->
                        surveyUnitUpdateDocument.getState().equals(DataState.FORCED.toString())
                ).isNotEmpty();

        SurveyUnitUpdateDocument forcedSurveyUnitDocument = repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire).get(0);
        assertThat(forcedSurveyUnitDocument.getVariablesUpdate())
                .filteredOn(variable -> variable.getIdVar().equals(variableName))
                .isEmpty();
    }

    @Then("There is a FORCED copy of SurveyUnit {string} without external variable {string}")
    public void check_forced_external_variable_removed(String surveyUnitId, String variableName) {
        assertThat(repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire))
                .filteredOn(surveyUnitUpdateDocument ->
                        surveyUnitUpdateDocument.getState().equals(DataState.FORCED.toString())
                ).isNotEmpty();

        SurveyUnitUpdateDocument forcedSurveyUnitDocument = repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire).get(0);
        assertThat(forcedSurveyUnitDocument.getExternalVariables())
                .filteredOn(variable -> variable.getIdVar().equals(variableName))
                .isEmpty();
    }

    @Then("There is a FORCED copy of SurveyUnit {string} with update variable {string} containing {int} empty values")
    public void check_forced_update_variable_corrected(String surveyUnitId, String variableName, int expectedEmptyValuesCount){
        assertThat(repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire))
            .filteredOn(surveyUnitUpdateDocument ->
                    surveyUnitUpdateDocument.getState().equals(DataState.FORCED.toString())
            ).isNotEmpty();

        SurveyUnitUpdateDocument forcedSurveyUnitDocument = repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire).get(0);
        assertThat(forcedSurveyUnitDocument.getVariablesUpdate())
                .filteredOn(variable -> variable.getIdVar().equals(variableName))
                .isNotEmpty();

        forcedSurveyUnitDocument.getVariablesUpdate().removeIf(variable -> !variable.getIdVar().equals(variableName));
        VariableState concernedVariable = forcedSurveyUnitDocument.getVariablesUpdate().get(0);

        int actualEmptyValuesCount = 0;
        for(String value : concernedVariable.getValues()){
            if(value.isEmpty()) actualEmptyValuesCount++;
        }

        assertThat(actualEmptyValuesCount).isEqualTo(expectedEmptyValuesCount);
    }

    @Then("There is a FORCED copy of SurveyUnit {string} with external variable {string} containing {int} empty values")
    public void check_forced_external_variable_corrected(String surveyUnitId, String variableName, int expectedEmptyValuesCount){
        assertThat(repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire))
                .filteredOn(surveyUnitUpdateDocument ->
                        surveyUnitUpdateDocument.getState().equals(DataState.FORCED.toString())
                ).isNotEmpty();

        SurveyUnitUpdateDocument forcedSurveyUnitDocument = repository.findByIdUEAndIdQuestionnaire(surveyUnitId,idQuestionnaire).get(0);
        assertThat(forcedSurveyUnitDocument.getExternalVariables())
                .filteredOn(variable -> variable.getIdVar().equals(variableName))
                .isNotEmpty();

        forcedSurveyUnitDocument.getExternalVariables().removeIf(variable -> !variable.getIdVar().equals(variableName));
        ExternalVariable concernedVariable = forcedSurveyUnitDocument.getExternalVariables().get(0);

        int actualEmptyValuesCount = 0;
        for(String value : concernedVariable.getValues()){
            if(value.isEmpty()) actualEmptyValuesCount++;
        }

        assertThat(actualEmptyValuesCount).isEqualTo(expectedEmptyValuesCount);
    }
}
