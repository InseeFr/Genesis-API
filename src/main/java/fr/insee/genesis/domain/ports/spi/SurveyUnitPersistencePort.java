package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitDto> suList);

    List<SurveyUnitDto> findByIds(String idUE, String idQuest);

    List<SurveyUnitDto> findByIdUE(String idUE);

    List<SurveyUnitDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire);

    Stream<SurveyUnitDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findIdUEsByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long count();

    Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countByIdCampaign(String idCampaign);

    Set<String> findDistinctIdQuestionnaires();

    Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire);
}
