package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.GroupedInterrogationDocument;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface LunaticJsonMongoDBRepository extends MongoRepository<LunaticJsonRawDataDocument,String> {

    @Query("{\"processDate\" : null}")
    List<LunaticJsonRawDataDocument> findByNullProcessDate();
    @Query(value = "{ 'campaignId' : ?0 }", fields = "{ 'mode' :  1 }")
    List<Mode> findModesByCampaignId(String campaignId);
    @Query(value = "{ 'campaignId' : ?0, 'mode' : ?1, 'interrogationId': {$in: ?2} }")
    List<LunaticJsonRawDataDocument> findModesByCampaignIdAndByModeAndinterrogationIdIninterrogationIdList(String campaignName, Mode mode, List<String> interrogationIdList);
    long countByQuestionnaireId(String questionnaireId);
    @Aggregation(pipeline = {
            "{ '$match': { 'processDate': { '$gte': ?0 } } }",
            "{ '$group': { '_id': '$questionnaireId', 'interrogationIds': { '$addToSet': '$interrogationId' } } }",
            "{ '$project': { 'questionnaireId': '$_id', 'interrogationIds': 1, '_id': 0 } }"
    })
    List<GroupedInterrogationDocument> aggregateRawGroupedByQuestionnaire(LocalDateTime since);

}
