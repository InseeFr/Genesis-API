package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.List;

import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
@Qualifier("lunaticModelMongoAdapter")
public class LunaticModelMongoAdapter implements LunaticModelPersistancePort {
    private final LunaticModelMongoDBRepository repository;
    private final MongoTemplate mongoTemplate;

    public LunaticModelMongoAdapter(LunaticModelMongoDBRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(LunaticModelModel lunaticModelModel) {
        mongoTemplate.update(LunaticModelDocument.class)
                .matching(where("questionnaireId").is(lunaticModelModel.questionnaireId()))
                .apply(new Update().set("lunaticModel", lunaticModelModel.lunaticModel()))
                .upsert();
    }

    @Override
    public List<LunaticModelDocument> find(String questionnaireId) {
        return repository.findByQuestionnaireId(questionnaireId);
    }
}
