package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.contextualexternal.ContextualExternalVariableDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContextualExternalVariableMongoDBRepository extends MongoRepository<ContextualExternalVariableDocument,String> {
    void deleteByQuestionnaireId(String questionnaireId);
    void deleteByCollectionInstrumentId(String collectionInstrumentId);

    @Query(value = "{ 'questionnaireId' : ?0, 'interrogationId' : ?1 }")
    List<ContextualExternalVariableDocument> findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);

    @Query(value = "{ 'collectionInstrumentId' : ?0, 'interrogationId' : ?1 }")
    List<ContextualExternalVariableDocument> findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
}
