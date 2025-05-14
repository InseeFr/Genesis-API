package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.schedule.TrustParameters;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.LocalDateTime;

public interface DataProcessingContextApiPort {
    void saveContext(String partitionId, Boolean withReview) throws GenesisException;

    void saveKraftwerkExecutionSchedule(String partitionId,
                                        String frequency,
                                        ServiceToCall serviceToCall,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        TrustParameters trustParameters) throws GenesisException;
}
