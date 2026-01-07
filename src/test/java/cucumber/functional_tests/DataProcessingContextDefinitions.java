package cucumber.functional_tests;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.controller.rest.DataProcessingContextController;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import io.cucumber.java.Before;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.assertj.core.api.Assertions;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class DataProcessingContextDefinitions {
    private DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;
    private DataProcessingContextController dataProcessingContextController;
    private DataProcessingContextDocument fetchedDocument;
    private ResponseEntity<Object> response;

    @Before
    public void init() {
        fetchedDocument = null;
        response = null;
        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
        dataProcessingContextController = new DataProcessingContextController(
                new DataProcessingContextService(
                        dataProcessingContextPersistancePortStub,
                        new SurveyUnitPersistencePortStub()
                        ),
                new FileUtils(new ConfigStub())
        );
    }

    @Given("We have a context in database with partition {string}")
    public void add_context_to_database(String partitionId) {
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setPartitionId(partitionId);
        doc.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc.setWithReview(false);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc);
    }


    @Given("We have a context in database with partition {string} and review indicator to {string}")
    public void add_context_with_review_indicator(String partitionId, String withReviewString) {
        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setPartitionId(partitionId);
        doc.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc.setWithReview(Boolean.parseBoolean(withReviewString));
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc);
    }

    @Given("We have a context in database with partition {string} and {int} valid schedule\\(s)")
    public void add_context_with_schedule(String partitionId, int expectedScheduleNumber) {
        DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
        dataProcessingContextDocument.setPartitionId(partitionId);
        dataProcessingContextDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        dataProcessingContextDocument.setWithReview(false);

        for(int i = 0; i < expectedScheduleNumber; i++){
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(
                    new KraftwerkExecutionSchedule(
                            "0 0 1 * * *",
                            getRandomServiceToCall(),
                            LocalDateTime.MIN,
                            LocalDateTime.now().plusDays(1),
                            null
                    )
            );
        }

        dataProcessingContextPersistancePortStub.getMongoStub().add(dataProcessingContextDocument);
    }

    @Given("We have a context in database with partition {string} and {int} expired schedule\\(s)")
    public void add_context_with_expired_schedule(String partitionId, int expectedScheduleNumber) {
        DataProcessingContextDocument dataProcessingContextDocument = new DataProcessingContextDocument();
        dataProcessingContextDocument.setPartitionId(partitionId);
        dataProcessingContextDocument.setKraftwerkExecutionScheduleList(new ArrayList<>());
        dataProcessingContextDocument.setWithReview(false);

        for(int i = 0; i < expectedScheduleNumber; i++){
            dataProcessingContextDocument.getKraftwerkExecutionScheduleList().add(
                    new KraftwerkExecutionSchedule(
                            "0 0 1 * * *",
                            getRandomServiceToCall(),
                            LocalDateTime.MIN,
                            LocalDateTime.now().minusDays(1),
                            null
                    )
            );
        }

        dataProcessingContextPersistancePortStub.getMongoStub().add(dataProcessingContextDocument);
    }

    @When("We save data processing context for partition {string}")
    public void save_context(String partitionId){
        response = dataProcessingContextController.saveContext(partitionId, null);
    }

    @When("We save data processing context for partition {string} and review indicator to {string}")
    public void save_context_with_review_indicator(String partitionId, String withReviewString) {
        response = dataProcessingContextController.saveContext(partitionId, Boolean.parseBoolean(withReviewString));
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
                serviceToCall,
                frequency,
                startDate,
                endDate,
                false,
                null,
                null,
                false
        );
    }

    @When("We delete the schedules of {string}")
    public void delete_schedules(String partitionId){
        response = dataProcessingContextController.deleteSchedules(partitionId);
    }

    @When("We get all the schedules")
    public void get_all_schedules() {
        response = dataProcessingContextController.getAllSchedules();
    }

    @When("We delete the expired schedules")
    public void delete_expired_schedules() throws GenesisException, IOException {
        dataProcessingContextController.deleteExpiredSchedules();
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

    @Then("The context controller response should have a {int} status code")
    public void check_response_status(int expectedStatusCode) {
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(expectedStatusCode);
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

    @Then("The context of {string} should have {int} schedules")
    public void check_schedule_volumetry(String partitionId, int expectedScheduleVolumetry) {
        List<DataProcessingContextDocument> dataProcessingContextDocuments = dataProcessingContextPersistancePortStub.getMongoStub()
                .stream().filter(dataProcessingContextDocument -> dataProcessingContextDocument.getPartitionId().equals(partitionId))
                .toList();
        Assertions.assertThat(dataProcessingContextDocuments).hasSize(1);
        Assertions.assertThat(dataProcessingContextDocuments.getFirst().getKraftwerkExecutionScheduleList()).hasSize(expectedScheduleVolumetry);
    }

    @Then("The get all schedules should have {int} schedules, {int} per partition")
    @SuppressWarnings("unchecked")
    public void check_schedules_per_partition(int expectedTotalSchedules, int expectedSchedulesPerPartition) {
        List<ScheduleDto> scheduleDtos = (List<ScheduleDto>) response.getBody();

        Assertions.assertThat(scheduleDtos).hasSize(expectedTotalSchedules);
        for(ScheduleDto scheduleDto : scheduleDtos){
            Assertions.assertThat(scheduleDto.kraftwerkExecutionScheduleList()).hasSize(expectedSchedulesPerPartition);
        }
    }

    @Then("The get all schedules should have {int} schedules and no schedule for {string}")
    @SuppressWarnings("unchecked")
    public void check_schedules_with_absence(int expectedTotalSchedules, String expectedNoSchedulePartitionId) {
        List<ScheduleDto> scheduleDtos = (List<ScheduleDto>) response.getBody();

        Assertions.assertThat(scheduleDtos).hasSize(expectedTotalSchedules);
        for(ScheduleDto scheduleDto : scheduleDtos){
            Assertions.assertThat(
                    scheduleDto.surveyName().equals(expectedNoSchedulePartitionId)
                    && !scheduleDto.kraftwerkExecutionScheduleList().isEmpty()
            ).isFalse();
        }
    }

    private ServiceToCall getRandomServiceToCall() {
        return ServiceToCall.values()[new Random().nextInt(ServiceToCall.values().length)];
    }
}
