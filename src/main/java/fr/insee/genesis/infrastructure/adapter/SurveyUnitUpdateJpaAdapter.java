package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;
import fr.insee.genesis.infrastructure.entity.SurveyUnitUpdate;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateMapper;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Transactional
public class SurveyUnitUpdateJpaAdapter implements SurveyUnitUpdatePersistencePort {

    @PersistenceContext
    private EntityManager entityManager;
    private static final int BATCH_SIZE = 100;

    @Override
    public void saveAll(List<SurveyUnitUpdateDto> suListDto) {
        List<SurveyUnitUpdate> suList = SurveyUnitUpdateMapper.INSTANCE.listDtoToListEntity(suListDto);
        for (int i = 0; i < suList.size(); i++) {
            if(i > 0 && i % BATCH_SIZE == 0) {
                entityManager.flush();
                entityManager.clear();
            }
            entityManager.persist(suList.get(i));
        }
    }
}
