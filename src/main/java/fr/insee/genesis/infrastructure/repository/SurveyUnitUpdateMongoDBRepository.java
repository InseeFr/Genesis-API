package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.model.entity.SurveyUnitUpdate;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SurveyUnitUpdateMongoDBRepository extends MongoRepository<SurveyUnitUpdateDocument, String> {

	List<SurveyUnitUpdateDocument> findByIdUE(String idUE);

	List<SurveyUnitUpdateDocument> findByIdQuestionnaire(String idQuestionnaire);

	List<SurveyUnitUpdateDocument> findByIdUEAndIdQuestionnaire(String idUE, String idQuestionnaire);


}
