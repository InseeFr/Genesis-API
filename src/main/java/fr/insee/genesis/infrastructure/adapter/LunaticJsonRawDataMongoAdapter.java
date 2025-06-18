package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.mappers.GroupedInterrogationDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Qualifier("lunaticJsonMongoAdapter")
public class LunaticJsonRawDataMongoAdapter implements LunaticJsonRawDataPersistencePort {
    private final LunaticJsonMongoDBRepository repository;
    private final MongoTemplate mongoTemplate;

    public LunaticJsonRawDataMongoAdapter(LunaticJsonMongoDBRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void save(LunaticJsonRawDataModel rawData) {
        LunaticJsonRawDataDocument doc = LunaticJsonRawDataDocumentMapper.INSTANCE.modelToDocument(rawData);
        repository.insert(doc);
    }

    @Override
    public List<LunaticJsonRawDataModel> getAllUnprocessedData() {
        return LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(repository.findByNullProcessDate());
    }

    @Override
    public List<LunaticJsonRawDataModel> findRawData(String campaignName, Mode mode, List<String> interrogationIdList) {
        List<LunaticJsonRawDataDocument> rawDataDocs = repository.findModesByCampaignIdAndByModeAndinterrogationIdIninterrogationIdList(campaignName, mode, interrogationIdList);return LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(rawDataDocs);
    }

    @Override
    public void updateProcessDates(String campaignId, Set<String> interrogationIds) {
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("campaignId").is(campaignId).and("interrogationId").in(interrogationIds))
                , new Update().set("processDate", LocalDateTime.now())
                , Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME
        );
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        Set<String> questionnaireIds = new HashSet<>();
        for(String questionnaireId : mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_RAW_COLLECTION_NAME).distinct(
                "questionnaireId",
                String.class)){
            questionnaireIds.add(questionnaireId);
        }
        return questionnaireIds;
    }

    @Override
    public long countResponsesByQuestionnaireId(String questionnaireId) {
        return repository.countByQuestionnaireId(questionnaireId);
    }

    @Override
    public List<GroupedInterrogation> findProcessedIdsGroupedByQuestionnaireSince(LocalDateTime since){
        return GroupedInterrogationDocumentMapper.INSTANCE.listDocumentToListModel(repository.aggregateRawGroupedByQuestionnaire(since));
    }


}
