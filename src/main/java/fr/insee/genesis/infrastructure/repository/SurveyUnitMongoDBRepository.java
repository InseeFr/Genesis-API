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

	@Query(value = "{ 'questionnaireId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByQuestionnaireId(String questionnaireId);

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Query(value = "{ 'questionnaireId' : ?0}", count = true)
	long countInterrogationIdsByQuestionnaireId(String campaignId);


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
	//========= OPTIMISATIONS PERFS (END) ==========

	@Query(value = "{ 'campaignId' : ?0 }", fields = "{ 'interrogationId' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findInterrogationIdsByCampaignId(String campaignId);

	Long deleteByQuestionnaireId(String questionnaireId);

	@Meta(cursorBatchSize = 20)
	Stream<SurveyUnitDocument> findByQuestionnaireId(String questionnaireId);

	long count();

	@Query(value = "{ 'campaignId' : ?0 }", fields = "{ _id : 0, 'questionnaireId' : 1 }")
	Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

	long countByCampaignId(String campaignId);

	@Query(value = "{ 'questionnaireId' : ?0 }", fields = "{ _id : 0, 'campaignId' : 1 }")
	Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);
}