package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.api.RundeckExecutionApiPort;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RundeckExecutionControllerTest {

    @Mock
    RundeckExecutionApiPort rundeckExecutionApiPort;

    @InjectMocks
    RundeckExecutionController rundeckExecutionController;

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