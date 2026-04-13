package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.*;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.InterrogationInfo;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIdsInterrogationAndCollectionInstrument(String interrogationId, String collectionInstrumentId);

    List<SurveyUnitModel> findByIdsUsualSurveyUnitAndCollectionInstrument(String usualSurveyUnitId, String collectionInstrumentId);

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    List<SurveyUnitModel> findLatestByIdAndByCollectionInstrumentId(String interrogationId, String collectionInstrumentId);

    SurveyUnitSimplifiedDto findSimplified(
            String collectionInstrumentId,
            String interrogationId,
            Mode mode,
            Instant recordedBefore
    );

    List<SurveyUnitSimplifiedDto> findSimplifiedList(
            String collectionInstrumentId,
            List<InterrogationId> interrogationIds,
            Instant before
    );


    //========= OPTIMISATIONS PERFS (START) ==========
    List<List<SurveyUnitModel>> findLatestByIdAndByQuestionnaireIdAndModeOrdered(String questionnaireId, String mode, List<InterrogationId> interrogationIds);
    //========= OPTIMISATIONS PERFS (END) ==========

    SurveyUnitDto findLatestValuesByStateByIdAndByCollectionInstrumentId(String interrogationId, String collectionInstrumentId) throws GenesisException;

    List<SurveyUnitModel> findInterrogationIdsAndModesByQuestionnaireId(String questionnaireId);

    List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<InterrogationInfo> findDistinctInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);

    List<InterrogationInfo> findDistinctInterrogationIdsByCollectionInstrumentIdAndSince(String collectionInstrumentId, Instant since);

    List<InterrogationId> findDistinctInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(String collectionInstrumentId, Instant start, Instant end);

    //========= OPTIMISATIONS PERFS (START) ==========
    long countResponsesByCollectionInstrumentId(String questionnaireId);

    List<InterrogationId> findDistinctPageableInterrogationIdsByQuestionnaireId(String questionnaireId,
                                                                                long totalSize, long blockSize, long page);

    List<Mode> findModesByQuestionnaireIdV2(String questionnaireId);
    //========= OPTIMISATIONS PERFS (END) ==========

    List<Mode> findModesByCollectionInstrumentId(String collectionInstrumentId);

    List<Mode> findModesByCampaignId(String campaignId);

    //========= OPTIMISATIONS PERFS (START) ==========
    List<Mode> findModesByCampaignIdV2(String campaignId);
    //========= OPTIMISATIONS PERFS (END) ==========

    Long deleteByCollectionInstrumentId(String collectionInstrumentId);

    Long deleteByQuestionnaireIdAndInterrogationIds(
            String questionnaireId,
            Set<String> interrogationIds
    );

    long countResponses();

    Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId);
    //========= OPTIMISATIONS PERFS (END) ==========

    @Deprecated
    Set<String> findDistinctCampaignIds();

    @Deprecated
    long countResponsesByCampaignId(String campaignId);

    Set<String> findDistinctQuestionnairesAndCollectionInstrumentIds();

    List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires();

    List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns();

    List<SurveyUnitModel> parseEditedVariables(SurveyUnitInputDto surveyUnitInputDto,
                                         String userIdentifier,
                                         VariablesMap variablesMap) throws GenesisException;

    String findQuestionnaireIdByInterrogationId(String interrogationId) throws GenesisException;

    Set<String> findCampaignIdsFrom(SurveyUnitInputDto dto);

    long countResponsesByQuestionnaireId(String questionnaireId);

    long countDistinctInterrogationIdsByQuestionnaireAndCollectionInstrumentId(String id);
}