package integration_tests;

import fr.insee.genesis.domain.model.rundeck.DateStarted;
import fr.insee.genesis.domain.model.rundeck.Job;
import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.service.rundeck.RundeckExecutionService;
import integration_tests.stubs.RundeckExecutionPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class RundeckExecutionServiceTest {

    static RundeckExecutionPersistencePortStub rundeckPersistencePortStub;

    static RundeckExecutionService rundeckExecutionService;

    @BeforeAll
    static void init(){
        rundeckPersistencePortStub = new RundeckExecutionPersistencePortStub();
        rundeckExecutionService = new RundeckExecutionService(rundeckPersistencePortStub);
    }

    @Test
    void addRundeckExecution_test()  {
        //GIVEN
        DateStarted dateStarted = new DateStarted();
        dateStarted.setUnixtime(1737643070029L);
        dateStarted.setDate("2025-01-23T14:37:50Z");

        Job job = new Job();
        job.setIdJob("9506a45d-79e3-42d9-afb4-c8f59652c676");
        job.setAverageDuration(8817);
        job.setName("TEST");
        job.setGroup("");
        job.setProject("project-test");
        job.setDescription("job de test qui attend 8 s et retourne OK");
        job.setHref("https://example-url");
        job.setPermalink("https://example-url");

        // Création de l'objet RundeckExecution
        RundeckExecution execution = new RundeckExecution();
        execution.setIdExecution(1747563);
        execution.setHref("https://example-url");
        execution.setPermalink("https://example-url");
        execution.setStatus("running");
        execution.setProject("project-test");
        execution.setExecutionType("user");
        execution.setUser("project-test");
        execution.setDateStarted(dateStarted);
        execution.setJob(job);
        execution.setDescription("A very usefull description");
        execution.setArgstring(null);
        execution.setServerUUID("e9ff62c2-b621-4b31-827e-9d47f0c34310");

        //WHEN
        rundeckExecutionService.addExecution(execution);

        //THEN
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getIdExecution()).isEqualTo(1747563);
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getStatus()).isEqualTo("running");

        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getUser()).isEqualTo("project-test");
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getProject()).isEqualTo("project-test");
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getDateStarted().getUnixtime()).isEqualTo(1737643070029L);
        Assertions.assertThat(rundeckPersistencePortStub.getMongoStub().getFirst().getJob().getIdJob()).isEqualTo("9506a45d-79e3-42d9-afb4-c8f59652c676");

    }


}