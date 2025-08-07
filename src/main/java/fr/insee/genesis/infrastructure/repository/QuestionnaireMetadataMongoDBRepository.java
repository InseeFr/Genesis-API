package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionnaireMetadataMongoDBRepository extends CrudRepository<QuestionnaireMetadataDocument, String> {
    @Query("{'questionnaireId' : ?0, 'mode' : ?1}")
    List<QuestionnaireMetadataDocument> findByQuestionnaireIdAndMode(String questionnaireId, Mode mode);

    @Query(value = "{'questionnaireId' : ?0, 'mode' : ?1}", delete = true)
    void deleteByQuestionnaireIdAndMode(String questionnaireId, Mode mode);
}
