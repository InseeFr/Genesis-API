package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
import org.springframework.data.mongodb.repository.Meta;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Repository
public interface SurveyUnitMongoDBRepository extends MongoRepository<SurveyUnitDocument, String> {

	List<SurveyUnitDocument> findByIdUE(String idUE);

	List<SurveyUnitDocument> findByIdUEAndIdQuestionnaire(String idUE, String idQuestionnaire);

	@Query(value = "{ 'idQuestionnaire' : ?0 }", fields = "{ 'idUE' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findIdUEsByIdQuestionnaire(String idQuestionnaire);

	@Query(value = "{ 'idCampaign' : ?0 }", fields = "{ 'idUE' : 1, 'mode' :  1 }")
	List<SurveyUnitDocument> findIdUEsByIdCampaign(String idCampaign);

	Long deleteByIdQuestionnaire(String idQuestionnaire);

	@Meta(cursorBatchSize = 20)
	Stream<SurveyUnitDocument> findByIdQuestionnaire(String idQuestionnaire);

	long count();

	@Query(value = "{ 'idCampaign' : ?0 }", fields = "{ _id : 0, 'idQuestionnaire' : 1 }")
	Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

	long countByIdCampaign(String idCampaign);

	@Query(value = "{ 'idQuestionnaire' : ?0 }", fields = "{ _id : 0, 'idCampaign' : 1 }")
	Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire);
}