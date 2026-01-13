package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.extraction.json.LastJsonExtractionDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LastJsonExtractionMongoDBRepository extends MongoRepository<LastJsonExtractionDocument,String> {
}
