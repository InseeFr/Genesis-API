package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DataProcessingContextMongoDBRepository extends MongoRepository<DataProcessingContextDocument,String> {
    @Query(value = "{ 'partitionId' : {$in: ?0} }")
    List<DataProcessingContextDocument> findByPartitionIdList(List<String> partitionIds);

    @Query(value = "{ 'partitionId' : ?0 }", delete = true)
    void deleteByPartitionId(String partitionId);
}
