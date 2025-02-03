package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.rundeck.Job;
import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.stubs.RundeckExecutionApiPortStub;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RundeckExecutionControllerTest {

    private static RundeckExecutionController rundeckExecutionController;
    private static RundeckExecutionApiPortStub rundeckExecutionApiPortStub;

    @BeforeAll
    static void init(){
        rundeckExecutionApiPortStub = new RundeckExecutionApiPortStub();
        rundeckExecutionController = new RundeckExecutionController(rundeckExecutionApiPortStub);
    }

    @Test
    void testAddRundeckExecution_Success() {

        RundeckExecution rundeckExecution = new RundeckExecution();
        Job job = new Job();
        job.setName("TEST");
        rundeckExecution.setJob(job);

        // WHEN
        ResponseEntity<Object> response = rundeckExecutionController.addRundeckExecution(rundeckExecution);

        // THEN
        assertEquals(ResponseEntity.ok().build(), response);
    }

    @Test
    void testAddRundeckExecution_Failure() {
        // GIVEN
        rundeckExecutionApiPortStub.setShouldThrowException(true);

        RundeckExecution rundeckExecution = new RundeckExecution();
        Job job = new Job();
        job.setName("TEST");

        rundeckExecution.setJob(job);

        // WHEN
        ResponseEntity<Object> response = rundeckExecutionController.addRundeckExecution(rundeckExecution);

        // THEN
        assertEquals(ResponseEntity.internalServerError().build(), response);
    }

}