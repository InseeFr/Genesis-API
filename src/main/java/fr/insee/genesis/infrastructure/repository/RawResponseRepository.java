package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.modelefiliere.ModeDto;
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

    RawResponseDocument findByCollectionInstrumentIdAndInterrogationId(
            String collectionInstrumentId,
            String interrogationId
    );

    @Aggregation(pipeline = {
            "{ $match: { processDate: null, collectionInstrumentId: { $ne: null } } }",
            "{ $group: { _id: '$collectionInstrumentId' } }",
            "{ $project: { _id: 0, collectionInstrumentId: '$_id' } }"
    })
    List<String> findDistinctCollectionInstrumentIdByProcessDateIsNull();

    @Aggregation(pipeline = {
            "{ $match: { collectionInstrumentId: ?0, processDate: { $ne: null }, recordDate: { $gte: ?1, $lte: ?2 } } }",
            "{ $group: { _id: '$interrogationId' } }",
            "{ $project: { _id: 0, interrogationId: '$_id' } }"
    })
    List<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId,
            Instant sinceDate,
            Instant endDate
    );

    @Aggregation(pipeline = {
            "{ $match: { collectionInstrumentId: ?0, processDate: { $ne: null } } }",
            "{ $group: { _id: '$interrogationId' } }",
            "{ $project: { _id: 0, interrogationId: '$_id' } }"
    })
    List<String> findProcessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);

    @Aggregation(pipeline = {
            "{ $match: { collectionInstrumentId: ?0,processDate: null } }",
            "{ $project: { _id: 0, interrogationId: '$interrogationId' } }"
    })
    List<String> findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(String collectionInstrumentId);

    @Aggregation(pipeline = {
            "{ '$match': { 'collectionInstrumentId': ?0 } }",
            "{ '$project': { '_id': 0, 'mode': 1 } }"
    })
    List<ModeDto> findModesByCollectionInstrumentId(String collectionInstrumentId);

    @Query(value = "{ 'payload.campaignId' : ?0, 'recordDate' : { $gte: ?1, $lte: ?2 } }")
    Page<RawResponseDocument> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable);
    
    @Query(value = "{ 'interrogationId': ?0}")
    List<RawResponseDocument> findByInterrogationId(String interrogationId);


    long countByCollectionInstrumentId(String collectionInstrumentId);

    @Aggregation(pipeline = {
            "{ $group: { _id: '$collectionInstrumentId' } }",
            "{ $project: { _id: 0, collectionInstrumentId: '$_id' } }"
    })
    List<String> findDistinctCollectionInstrumentId();
    Page<RawResponseDocument> findByCollectionInstrumentId(String collectionInstrumentId, Pageable pageable);

    List<RawResponseDocument> findByCollectionInstrumentId(String collectionInstrumentId);

    boolean existsByInterrogationId(String interrogationId);

    @Aggregation(pipeline = {
            "{ '$match': { 'collectionInstrumentId': ?0 } }",
            "{ '$group': { '_id': '$interrogationId' } }",
            "{ '$count': 'count' }"
    })
    Long countDistinctInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);

}
