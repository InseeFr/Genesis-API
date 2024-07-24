package fr.insee.genesis.infrastructure.adapter;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateDocumentMapper;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateMongoDBRepository;
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
public class SurveyUnitUpdateMongoAdapter implements SurveyUnitUpdatePersistencePort {

	@Autowired
	private SurveyUnitUpdateMongoDBRepository mongoRepository;

	@Autowired
	MongoTemplate mongoTemplate;

	@Override
	public void saveAll(List<SurveyUnitUpdateDto> suListDto) {
		List<SurveyUnitUpdateDocument> suList = SurveyUnitUpdateDocumentMapper.INSTANCE.listDtoToListDocument(suListDto);
		mongoRepository.insert(suList);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUEAndIdQuestionnaire(idUE, idQuest);
		return surveyUnitsUpdate.isEmpty() ? Collections.emptyList() : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUE(idUE);
		return surveyUnitsUpdate.isEmpty() ? Collections.emptyList() : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate= new ArrayList<>();
		// TODO: 18-10-2023 : find a way to do this in one query
		idUEs.forEach(su -> {
			List<SurveyUnitUpdateDocument> docs = mongoRepository.findByIdUEAndIdQuestionnaire(su.getIdUE(), idQuestionnaire);
			surveyUnitsUpdate.addAll(docs);
		});
		return surveyUnitsUpdate.isEmpty() ? Collections.emptyList() : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}


	@Override
	public Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
		Stream<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdQuestionnaire(idQuestionnaire);
		return surveyUnitsUpdate.map(SurveyUnitUpdateDocumentMapper.INSTANCE::documentToDto);
		//return surveyUnitsUpdate.isEmpty() ? Collections.emptyList() : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
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
	public List<String> findIdQuestionnairesByIdCampaign(String idCampaign){
		List<String> mongoResponse = mongoRepository.findIdQuestionnairesByIdCampaign(idCampaign).stream().distinct().toList();

		//Extract idQuestionnaires from JSON response
		List<String> idQuestionnaires = new ArrayList<>();
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
	public List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findIdUEsByIdQuestionnaire(idQuestionnaire);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnits);
	}

	@Override
	public List<SurveyUnitDto> findIdUEsByIdCampaign(String idCampaign) {
		List<SurveyUnitDocument> surveyUnits = mongoRepository.findIdUEsByIdCampaign(idCampaign);
		return surveyUnits.isEmpty() ? Collections.emptyList() : SurveyUnitDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnits);

	}

	public long countByIdCampaign(String idCampaign){
		return mongoRepository.countByIdCampaign(idCampaign);
	}
}
