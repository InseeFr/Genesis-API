package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String interrogationId, String questionnaireId);

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId);

    Long deleteByQuestionnaireId(String questionnaireId);

    long count();

    Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

    Set<String> findDistinctCampaignIds();

    long countByCampaignId(String campaignId);

    Set<String> findDistinctQuestionnaireIds();

    Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);
}
