package fr.insee.genesis.infrastructure.repository;

import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface LunaticModelMongoDBRepository extends CrudRepository<LunaticModelDocument, String> {

    @Query("{'questionnaireId' : ?0}")
    List<LunaticModelDocument> findByQuestionnaireId(String questionnaireId);
}
