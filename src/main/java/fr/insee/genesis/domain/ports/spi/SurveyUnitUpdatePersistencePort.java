package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitUpdatePersistencePort {

    void saveAll(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire);

    Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findIdUEsByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long count();

    List<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countByIdCampaign(String idCampaign);
}
