package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface RawResponsePersistencePort {

    List<RawResponse> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList);
    void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds);
    List<String> getUnprocessedCollectionIds();
    Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);
    Page<RawResponse> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable);
}
