package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateDocumentMapper;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateMongoDBRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Slf4j
@Service
@ConditionalOnProperty(name = "fr.insee.genesis.persistence.implementation", havingValue = "mongodb")
public class SurveyUnitUpdateMongoAdapter implements SurveyUnitUpdatePersistencePort {

	@Autowired
	private SurveyUnitUpdateMongoDBRepository mongoRepository;

	@Override
	public void saveAll(List<SurveyUnitUpdateDto> suListDto) {
		log.info("Saving {} survey units updates", suListDto.size());
		List<SurveyUnitUpdateDocument> suList = SurveyUnitUpdateDocumentMapper.INSTANCE.listDtoToListDocument(suListDto);
		mongoRepository.insert(suList);
		log.info("Survey units updates saved");
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUEAndIdQuestionnaire(idUE, idQuest);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdUE(idUE);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}

	@Override
	public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
		List<SurveyUnitUpdateDocument> surveyUnitsUpdate = mongoRepository.findByIdQuestionnaire(idQuestionnaire);
		return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateDocumentMapper.INSTANCE.listDocumentToListDto(surveyUnitsUpdate);
	}
}
