package fr.insee.genesis.domain.ports.spi;

import java.time.LocalDateTime;
import java.util.Set;

public interface RawResponseReprocessPersistencePort {

    Set<String> findProcessedInterrogationIdsByCollectionInstrumentId(
            String collectionInstrumentId);

    Set<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId, LocalDateTime sinceDate, LocalDateTime endDate);

    void resetProcessDates(String collectionInstrumentId, Set<String> interrogationIds);

}
