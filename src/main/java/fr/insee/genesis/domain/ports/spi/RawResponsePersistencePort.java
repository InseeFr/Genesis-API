package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface RawResponsePersistencePort {

    List<RawResponseModel> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList);
    List<RawResponseModel> findRawResponsesByInterrogationID(String interrogationId);
    void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds);
    List<String> getUnprocessedCollectionIds();
    Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);
    Page<RawResponseModel> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable);
}
