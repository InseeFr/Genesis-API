package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.ports.spi.SurveyMetadataPersistancePort;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.repository.SurveyMetadataMongoDBRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.FindAndReplaceOptions;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.stereotype.Service;

@Service
@Qualifier("variableTypeMongoAdapter")
public class SurveyMetadataMongoAdapter implements SurveyMetadataPersistancePort {
    private final SurveyMetadataMongoDBRepository surveyMetadataMongoDBRepository;
    private final MongoTemplate mongoTemplate;

    @Autowired
    public SurveyMetadataMongoAdapter(SurveyMetadataMongoDBRepository surveyMetadataMongoDBRepository, MongoTemplate mongoTemplate) {
        this.surveyMetadataMongoDBRepository = surveyMetadataMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(SurveyMetadataModel surveyMetadataModel) {
        mongoTemplate.update(SurveyMetadataDocument.class)
                .matching(Query.query(Criteria.where("campaignId").is(surveyMetadataModel.campaignId())))
                .replaceWith(SurveyMetadataDocumentMapper.INSTANCE.modelToDocument(surveyMetadataModel))
                .withOptions(FindAndReplaceOptions.options().upsert())
                .findAndReplace();
    }

    @Override
    public SurveyMetadataDocument find(String campaignId, String questionnaireId, Mode mode) {
        return surveyMetadataMongoDBRepository.findFirstByCampaignIdQuestionnaireIdMode(campaignId, questionnaireId, mode);
    }
}
