package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.editedprevious.EditedPreviousResponseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditedPreviousResponseMongoDBRepository extends MongoRepository<EditedPreviousResponseDocument,String> {
    void deleteByQuestionnaireId(String questionnaireId);
}
