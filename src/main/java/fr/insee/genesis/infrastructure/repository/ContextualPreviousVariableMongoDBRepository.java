package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.contextualprevious.ContextualPreviousVariableDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ContextualPreviousVariableMongoDBRepository extends MongoRepository<ContextualPreviousVariableDocument,String> {
    void deleteByQuestionnaireId(String questionnaireId);
    void deleteByCollectionInstrumentId(String collectionInstrumentId);

    @Query(value = "{ 'questionnaireId' : ?0, 'interrogationId' : ?1 }")
    List<ContextualPreviousVariableDocument> findByQuestionnaireIdAndInterrogationId(String questionnaireId, String interrogationId);
    @Query(value = "{ 'questionnaireId' : ?0, 'interrogationId' : {$in: ?1} }")
    List<ContextualPreviousVariableDocument> findByQuestionnaireIdAndInterrogationIdList(String collectionInstrumentId, List<String> interrogationIds);

    @Query(value = "{ 'collectionInstrumentId' : ?0, 'interrogationId' : ?1 }")
    List<ContextualPreviousVariableDocument> findByCollectionInstrumentIdAndInterrogationId(String collectionInstrumentId, String interrogationId);
    @Query(value = "{ 'collectionInstrumentId' : ?0, 'interrogationId' : {$in: ?1} }")
    List<ContextualPreviousVariableDocument> findByCollectionInstrumentIdAndInterrogationIdList(String collectionInstrumentId, List<String> interrogationIds);
}
