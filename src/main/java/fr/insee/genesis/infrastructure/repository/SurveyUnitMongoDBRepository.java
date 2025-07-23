package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface SurveyUnitMongoDBRepository extends MongoRepository<SurveyUnitDocument, String> {

	List<SurveyUnitDocument> findByInterrogationId(String interrogationId);

	List<SurveyUnitDocument> findByInterrogationIdAndQuestionnaireId(String interrogationId, String questionnaireId);

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Query("{ 'questionnaireId' : ?0, 'mode' : ?1, 'interrogationId' : { $in: ?2 } }")
	List<SurveyUnitDocument> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);
	//========= OPTIMISATIONS PERFS (END) ==========

	/**
	 * !!!WARNING!!! : A CALL WITH THIS ENDPOINT ON A BIG COLLECTION (> 300k) MAY KILL THE GENESIS-API APP.!!!
	 */
	@Query(value = "{ 'questionnaireId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByQuestionnaireId(String questionnaireId);

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Query(value = "{ 'questionnaireId' : ?0}", count = true)
	long countInterrogationIdsByQuestionnaireId(String questionnaireId);


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
	List<SurveyUnitDocument> findModesByQuestionnaireIdV2(String questionnaireId);
	//========= OPTIMISATIONS PERFS (END) ==========


	Long deleteByQuestionnaireId(String questionnaireId);

	@Meta(cursorBatchSize = 20)
	Stream<SurveyUnitDocument> findByQuestionnaireId(String questionnaireId);

	long count();


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