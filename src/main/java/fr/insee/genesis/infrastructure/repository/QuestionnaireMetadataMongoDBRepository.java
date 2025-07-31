package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface QuestionnaireMetadataMongoDBRepository extends CrudRepository<QuestionnaireMetadataDocument, String> {
    @Query("{'questionnaireId' : ?0}")
    List<QuestionnaireMetadataDocument> findByQuestionnaireId(String questionnaireId);

    @Query(value = "{'questionnaireId' : ?0}", delete = true)
    void deleteByQuestionnaireId(String questionnaireId);
}
