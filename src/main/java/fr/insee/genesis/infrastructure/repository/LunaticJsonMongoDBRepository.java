package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LunaticJsonMongoDBRepository extends MongoRepository<LunaticJsonDataDocument, String> {
    @Query("{\"processDate\" : null}")
    List<LunaticJsonDataDocument> findByNullProcessDate();
}
