package fr.insee.genesis.domain.ports.spi;

import java.time.Instant;
import java.util.Set;

public interface RawResponseReprocessPersistencePort {

    Set<String> findProcessedInterrogationIdsByCollectionInstrumentId(
            String collectionInstrumentId);

    Set<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId, Instant sinceDate, Instant endDate);

    void resetProcessDates(String collectionInstrumentId, Set<String> interrogationIds);

}
