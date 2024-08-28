package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnit> suList);

    List<SurveyUnit> findByIds(String idUE, String idQuest);

    List<SurveyUnit> findByIdUE(String idUE);

    List<SurveyUnit> findByIdUEsAndIdQuestionnaire(List<SurveyUnit> idUEs, String idQuestionnaire);

    Stream<SurveyUnit> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnit> findIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnit> findIdUEsByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long count();

    Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countByIdCampaign(String idCampaign);

    Set<String> findDistinctIdQuestionnaires();

    Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire);
}
