package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String idUE, String idQuest);

    List<SurveyUnitModel> findByIdUE(String idUE);

    List<SurveyUnitModel> findByIdUEsAndIdQuestionnaire(List<SurveyUnitModel> idUEs, String idQuestionnaire);

    Stream<SurveyUnitModel> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitModel> findIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitModel> findIdUEsByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long count();

    Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countByIdCampaign(String idCampaign);

    Set<String> findDistinctIdQuestionnaires();

    Set<String> findIdCampaignsByIdQuestionnaire(String idQuestionnaire);
}
