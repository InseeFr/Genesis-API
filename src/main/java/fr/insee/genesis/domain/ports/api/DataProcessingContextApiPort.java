package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.LocalDateTime;
import java.util.List;

public interface DataProcessingContextApiPort {
    void saveContext(String partitionId, Boolean withReview) throws GenesisException;
    void saveContextByCollectionInstrumentId(String collectionInstrumentID, Boolean withReview) throws GenesisException;

    @Deprecated(forRemoval = true)
    void saveKraftwerkExecutionSchedule(String partitionId,
                                        ServiceToCall serviceToCall,
                                        String frequency,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        TrustParameters trustParameters) throws GenesisException;

    void saveKraftwerkExecutionScheduleByCollectionInstrumentId(String collectionInstrumentId,
                                        ServiceToCall serviceToCall,
                                        String frequency,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        TrustParameters trustParameters) throws GenesisException;

    void updateLastExecutionDate(String surveyName, LocalDateTime newDate) throws GenesisException;
    void updateLastExecutionDateByCollectionInstrumentId(String collectionInstrumentId, LocalDateTime newDate) throws GenesisException;

    void deleteSchedules(String surveyName) throws GenesisException;
    void deleteSchedulesByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;

    List<ScheduleDto> getAllSchedules();

    void deleteExpiredSchedules(String logFolder) throws GenesisException;

    long countSchedules();

    DataProcessingContextModel getContext(String interrogationId) throws GenesisException;

    DataProcessingContextModel getContextByCollectionInstrumentId(String collectionInstrumentId);

    @Deprecated(forRemoval = true)
    List<String> getPartitionIds(boolean withReview);

    List<String> getCollectionInstrumentIds(boolean withReview);

    /**
     * Gets the review indicator for a partition
     * @param partitionId id of the partition
     * @return the review indicator stored in genesis
     */
    @Deprecated(forRemoval = true)
    boolean getReviewByPartitionId(String partitionId) throws GenesisException;

    boolean getReviewByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;


}
