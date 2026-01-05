package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.mappers.GroupedInterrogationDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.Instant;
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
    public Set<String> findDistinctQuestionnaireIdsByNullProcessDate(){
        return getDistinctQuestionnaireIdsInCollection(Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME);
    }

    @Override
    public Set<Mode> findModesByQuestionnaire(String questionnaireId) {
        return new HashSet<>(repository.findModesByQuestionnaireId(questionnaireId));
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
        return getDistinctQuestionnaireIdsInCollection(Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME);
    }

    @Override
    public Page<LunaticJsonRawDataModel> findByCampaignIdAndDate(String campaignId, Instant startDt, Instant  endDt, Pageable pageable) {
        Page<LunaticJsonRawDataDocument> rawDataDocsPage =  repository.findByCampaignIdAndRecordDateBetween(campaignId,startDt,endDt,pageable);
        List<LunaticJsonRawDataModel> modelList = LunaticJsonRawDataDocumentMapper.INSTANCE.listDocumentToListModel(rawDataDocsPage.getContent());
        return new PageImpl<>(modelList, rawDataDocsPage.getPageable(), rawDataDocsPage.getTotalElements());
    }

    @Override
    public long countResponsesByQuestionnaireId(String questionnaireId) {
        return repository.countByQuestionnaireId(questionnaireId);
    }

    @Override
    public List<GroupedInterrogation> findProcessedIdsGroupedByQuestionnaireSince(LocalDateTime since){
        return GroupedInterrogationDocumentMapper.INSTANCE.listDocumentToListModel(repository.aggregateRawGrouped(since));
    }

    @Override
    public List<GroupedInterrogation> findUnprocessedIds() {
        return GroupedInterrogationDocumentMapper.INSTANCE.listDocumentToListModel(repository.aggregateRawGroupedWithNullProcessDate());
    }

    @Override
    public Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
        Set<String> interrogationIds = new HashSet<>();

        repository.aggregateRawGroupedWithNullProcessDate(collectionInstrumentId).forEach(
                groupedInterrogationDocument -> interrogationIds.addAll(groupedInterrogationDocument.getInterrogationIds())
        );

        return interrogationIds;
    }

    private Set<String> getDistinctQuestionnaireIdsInCollection(String collectionName) {
        Set<String> questionnaireIds = new HashSet<>();
        for (String questionnaireId : mongoTemplate.getCollection(collectionName).distinct(
                "questionnaireId",
                String.class)) {
            questionnaireIds.add(questionnaireId);
        }
        return questionnaireIds;
    }
}
