package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.AllArgsConstructor;
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
@AllArgsConstructor
@Qualifier("lunaticJsonReprocessMongoAdapter")
public class LunaticJsonReprocessMongoAdapter implements RawResponseReprocessPersistencePort {

    private final LunaticJsonMongoDBRepository repository;
    private final MongoTemplate mongoTemplate;

    /**
     * @param questionnaireId Legacy name for 'collectionInstrumentId'.
     */
    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentId(String questionnaireId) {
        return new HashSet<>(repository.findProcessedInterrogationIdsByQuestionnaireId(questionnaireId));
    }

    /**
     * @param questionnaireId Legacy name for 'collectionInstrumentId'.
     */
    @Override
    public Set<String> findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String questionnaireId, Instant sinceDate, Instant endDate) {
        return new HashSet<>(
                repository.findProcessedInterrogationIdsByQuestionnaireIdAndRecordDateBetween(
                        questionnaireId, sinceDate, endDate));
    }

    /**
     * @param questionnaireId Legacy name for 'collectionInstrumentId'.
     */
    @Override
    public void resetProcessDates(String questionnaireId, Set<String> interrogationIds) {
        mongoTemplate.updateMulti(
                Query.query(
                        Criteria.where("questionnaireId").is(questionnaireId)
                                .and("interrogationId").in(interrogationIds)
                ),
                new Update().unset("processDate"),
                Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME
        );
    }

}
