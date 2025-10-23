package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.ScheduleDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.context.schedule.KraftwerkExecutionSchedule;
import fr.insee.genesis.domain.model.context.schedule.ServiceToCall;
import fr.insee.genesis.domain.model.context.schedule.TrustParameters;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.LocalDateTime;
import java.util.List;

public interface DataProcessingContextApiPort {
    void saveContext(String partitionId, Boolean withReview) throws GenesisException;

    void saveKraftwerkExecutionSchedule(String partitionId,
                                        ServiceToCall serviceToCall,
                                        String frequency,
                                        LocalDateTime startDate,
                                        LocalDateTime endDate,
                                        TrustParameters trustParameters) throws GenesisException;

    void updateLastExecutionDate(String surveyName, LocalDateTime newDate) throws GenesisException;

    void deleteSchedules(String surveyName) throws GenesisException;

    List<ScheduleDto> getAllSchedules();

    List<KraftwerkExecutionSchedule> deleteExpiredSchedules(String surveyScheduleName) throws GenesisException;

    long countSchedules();

    DataProcessingContextModel getContext(String interrogationId) throws GenesisException;
    DataProcessingContextModel getContextByPartitionId(String partitionId) throws GenesisException;
    List<String> getPartitionIds(boolean withReview);

    /**
     * Gets the review indicator for a partition
     * @param partitionId id of the partition
     * @return the review indicator stored in genesis
     */
    boolean getReviewByPartitionId(String partitionId) throws GenesisException;
}
