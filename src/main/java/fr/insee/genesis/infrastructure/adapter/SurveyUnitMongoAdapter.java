package fr.insee.genesis.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Service;

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

	private SurveyUnitMongoDBRepository mongoRepository;
	private MongoTemplate mongoTemplate;

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
	public List<SurveyUnitModel> findByIds(String interrogationId, String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findByInterrogationIdAndQuestionnaireId(interrogationId, questionnaireId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnitModel> findByInterrogationId(String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findByInterrogationId(questionnaireId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> questionnaireIds, String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits= new ArrayList<>();
		// TODO: 18-10-2023 : find a way to do this in one query
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
	public Long deleteByQuestionnaireId(String questionnaireId) {
		return mongoRepository.deleteByQuestionnaireId(questionnaireId);
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
		Set<String> questionnaireIds = new HashSet<>();
		for(String line : mongoResponse){
			ObjectMapper objectMapper = new ObjectMapper();
			try{
				JsonNode jsonNode = objectMapper.readTree(line);
				questionnaireIds.add(jsonNode.get("questionnaireId").asText());
			}catch (JsonProcessingException e){
				log.error(e.getMessage());
			}
		}

		return questionnaireIds;
	}

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
	public List<SurveyUnitModel> findInterrogationIdsByQuestionnaireId(String questionnaireId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findInterrogationIdsByQuestionnaireId(questionnaireId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findInterrogationIdsByCampaignId(campaignId);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);

	}

	public long countByCampaignId(String campaignId){
		return mongoRepository.countByCampaignId(campaignId);
	}

	@Override
	public Set<String> findDistinctQuestionnaireIds() {
		Set<String> questionnaireIds = new HashSet<>();
		for(String questionnaireId : mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME).distinct(
				"questionnaireId",
				String.class)){
			questionnaireIds.add(questionnaireId);
		}
		return questionnaireIds;
	}

	@Override
	public Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId) {
		List<String> mongoResponse =
				mongoRepository.findCampaignIdsByQuestionnaireId(questionnaireId).stream().distinct().toList();

		//Extract idCampagigns from JSON response
		Set<String> campaignIds = new HashSet<>();
		for(String line : mongoResponse){
			ObjectMapper objectMapper = new ObjectMapper();
			try{
				JsonNode jsonNode = objectMapper.readTree(line);
				campaignIds.add(jsonNode.get("campaignId").asText());
			}catch (JsonProcessingException e){
				log.error(e.getMessage());
			}
		}

		return campaignIds;
	}
}
