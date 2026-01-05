package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;

import java.util.List;
import java.util.Set;

public interface RawResponsePersistencePort {

    List<RawResponseModel> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList);
    List<RawResponseModel> findRawResponsesByInterrogationID(String interrogationId);
    void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds);
    List<String> getUnprocessedCollectionIds();
    Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);
 }
