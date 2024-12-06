package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rawdata.LunaticXmlDataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface LunaticXmlMongoDBRepository extends MongoRepository<LunaticXmlDataDocument, String> {
}
