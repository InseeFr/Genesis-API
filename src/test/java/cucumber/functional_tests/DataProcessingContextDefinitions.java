package cucumber.functional_tests;

import fr.insee.genesis.controller.rest.responses.DataProcessingContextController;
import fr.insee.genesis.domain.model.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class DataProcessingContextDefinitions {
    private DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;
    private DataProcessingContextController dataProcessingContextController;
    private DataProcessingContextDocument fetchedDocument;
    private ResponseEntity<Object> saveResponse;

    @Before
    public void init() {
        fetchedDocument = null;
        saveResponse = null;
        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
        dataProcessingContextController = new DataProcessingContextController(
                new DataProcessingContextService(dataProcessingContextPersistancePortStub),
                new FileUtils(new ConfigStub())
        );
    }

    @Given("We have a context in database with partition {string}")
    public void add_context_to_database(String partitionId) {
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                new DataProcessingContextDocument(partitionId, new ArrayList<>(), false)
        );
    }


    @Given("We have a context in database with partition {string} and review indicator to {string}")
    public void add_context_with_review_indicator(String partitionId, String withReviewString) {
        boolean withReview = Boolean.parseBoolean(withReviewString);
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                new DataProcessingContextDocument(partitionId, new ArrayList<>(), withReview)
        );
    }

    @When("We save data processing context for partition {string}")
    public void save_context(String partitionId){
        saveResponse = dataProcessingContextController.saveContext(partitionId, null);
    }

    @When("We save data processing context for partition {string} and review indicator to {string}")
    public void save_context_with_review_indicator(String partitionId, String withReviewString) {
        saveResponse = dataProcessingContextController.saveContext(partitionId, Boolean.parseBoolean(withReviewString));
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
        dataProcessingContextController.saveSchedule(partitionId,
                frequency,
                serviceToCall,
                startDate,
                endDate,
                false,
                null,
                null,
                false
        );
    }


    @Then("We should have one context document for partition {string}")
    public void check_context(String partitionId) {
        List<DataProcessingContextDocument> documentsOfPartition =
                dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(
                document -> document.getPartitionId().equals(partitionId)
        ).toList();
        Assertions.assertThat(documentsOfPartition).hasSize(1);
        fetchedDocument = documentsOfPartition.getFirst();
    }

    @Then("Review indicator should be {string}")
    public void check_review_indicator(String expectedReviewIndicatorString){
        boolean expectedReviewIndicator = Boolean.parseBoolean(expectedReviewIndicatorString);
        Assertions.assertThat(fetchedDocument).isNotNull();
        Assertions.assertThat(fetchedDocument.isWithReview()).isEqualTo(expectedReviewIndicator);
    }

    @Then("Save data processing response should have a {int} status code")
    public void check_save_status_code(int expectedStatusCode) {
        Assertions.assertThat(saveResponse.getStatusCode().value()).isEqualTo(expectedStatusCode);
    }

    @Then("We should have a context document for partition {string} containing a kraftwerk schedule with frequency {string} and service to call {string}, with beginning date at {string} and ending at {string}")
    public void check_kraftwerk_schedule(String partitionId,
                                         String expectedFrequency,
                                         String expectedServiceToCallString,
                                         String expectedStartDateString,
                                         String expectedEndDateString) {
        List<DataProcessingContextDocument> documentsOfPartition =
                dataProcessingContextPersistancePortStub.getMongoStub().stream().filter(
                        document -> document.getPartitionId().equals(partitionId)
                ).toList();
        Assertions.assertThat(documentsOfPartition).hasSize(1);

        DataProcessingContextDocument dataProcessingContextDocument = documentsOfPartition.getFirst();
        Assertions.assertThat(dataProcessingContextDocument.getKraftwerkExecutionScheduleList()).hasSize(1);

        KraftwerkExecutionSchedule kraftwerkExecutionSchedule =
                dataProcessingContextDocument.getKraftwerkExecutionScheduleList().getFirst();
        Assertions.assertThat(kraftwerkExecutionSchedule.getFrequency()).isEqualTo(expectedFrequency);
        Assertions.assertThat(kraftwerkExecutionSchedule.getServiceToCall()).isEqualTo(ServiceToCall.valueOf(expectedServiceToCallString));
        Assertions.assertThat(kraftwerkExecutionSchedule.getScheduleBeginDate()).isEqualTo(LocalDateTime.parse(expectedStartDateString));
        Assertions.assertThat(kraftwerkExecutionSchedule.getScheduleEndDate()).isEqualTo(LocalDateTime.parse(expectedEndDateString));
    }
}
