package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.InterrogationInfo;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public interface SurveyUnitPersistencePort {

    void saveAll(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIds(String interrogationId, String collectionInstrumentId);

    List<SurveyUnitModel> findByUsualSurveyUnitAndCollectionInstrumentIds(String usualSurveyUnitId, String collectionInstrumentId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    List<SurveyUnitModel> findBySetOfIdsAndQuestionnaireIdAndMode(String questionnaireId, String mode, List<String> interrogationIdSet);
    //========= OPTIMISATIONS PERFS (START) ==========

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findByInterrogationIdsAndQuestionnaireId(List<SurveyUnitModel> interrogationIds, String questionnaireId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);

    List<SurveyUnitModel> findInterrogationIdsByQuestionnaireIdAndDateAfter(String questionnaireId, LocalDateTime since);

    List<InterrogationInfo> findInterrogationInfoByCollectionInstrumentId(String collectionInstrumentId);

    List<InterrogationInfo> searchInterrogations(String collectionInstrumentId, Instant start, Instant end);

    //======== OPTIMISATIONS PERFS (START) ========
    long countByCollectionInstrumentId(String collectionInstrumentId);

    List<SurveyUnitModel> findPageableInterrogationIdsByQuestionnaireId(String questionnaireId, Long skip, Long limit);

    List<SurveyUnitModel> findModesByCampaignIdV2(String campaignId);

    List<SurveyUnitModel> findModesByQuestionnaireIdV2(String questionnaireId);
    //======= OPTIMISATIONS PERFS (END) =========

    List<SurveyUnitModel> findInterrogationIdsByCampaignId(String campaignId);

    Long deleteByCollectionInstrumentId(String collectionInstrumentId);

    Long deleteByCollectionInstrumentIdAndInterrogationIds(
            String collectionInstrumentId,
            Set<String> interrogationIds
    );

    Long deleteByQuestionnaireIdAndInterrogationIds(
            String questionnaireId,
            Set<String> interrogationIds
    );

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

    Set<String> findDistinctQuestionnairesAndCollectionInstrumentIds();

    Set<String> findCampaignIdsByQuestionnaireId(String questionnaireId);

    long countByQuestionnaireId(String questionnaireId);

    long countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(String id);

}
