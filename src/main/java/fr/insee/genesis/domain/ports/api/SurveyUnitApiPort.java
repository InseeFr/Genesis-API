package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIdsInterrogationAndQuestionnaire(String interrogationId, String questionnaireId);

    List<SurveyUnitModel> findByInterrogationId(String interrogationId);

    Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId);

    List<SurveyUnitModel> findLatestByIdAndByQuestionnaireId(String interrogationId, String questionnaireId);

    //========= OPTIMISATIONS PERFS (START) ==========
    List<List<SurveyUnitModel>> findLatestByIdAndByQuestionnaireIdAndModeOrdered(String questionnaireId, String mode, List<InterrogationId> interrogationIds);
    //========= OPTIMISATIONS PERFS (END) ==========

    SurveyUnitDto findLatestValuesByStateByIdAndByQuestionnaireId(String interrogationId, String questionnaireId);

    List<SurveyUnitModel> findInterrogationIdsAndModesByQuestionnaireId(String questionnaireId);

    List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireId(String questionnaireId);

    //========= OPTIMISATIONS PERFS (START) ==========
    long countInterrogationIdsByQuestionnaireId(String questionnaireId);

    List<InterrogationId> findDistinctPageableInterrogationIdsByQuestionnaireId(String questionnaireId,
                                                                                long totalSize, long blockSize, long page);

    List<Mode> findModesByQuestionnaireIdV2(String questionnaireId);
    //========= OPTIMISATIONS PERFS (END) ==========

    List<Mode> findModesByQuestionnaireId(String questionnaireId);

    List<Mode> findModesByCampaignId(String campaignId);

    //========= OPTIMISATIONS PERFS (START) ==========
    List<Mode> findModesByCampaignIdV2(String campaignId);
    //========= OPTIMISATIONS PERFS (END) ==========

    Long deleteByQuestionnaireId(String questionnaireId);

    long countResponses();

    Set<String> findQuestionnaireIdsByCampaignId(String campaignId);

    //========= OPTIMISATIONS PERFS (START) ==========
    /**
     * @author Adrien Marchal
     */
    Set<String> findQuestionnaireIdsByCampaignIdV2(String campaignId);
    //========= OPTIMISATIONS PERFS (END) ==========

    Set<String> findDistinctCampaignIds();

    long countResponsesByCampaignId(String campaignId);

    Set<String> findDistinctQuestionnaireIds();

    List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires();

    List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns();

    List<SurveyUnitModel> parseEditedVariables(SurveyUnitInputDto surveyUnitInputDto,
                                         String userIdentifier,
                                         VariablesMap variablesMap) throws GenesisException;

    String findQuestionnaireIdByInterrogationId(String interrogationId) throws GenesisException;

    Set<String> findCampaignIdsFrom(SurveyUnitInputDto dto);
}