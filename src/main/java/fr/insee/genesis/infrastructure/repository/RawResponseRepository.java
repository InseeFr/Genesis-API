package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;

@Repository
public interface RawResponseRepository extends MongoRepository<RawResponseDocument,String> {

    @Query(value = "{ 'collectionInstrumentId' : ?0, 'mode' : ?1, 'interrogationId': {$in: ?2} }")
    List<RawResponseDocument> findByCollectionInstrumentIdAndModeAndInterrogationIdList(String questionnaireId, String mode, List<String> interrogationIdList);
    @Aggregation(pipeline = {
            "{ $match: { processDate: null } }",
            "{ $group: { _id: '$collectionInstrumentId' } }",
            "{ $project: { _id: 0, collectionInstrumentId: '$_id' } }"
    })
    List<String> findDistinctCollectionInstrumentIdByProcessDateIsNull();

    @Aggregation(pipeline = {
            "{ $match: { collectionInstrumentId: ?0,processDate: null } }",
            "{ $project: { _id: 0, interrogationId: '$interrogationId' } }"
    })
    List<String> findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(String collectionInstrumentId);

    @Query(value = "{ 'payload.campaignId' : ?0, 'recordDate' : { $gte: ?1, $lte: ?2 } }")
    Page<RawResponseDocument> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable);
}
