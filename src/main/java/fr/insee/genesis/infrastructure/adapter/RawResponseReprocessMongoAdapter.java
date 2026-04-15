package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.infrastructure.repository.RawResponseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Qualifier("rawResponseMongoAdapter")
public class RawResponseReprocessMongoAdapter implements RawResponseReprocessPersistencePort {

    private final RawResponseRepository repository;
    private final MongoTemplate mongoTemplate;

    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
        return new HashSet<>(repository.findProcessedInterrogationIdsByCollectionInstrumentId(collectionInstrumentId));
    }

    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId, Instant sinceDate, Instant endDate) {
        return new HashSet<>(
                repository.findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
                        collectionInstrumentId, sinceDate, endDate));
    }

    @Override
    public void resetProcessDates(String collectionInstrumentId, Set<String> interrogationIds) {
        mongoTemplate.updateMulti(
                Query.query(
                        Criteria.where("collectionInstrumentId").is(collectionInstrumentId)
                                .and("interrogationId").in(interrogationIds)
                ),
                new Update().unset("processDate"),
                Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME
        );
    }

}
