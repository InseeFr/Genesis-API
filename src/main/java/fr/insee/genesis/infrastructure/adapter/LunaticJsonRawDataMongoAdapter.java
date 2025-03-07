package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistancePort;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import fr.insee.genesis.infrastructure.repository.LunaticJsonMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Slf4j
@Service
@Qualifier("lunaticJsonMongoAdapter")
public class LunaticJsonRawDataMongoAdapter implements LunaticJsonRawDataPersistancePort {

	private final LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository;
	private final MongoTemplate mongoTemplate;

	@Autowired
	public LunaticJsonRawDataMongoAdapter(LunaticJsonMongoDBRepository lunaticJsonMongoDBRepository, MongoTemplate mongoTemplate) {
		this.lunaticJsonMongoDBRepository = lunaticJsonMongoDBRepository;
        this.mongoTemplate = mongoTemplate;
    }

	@Override
	public void save(LunaticJsonRawDataModel lunaticJsonRawDataModel) {
		LunaticJsonDataDocument document = LunaticJsonDocumentMapper.INSTANCE
				.modelToDocument(lunaticJsonRawDataModel);
		lunaticJsonMongoDBRepository.insert(document);
	}

	@Override
	public List<LunaticJsonRawDataModel> getAllUnprocessedData() {
		return LunaticJsonDocumentMapper.INSTANCE.listDocumentToListModel(lunaticJsonMongoDBRepository.findByNullProcessDate());
	}

	@Override
	public List<LunaticJsonDataDocument> findRawData(String campaignName, Mode mode, List<String> interrogationIdList) {
		return lunaticJsonMongoDBRepository.findModesByCampaignIdAndByModeAndinterrogationIdIninterrogationIdList(campaignName, mode, interrogationIdList);
	}

	@Override
	public void updateProcessDates(String campaignId, Set<String> interrogationIds) {
		mongoTemplate.updateMulti(
				Query.query(Criteria.where("campaignId").is(campaignId).and("interrogationId").in(interrogationIds))
				, new Update().set("processDate", LocalDateTime.now())
				, Constants.MONGODB_LUNATIC_RAWDATA_COLLECTION_NAME
		);
	}
}
