package fr.insee.genesis.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.mongodb.client.MongoCollection;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.bson.Document;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

@Slf4j
@Service
@Qualifier("surveyUnitMongoAdapter")
public class SurveyUnitMongoAdapter implements SurveyUnitPersistencePort {

    public static final String QUESTIONNAIRE_ID = "questionnaireId";
    private final SurveyUnitMongoDBRepository mongoRepository;
	private final MongoTemplate mongoTemplate;

	@Autowired
	public SurveyUnitMongoAdapter(SurveyUnitMongoDBRepository mongoRepository, MongoTemplate mongoTemplate) {
		this.mongoRepository = mongoRepository;
		this.mongoTemplate = mongoTemplate;
	}

	@Override
	public void saveAll(List<SurveyUnitModel> surveyUnitModels) {
		List<SurveyUnitDocument> suList = SurveyUnitDocumentMapper.INSTANCE.listModelToListDocument(surveyUnitModels);
		mongoRepository.insert(suList);
	}

	@Override
	public List<SurveyUnitModel> findByIds(String interrogationId, String collectionInstrumentId) {
		List<SurveyUnitDocument> results = new ArrayList<>();
		results.addAll(mongoRepository.findByInterrogationIdAndCollectionInstrumentId(interrogationId, collectionInstrumentId));
		// To ensure compatibility with older documents (with questionnaireId instead of collectionInstrumentId)
		results.addAll(mongoRepository.findByInterrogationIdAndQuestionnaireId(interrogationId, collectionInstrumentId));
		return results.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(results);
	}


	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Override
	public List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findBySetOfIdsAndQuestionnaireIdAndMode(questionnaireId, mode, interrogationIdSet);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}
	//========= OPTIMISATIONS PERFS (END) ==========

	@Override
	public List<SurveyUnitModel> findByInterrogationId(String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findByInterrogationId(questionnaireId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> questionnaireIds, String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits= new ArrayList<>();
		questionnaireIds.forEach(su -> {
			List<SurveyUnitDocument> docs = mongoRepository.findByInterrogationIdAndQuestionnaireId(su.getInterrogationId(), questionnaireId);
			surveyUnits.addAll(docs);
		});
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}


	@Override
	public Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId) {
		Stream<SurveyUnitDocument> surveyUnits = mongoRepository.findByQuestionnaireId(questionnaireId);
		return surveyUnits.map(SurveyUnitDocumentMapper.INSTANCE::documentToModel);
	}

	@Override
	public Long deleteByCollectionInstrumentId(String collectionInstrumentId) {
		Long countDeleted = mongoRepository.deleteByCollectionInstrumentId(collectionInstrumentId);
		// If the responses are in the old format (previous to modele filiere)
		countDeleted += mongoRepository.deleteByQuestionnaireId(collectionInstrumentId);
		return countDeleted;
	}

	@Override
	public long count() {
		return mongoRepository.count();
	}

	@Override
	public Set<String> findQuestionnaireIdsByCampaignId(String campaignId){
		Set<String> mongoResponse =
				mongoRepository.findQuestionnaireIdsByCampaignId(campaignId);

		//Extract questionnaireIds from JSON response
        return extractQuestionnaireIdsFromJson(mongoResponse);
	}

	//========= OPTIMISATIONS PERFS (START) ==========
	/**
	 * @author Adrien Marchal
	 */
	@Override
	public Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId){
		Set<String> mongoResponse =
				mongoRepository.findQuestionnaireIdsByCampaignIdV2(campaignId);

		//Extract questionnaireIds from JSON response
        return extractQuestionnaireIdsFromJson(mongoResponse);
	}

    private static @NotNull Set<String> extractQuestionnaireIdsFromJson(Set<String> mongoResponse) {
        Set<String> questionnaireIds = new HashSet<>();
        for(String line : mongoResponse){
            ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
            try{
                JsonNode jsonNode = objectMapper.readTree(line);
                questionnaireIds.add(jsonNode.get(QUESTIONNAIRE_ID).asText());
            }catch (JsonProcessingException e){
                log.error(e.getMessage());
            }
        }
        return questionnaireIds;
    }
    //========= OPTIMISATIONS PERFS (END) ==========

	@Override
	public Set<String> findDistinctCampaignIds() {
		Set<String> campaignIds = new HashSet<>();
		for(String campaignId : mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME).distinct("campaignId",
				String.class)){
			campaignIds.add(campaignId);
		}
		return campaignIds;
	}

	@Override
	public List<SurveyUnitModel> findInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
		List<SurveyUnitDocument> results =  new ArrayList<>();
		results.addAll(mongoRepository.findInterrogationIdsByCollectionInstrumentId(collectionInstrumentId));
		results.addAll(mongoRepository.findInterrogationIdsByQuestionnaireId(collectionInstrumentId));
		return results.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(results);
	}

	@Override
	public List<SurveyUnitModel> findInterrogationIdsByQuestionnaireIdAndDateAfter(String questionnaireId, LocalDateTime since) {
		List<SurveyUnitDocument> results =  new ArrayList<>();
		results.addAll(mongoRepository.findInterrogationIdsByQuestionnaireIdAndDateAfter(questionnaireId, since));
		results.addAll(mongoRepository.findInterrogationIdsByCollectionInstrumentIdAndDateAfter(questionnaireId, since));
		return results.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(results);
	}

	//========== OPTIMISATIONS PERFS (START) ===========
	@Override
	public long countByCollectionInstrumentId(String collectionInstrumentId) {
		return mongoRepository.countByQuestionnaireId(collectionInstrumentId);
	}

	@Override
	public List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findPageableInterrogationIdsByQuestionnaireId(questionnaireId, skip, limit);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnitModel> findModesByCampaignIdV2(String campaignId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findModesByCampaignIdV2(campaignId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);

	}

	@Override
	public List<SurveyUnitModel> findModesByQuestionnaireIdV2(String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findModesByQuestionnaireIdV2(questionnaireId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}
	//========== OPTIMISATIONS PERFS (END) ============

	@Override
	public List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findInterrogationIdsByCampaignId(campaignId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);

	}

	public long countByCampaignId(String campaignId){
		return mongoRepository.countByCampaignId(campaignId);
	}

	@Override
	public Set<String> findDistinctQuestionnairesAndCollectionInstrumentIds() {
		Set<String> questionnaireIds = new HashSet<>();
		// Id selection is executed by mongoDB
		MongoCollection<Document> collection = mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME);
		collection.distinct(QUESTIONNAIRE_ID, String.class).into(questionnaireIds);
		collection.distinct("collectionInstrumentId", String.class).into(questionnaireIds);
		questionnaireIds.remove(null);
		return questionnaireIds;
	}

	@Override
	public Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId) {
		List<String> mongoResponse =
				mongoRepository.findCampaignIdsByQuestionnaireId(questionnaireId).stream().distinct().toList();

		//Extract idCampaigns from JSON response
		Set<String> campaignIds = new HashSet<>();
		for(String line : mongoResponse){
			ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();
			try{
				JsonNode jsonNode = objectMapper.readTree(line);
				campaignIds.add(jsonNode.get("campaignId").asText());
			}catch (JsonProcessingException e){
				log.error(e.getMessage());
			}
		}

		return campaignIds;
	}

	@Override
	public long countByQuestionnaireId(String questionnaireId) {
		return mongoRepository.countByQuestionnaireId(questionnaireId);
	}
}
