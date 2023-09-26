package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.infrastructure.model.entity.SurveyUnitUpdate;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateMapper;
import fr.insee.genesis.infrastructure.repository.SurveyUnitUpdateJPARepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
@Slf4j
@ConditionalOnProperty(name = "fr.insee.genesis.persistence.implementation", havingValue = "postgresql")
public class SurveyUnitUpdateJpaAdapter implements SurveyUnitUpdatePersistencePort {

    @Autowired
    private SurveyUnitUpdateJPARepository surveyUnitUpdateRepository;

    @PersistenceContext
    private EntityManager entityManager;
    private static final int BATCH_SIZE = 10;

    @Override
    public void saveAll(List<SurveyUnitUpdateDto> suListDto) {
        log.info("Saving {} survey units updates", suListDto.size());
        List<SurveyUnitUpdate> suList = SurveyUnitUpdateMapper.INSTANCE.listDtoToListEntity(suListDto);
        for (int i = 0; i < suList.size(); i++) {
            if(i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
                log.info("Flushing and clearing entity manager");
            }
            entityManager.persist(suList.get(i));
        }
        log.info("Survey units updates saved");
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest) {
        List<SurveyUnitUpdate> surveyUnitsUpdate = surveyUnitUpdateRepository.findByIdUEAndIdQuestionnaire(idUE, idQuest);
        return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateMapper.INSTANCE.listEntityToListDto(surveyUnitsUpdate);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
        List<SurveyUnitUpdate> surveyUnitsUpdate = surveyUnitUpdateRepository.findByIdUE(idUE);
        return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateMapper.INSTANCE.listEntityToListDto(surveyUnitsUpdate);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitUpdate> surveyUnitsUpdate = surveyUnitUpdateRepository.findByIdQuestionnaire(idQuestionnaire);
        return surveyUnitsUpdate.isEmpty() ? null : SurveyUnitUpdateMapper.INSTANCE.listEntityToListDto(surveyUnitsUpdate);
    }



}
