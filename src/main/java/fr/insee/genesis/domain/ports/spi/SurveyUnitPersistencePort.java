package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String interrogationId, String questionnaireId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);
    //========= OPTIMISATIONS PERFS (START) ==========

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsByQuestionnaireId(String questionnaireId);

    //======== OPTIMISATIONS PERFS (START) ========
    long countInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit);

    List<SurveyUnitModel> findModesByCampaignIdV2(String campaignId);

    List<SurveyUnitModel> findModesByQuestionnaireIdV2(String questionnaireId);
    //======= OPTIMISATIONS PERFS (END) =========

    List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId);

    Long deleteByQuestionnaireId(String questionnaireId);

    long count();

    Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId);
    //========= OPTIMISATIONS PERFS (END) ==========

    Set<String> findDistinctCampaignIds();

    long countByCampaignId(String campaignId);

    Set<String> findDistinctQuestionnaireIds();

    Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);
}
