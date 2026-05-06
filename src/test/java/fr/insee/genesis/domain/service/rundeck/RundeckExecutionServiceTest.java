package fr.insee.genesis.domain.service.rundeck;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.spi.RundeckExecutionPersistencePort;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RundeckExecutionServiceUnitTest {

    private RundeckExecutionService rundeckExecutionService;

    @Mock
    private RundeckExecutionPersistencePort rundeckExecutionPersistencePort;

    @BeforeEach
    void init() {
        rundeckExecutionService = new RundeckExecutionService(rundeckExecutionPersistencePort);
    }

    @Test
    @DisplayName("addExecution should call persistencePort")
    void addExecution_shouldDelegateToPersistencePort() {
        // GIVEN
        RundeckExecution execution = new RundeckExecution();

        // WHEN
        rundeckExecutionService.addExecution(execution);

        // THEN
        verify(rundeckExecutionPersistencePort, times(1)).save(execution);
    }
}