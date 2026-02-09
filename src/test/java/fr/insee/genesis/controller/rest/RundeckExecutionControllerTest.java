package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.api.RundeckExecutionApiPort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class RundeckExecutionControllerTest {
    RundeckExecutionApiPort rundeckExecutionApiPort;
    RundeckExecutionController rundeckExecutionController;

    @BeforeEach
    void setUp() {
        rundeckExecutionApiPort = mock(RundeckExecutionApiPort.class);
        rundeckExecutionController = new RundeckExecutionController(
                rundeckExecutionApiPort
        );
    }

    @Test
    void addRundeckExecution() {
        //GIVEN
        RundeckExecution rundeckExecution = new RundeckExecution();

        //WHEN
        rundeckExecutionController.addRundeckExecution(rundeckExecution);

        //THEN
        verify(rundeckExecutionApiPort, times(1)).addExecution(rundeckExecution);
    }
}