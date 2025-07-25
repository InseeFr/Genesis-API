package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String interrogationId, String questionnaireId);

    /**
     * @author Adrien Marchal
     */
    List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    /**
     * !!!WARNING!!! : A CALL WITH THIS ENDPOINT ON A BIG COLLECTION (> 300k) MAY KILL THE GENESIS-API APP.!!!
     */
    List<SurveyUnitModel> findInterrogationIdsByQuestionnaireId(String questionnaireId);

    long countInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit);

    List<SurveyUnitModel> findModesByCampaignId(String campaignId);

    List<SurveyUnitModel> findModesByQuestionnaireId(String questionnaireId);


    Long deleteByQuestionnaireId(String questionnaireId);

    long count();


    /**
     * @author Adrien Marchal
     */
    Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

    Set<String> findDistinctCampaignIds();

    long countByCampaignId(String campaignId);

    Set<String> findDistinctQuestionnaireIds();

    Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);
}
