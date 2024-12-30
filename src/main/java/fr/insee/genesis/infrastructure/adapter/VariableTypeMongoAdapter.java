package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.variabletype.VariableTypeModel;
import fr.insee.genesis.domain.ports.spi.VariableTypePersistancePort;
import fr.insee.genesis.infrastructure.document.variabletype.VariableTypeDocument;
import fr.insee.genesis.infrastructure.mappers.VariableTypeDocumentMapper;
import fr.insee.genesis.infrastructure.repository.VariableTypeMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Qualifier("variableTypeMongoAdapter")
public class VariableTypeMongoAdapter implements VariableTypePersistancePort {
    private final VariableTypeMongoDBRepository variableTypeMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public VariableTypeMongoAdapter(VariableTypeMongoDBRepository variableTypeMongoDBRepository, MongoTemplate mongoTemplate) {
        this.variableTypeMongoDBRepository = variableTypeMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(VariableTypeModel variableTypeModel) {
        mongoTemplate.update(VariableTypeDocument.class)
                .matching(Query.query(Criteria.where("campaignId").is(variableTypeModel.campaignId())))
                .replaceWith(VariableTypeDocumentMapper.INSTANCE.modelToDocument(variableTypeModel))
                .withOptions(FindAndReplaceOptions.options().upsert())
                .findAndReplace();
    }

    @Override
    public VariableTypeDocument find(String campaignId, String questionnaireId, Mode mode) {
        return variableTypeMongoDBRepository.findFirstByCampaignIdQuestionnaireIdMode(campaignId, questionnaireId, mode);
    }
}
