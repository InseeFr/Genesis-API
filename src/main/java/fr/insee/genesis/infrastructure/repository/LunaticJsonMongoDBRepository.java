package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LunaticJsonMongoDBRepository extends MongoRepository<LunaticJsonDataDocument, String> {
}
