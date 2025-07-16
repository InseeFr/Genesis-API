package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.editedexternal.EditedExternalResponseDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EditedExternalResponseMongoDBRepository extends MongoRepository<EditedExternalResponseDocument,String> {
    void deleteByQuestionnaireId(String questionnaireId);
}
