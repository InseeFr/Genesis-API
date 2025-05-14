package cucumber.functional_tests;

import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;

public class DataProcessingContextDefinitions {

    private ResponseEntity<Object> saveResponse;

    @Before
    public void init() {
        //TODO init database stub
    }

    @Given("We have a context in database with partition {string}")
    public void add_context_to_database(String partitionId) {
        //TODO
    }


    @Given("We have a context in database with partition {string} and review indicator to {string}")
    public void add_context_with_review_indicator(String partitionId, String withReviewString) {
        boolean withReview = Boolean.parseBoolean(withReviewString);
        //TODO
    }

    @When("We save data processing context for partition {string}")
    public void save_context(String partitionId){
        saveResponse = TODO;
    }

    @When("We set withReview to {string}")
    public void set_review_indicator(String withReviewString) {
        boolean withReview = Boolean.parseBoolean(withReviewString);
        //TODO
    }

    @When("We save a new kraftwerk schedule for partition {string}, frequency {string} and service to call {string}, with beginning date at {string} and ending at {string}")
    public void save_kraftwerk_schedule(String partitionId,
                                        String frequency,
                                        String serviceToCallString,
                                        String startDateString,
                                        String endDateString) {
        ServiceToCall serviceToCall = ServiceToCall.valueOf(serviceToCallString);
        LocalDateTime startDate = LocalDateTime.parse(startDateString);
        LocalDateTime endDate = LocalDateTime.parse(endDateString);
        //TODO
    }


    @Then("We should have a context document for partition {string}")
    public void check_context(String partitionId) {
        //TODO
    }

    @Then("Review indicator should be {string}")
    public void check_review_indicator(String expectedReviewIndicator){
        Assertions.assertThat(Boolean.parseBoolean(TODO)).isEqualTo(expectedReviewIndicator);
    }

    @Then("Save data processing response should have a {int} status code")
    public void check_save_status_code(int expectedStatusCode) {
        Assertions.assertThat(saveResponse.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    @Then("We should have a context document for partition {string} containing a kraftwerk schedule with frequency {string} and service to call {string}, with beginning date at {string} and ending at {string}")
    public void check_kraftwerk_schedule(String partitionId,
                                         String expectedFrequency,
                                         String expectedServiceToCall,
                                         String expectedStartDate,
                                         String expectedEndDate) {
        //TODO
    }
}
