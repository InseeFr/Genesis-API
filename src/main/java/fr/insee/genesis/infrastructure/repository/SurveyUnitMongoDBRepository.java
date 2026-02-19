package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface SurveyUnitMongoDBRepository extends MongoRepository<SurveyUnitDocument, String> {

	List<SurveyUnitDocument> findByInterrogationId(String interrogationId);

	List<SurveyUnitDocument> findByInterrogationIdAndQuestionnaireId(String interrogationId, String questionnaireId);
	List<SurveyUnitDocument> findByInterrogationIdAndCollectionInstrumentId(String interrogationId, String collectionInstrumentId);

	List<SurveyUnitDocument> findByUsualSurveyUnitIdAndCollectionInstrumentId(String usualSurveyUnitId, String collectionInstrumentId);
	List<SurveyUnitDocument> findByUsualSurveyUnitIdAndQuestionnaireId(String usualSurveyUnitId, String questionnaireId);

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Query("{ 'questionnaireId' : ?0, 'mode' : ?1, 'interrogationId' : { $in: ?2 } }")
	List<SurveyUnitDocument> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);
	//========= OPTIMISATIONS PERFS (END) ==========

	@Query(value = "{ 'questionnaireId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByQuestionnaireId(String questionnaireId);

	@Query(value = "{ 'collectionInstrumentId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByCollectionInstrumentId(String questionnaireId);

	@Query(value = "{ 'questionnaireId' : ?0, 'recordDate': { $gte: ?1 } }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByQuestionnaireIdAndDateAfter(String questionnaireId, LocalDateTime since);

	@Query(value = "{ 'collectionInstrumentId' : ?0, 'recordDate': { $gte: ?1 } }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByCollectionInstrumentIdAndDateAfter(String collectionInstrumentId, LocalDateTime since);

    @Query(
            value = "{ 'collectionInstrumentId' : ?0, 'recordDate': { $gte: ?1, $lt: ?2 } }",
            fields = "{ 'interrogationId' : 1, 'mode' : 1 }"
    )
    List<SurveyUnitDocument> findInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
            String collectionInstrumentId,
            LocalDateTime start,
            LocalDateTime end
    );

    @Query(
            value = "{ 'questionnaireId' : ?0, 'recordDate': { $gte: ?1, $lt: ?2 } }",
            fields = "{ 'interrogationId' : 1, 'mode' : 1 }"
    )
    List<SurveyUnitDocument> findInterrogationIdsQuestionnaireIdAndRecordDateBetween(
            String questionnaireId,
            LocalDateTime start,
            LocalDateTime end
    );

    /**
	 * @author Adrien Marchal
	 */
	@Query(value = "{ 'questionnaireId' : ?0 }", count = true)
	long countByQuestionnaireId(String questionnaireId);

	@Query(value = "{ 'collectionInstrumentId' : ?0 }", count = true)
	long countByCollectionInstrumentId(String collectionInstrumentId);

	/**
	 * @author Adrien Marchal
	 */
	@Aggregation(pipeline = {
			"{ '$match': { 'questionnaireId' : ?0 } }",
			"{ '$sort' : { 'questionnaireId' : 1 } }",
			"{ '$skip' : ?1 }",
			"{ '$limit' : ?2 }"
	})
	List<SurveyUnitDocument> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit);

	@Aggregation(pipeline = {
			"{ '$match': { 'campaignId' : ?0 } }",
			"{ '$group': { '_id': '$mode' } }",
			"{ '$set': { 'mode': '$_id', '_id': '$$REMOVE' } }"
	})
	List<SurveyUnitDocument> findModesByCampaignIdV2(String campaignId);

	@Aggregation(pipeline = {
			"{ '$match': { 'questionnaireId' : ?0 } }",
			"{ '$group': { '_id': '$mode' } }",
			"{ '$set': { 'mode': '$_id', '_id': '$$REMOVE' } }"
	})
	List<SurveyUnitDocument> findModesByQuestionnaireIdV2(String campaignId);

	@Query(value = "{ 'campaignId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByCampaignId(String campaignId);

	Long deleteByQuestionnaireId(String questionnaireId);
	Long deleteByCollectionInstrumentId(String collectionInstrumentId);

	@Meta(cursorBatchSize = 20)
	Stream<SurveyUnitDocument> findByQuestionnaireId(String questionnaireId);

	long count();

	@Query(value = "{ 'campaignId' : ?0 }", fields = "{ _id : 0, 'questionnaireId' : 1 }")
	Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 * Here we make a "DISTINCT" query
	 */
	@Aggregation(pipeline = {
			"{ '$match': { 'campaignId' : ?0 } }",
			"{ '$group': { '_id': { 'questionnaireId' : '$questionnaireId'} } }",
			"{ '$set': { 'questionnaireId': '$_id', '_id': '$$REMOVE' } }"
	})
	Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId);
	//========= OPTIMISATIONS PERFS (END) ==========

	long countByCampaignId(String campaignId);

	@Query(value = "{ 'questionnaireId' : ?0 }", fields = "{ _id : 0, 'campaignId' : 1 }")
	Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);
}