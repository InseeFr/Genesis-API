package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.KraftwerkExecutionScheduleInput;
import fr.insee.genesis.controller.dto.rawdata.ScheduleResponseDto;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.List;

public interface DataProcessingContextApiPort {
    void saveContextByCollectionInstrumentId(String collectionInstrumentID, Boolean withReview) throws GenesisException;
    
    String createKraftwerkExecutionSchedule(KraftwerkExecutionScheduleInput scheduleInput) throws GenesisException;

    void updateKraftwerkExecutionSchedule(KraftwerkExecutionScheduleInput scheduleInput) throws GenesisException;

    void deleteScheduleV2(String collectionInstrumentId, String scheduleUuid) throws GenesisException;

    void deleteSchedulesByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;

    void deleteSchedulesV2ByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;

    List<ScheduleResponseDto> getSchedulesV2ByCollectionInstrumentId(String collectionInstrumentId);

    List<ScheduleResponseDto> getAllSchedulesV1();
    List<ScheduleResponseDto> getAllSchedulesV2();

    void deleteExpiredSchedules(String logFolder) throws GenesisException;

    long countContexts();

    DataProcessingContextModel getContext(String interrogationId) throws GenesisException;

    DataProcessingContextModel getContextByCollectionInstrumentId(String collectionInstrumentId);

    List<String> getCollectionInstrumentIds(boolean withReview);

    boolean getReviewByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException;

}
