package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.lunaticmodel.LunaticModelModel;
import fr.insee.genesis.domain.ports.spi.LunaticModelPersistancePort;
import fr.insee.genesis.infrastructure.document.lunaticmodel.LunaticModelDocument;
import fr.insee.genesis.infrastructure.repository.LunaticModelMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

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
        Criteria criteria = new Criteria().orOperator(
                Criteria.where("questionnaireId").is(lunaticModelModel.collectionInstrumentId()),
                Criteria.where("collectionInstrumentId").is(lunaticModelModel.collectionInstrumentId())
        );
        mongoTemplate.update(LunaticModelDocument.class)
                .matching(criteria)
                .apply(new Update().set("lunaticModel", lunaticModelModel.lunaticModel()))
                .upsert();
    }

    @Override
    public List<LunaticModelDocument> find(String collectionInstrumentId) {
        List<LunaticModelDocument> results = new ArrayList<>();
        results.addAll(repository.findByCollectionInstrumentId(collectionInstrumentId));
        results.addAll(repository.findByQuestionnaireId(collectionInstrumentId));
        return results.stream()
                .distinct()
                .toList();
    }

}
