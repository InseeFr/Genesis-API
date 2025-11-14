package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RawResponseRepository extends MongoRepository<RawResponseDocument,String> {

    @Query(value = "{ 'collectionInstrumentId' : ?0, 'mode' : ?1, 'interrogationId': {$in: ?2} }")
    List<RawResponseDocument> findByCollectionInstrumentIdAndModeAndInterrogationIdList(String questionnaireId, Mode mode, List<String> interrogationIdList);
}
