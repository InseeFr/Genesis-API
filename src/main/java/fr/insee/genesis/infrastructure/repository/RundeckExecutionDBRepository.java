package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rundeck.RundeckExecutionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RundeckExecutionDBRepository extends MongoRepository<RundeckExecutionDocument, String> {
}
