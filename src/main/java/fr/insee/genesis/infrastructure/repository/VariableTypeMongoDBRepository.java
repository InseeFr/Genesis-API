package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface VariableTypeMongoDBRepository extends MongoRepository<SurveyMetadataDocument, String> {

    @Query(value = "{ 'campaignId' : ?0 , 'questionnaireId' : ?1 , 'mode' : ?2 }")
    SurveyMetadataDocument findFirstByCampaignIdQuestionnaireIdMode(String campaignId, String questionnaireId, Mode mode);
}
