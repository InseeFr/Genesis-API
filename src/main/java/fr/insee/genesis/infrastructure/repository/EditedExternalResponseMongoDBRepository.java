package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EditedExternalResponseMongoDBRepository extends MongoRepository<EditedExternalResponseDocument,String> {
    void deleteByQuestionnaireId(String questionnaireId);

    @Query(value = "{ 'questionnaireId' : ?0, 'interrogationId' : ?1 }")
    List<EditedExternalResponseDocument> findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
}
