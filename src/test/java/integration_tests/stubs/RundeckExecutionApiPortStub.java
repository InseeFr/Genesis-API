package integration_tests.stubs;

import fr.insee.genesis.domain.model.rundeck.RundeckExecution;
import fr.insee.genesis.domain.ports.api.RundeckExecutionApiPort;
import lombok.Setter;

@Setter
public class RundeckExecutionApiPortStub implements RundeckExecutionApiPort {

    private boolean shouldThrowException = false;

    @Override
    public void addExecution(RundeckExecution rundeckExecution) {
        if (shouldThrowException) {
            throw new RuntimeException("Simulated exception");
        }
        // Otherwise, do nothing
        System.out.println("Execution saved: " + rundeckExecution.getJob().getName());
    }
}
