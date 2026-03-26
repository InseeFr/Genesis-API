package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.LocalDateTime;
import java.util.List;

public interface DataProcessingContextApiPort {
    void saveContextByCollectionInstrumentId(String collectionInstrumentID, Boolean withReview) throws GenesisException;

    void saveKraftwerkExecutionScheduleByCollectionInstrumentId(String collectionInstrumentId,
                                        ServiceToCall serviceToCall,
                                        String frequency,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        TrustParameters trustParameters) throws GenesisException;

    void updateLastExecutionDateByCollectionInstrumentId(String collectionInstrumentId, LocalDateTime newDate) throws GenesisException;

    void deleteSchedulesByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;

    List<ScheduleDto> getAllSchedules();

    void deleteExpiredSchedules(String logFolder) throws GenesisException;

    long countContexts();

    DataProcessingContextModel getContext(String interrogationId) throws GenesisException;

    DataProcessingContextModel getContextByCollectionInstrumentId(String collectionInstrumentId);

    List<String> getCollectionInstrumentIds(boolean withReview);

    boolean getReviewByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;


}
