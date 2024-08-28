package fr.insee.genesis.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
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
public class SurveyUnitMongoAdapter implements SurveyUnitPersistencePort {

	@Autowired
	private SurveyUnitMongoDBRepository mongoRepository;

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public void saveAll(List<SurveyUnit> suListDto) {
		List<SurveyUnitDocument> suList = SurveyUnitDocumentMapper.INSTANCE.listModelToListDocument(suListDto);
		mongoRepository.insert(suList);
	}

	@Override
	public List<SurveyUnit> findByIds(String idUE, String idQuest) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findByIdUEAndIdQuestionnaire(idUE, idQuest);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnit> findByIdUE(String idUE) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findByIdUE(idUE);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnit> findByIdUEsAndIdQuestionnaire(List<SurveyUnit> idUEs, String idQuestionnaire) {
		List<SurveyUnitDocument> surveyUnits= new ArrayList<>();
		// TODO: 18-10-2023 : find a way to do this in one query
		idUEs.forEach(su -> {
			List<SurveyUnitDocument> docs = mongoRepository.findByIdUEAndIdQuestionnaire(su.getIdUE(), idQuestionnaire);
			surveyUnits.addAll(docs);
		});
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}


	@Override
	public Stream<SurveyUnit> findByIdQuestionnaire(String idQuestionnaire) {
		Stream<SurveyUnitDocument> surveyUnits = mongoRepository.findByIdQuestionnaire(idQuestionnaire);
		return surveyUnits.map(SurveyUnitDocumentMapper.INSTANCE::documentToModel);
		//return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnits);
	}

	@Override
	public Long deleteByIdQuestionnaire(String idQuestionnaire) {
		return mongoRepository.deleteByIdQuestionnaire(idQuestionnaire);
	}

	@Override
	public long count() {
		return mongoRepository.count();
	}

	@Override
	public Set<String> findIdQuestionnairesByIdCampaign(String idCampaign){
		Set<String> mongoResponse =
				mongoRepository.findIdQuestionnairesByIdCampaign(idCampaign);

		//Extract idQuestionnaires from JSON response
		Set<String> idQuestionnaires = new HashSet<>();
		for(String line : mongoResponse){
			ObjectMapper objectMapper = new ObjectMapper();
			try{
				JsonNode jsonNode = objectMapper.readTree(line);
				idQuestionnaires.add(jsonNode.get("idQuestionnaire").asText());
			}catch (JsonProcessingException e){
				log.error(e.getMessage());
			}
		}

		return idQuestionnaires;
	}

	@Override
	public Set<String> findDistinctIdCampaigns() {
		Set<String> idCampaigns = new HashSet<>();
		for(String idCampaign : mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME).distinct("idCampaign",
				String.class)){
			idCampaigns.add(idCampaign);
		}
		return idCampaigns;
	}

	@Override
	public List<SurveyUnit> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findIdUEsByIdQuestionnaire(idQuestionnaire);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);
	}

	@Override
	public List<SurveyUnit> findIdUEsByIdCampaign(String idCampaign) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findIdUEsByIdCampaign(idCampaign);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnits);

	}

	public long countByIdCampaign(String idCampaign){
		return mongoRepository.countByIdCampaign(idCampaign);
	}

	@Override
	public Set<String> findDistinctIdQuestionnaires() {
		Set<String> idQuestionnaires = new HashSet<>();
		for(String idQuestionnaire : mongoTemplate.getCollection(Constants.MONGODB_RESPONSE_COLLECTION_NAME).distinct(
				"idQuestionnaire",
				String.class)){
			idQuestionnaires.add(idQuestionnaire);
		}
		return idQuestionnaires;
	}

	@Override
	public Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire) {
		List<String> mongoResponse =
				mongoRepository.findIdCampaignsByIdQuestionnaire(idQuestionnaire).stream().distinct().toList();

		//Extract idCampagigns from JSON response
		Set<String> idCampaigns = new HashSet<>();
		for(String line : mongoResponse){
			ObjectMapper objectMapper = new ObjectMapper();
			try{
				JsonNode jsonNode = objectMapper.readTree(line);
				idCampaigns.add(jsonNode.get("idCampaign").asText());
			}catch (JsonProcessingException e){
				log.error(e.getMessage());
			}
		}

		return idCampaigns;
	}
}
