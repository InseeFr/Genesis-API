package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.mappers.RawResponseDocumentMapper;
import fr.insee.genesis.infrastructure.repository.RawResponseRepository;
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

@Service
@Slf4j
@Qualifier("rawResponseMongoAdapter")
public class RawResponseMongoAdapter implements RawResponsePersistencePort {

    private final RawResponseRepository repository;
    private final MongoTemplate mongoTemplate;

    public RawResponseMongoAdapter(RawResponseRepository repository, MongoTemplate mongoTemplate) {
        this.repository = repository;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public List<RawResponseModel> findRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList) {
        List<RawResponseDocument> rawDataDocs = repository.findByCollectionInstrumentIdAndModeAndInterrogationIdList(collectionInstrumentId, mode.getJsonName(), interrogationIdList);
        return RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(rawDataDocs);
    }

    @Override
    public List<RawResponseModel> findRawResponsesByInterrogationID(String interrogationId) {
        List<RawResponseDocument> rawResponseDocumentList = repository.findByInterrogationId(interrogationId);
        return RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(rawResponseDocumentList);
    }

    @Override
    public void updateProcessDates(String collectionInstrumentId, Set<String> interrogationIds) {
        mongoTemplate.updateMulti(
                Query.query(Criteria.where("collectionInstrumentId").is(collectionInstrumentId).and("interrogationId").in(interrogationIds))
                , new Update().set("processDate", LocalDateTime.now())
                , Constants.MONGODB_RAW_RESPONSES_COLLECTION_NAME
        );
    }

    @Override
    public List<String> getUnprocessedCollectionIds() {
        return repository.findDistinctCollectionInstrumentIdByProcessDateIsNull();
    }

    @Override
    public Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
        // We remove duplicate ids
        return new HashSet<>(repository.findInterrogationIdByCollectionInstrumentIdAndProcessDateIsNull(collectionInstrumentId));
    }

    @Override
    public Page<RawResponse> findByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable) {
        Page<RawResponseDocument> rawDataDocs = repository.findByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
        List<RawResponse> modelList = RawResponseDocumentMapper.INSTANCE.listDocumentToListModel(rawDataDocs.getContent());
        return new PageImpl<>(modelList, rawDataDocs.getPageable(), rawDataDocs.getTotalElements());
    }
}
