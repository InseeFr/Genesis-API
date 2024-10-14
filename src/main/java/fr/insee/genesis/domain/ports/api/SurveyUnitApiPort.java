package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.controller.dto.SurveyUnitId;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnitModel> suList);

    List<SurveyUnitModel> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitModel> findByIdUE(String idUE);

    Stream<SurveyUnitModel> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitModel> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest);
    SurveyUnitDto findLatestByIdAndByIdQuestionnaireLastestStates(String idUE, String idQuest);

    List<SurveyUnitModel> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire);

    List<Mode> findModesByIdQuestionnaire(String idQuestionnaire);

    List<Mode> findModesByIdCampaign(String idCampaign);

    Long deleteByIdQuestionnaire(String idQuestionnaire);

    long countResponses();

    Set<String> findIdQuestionnairesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdCampaigns();

    long countResponsesByIdCampaign(String idCampaign);

    Set<String> findDistinctIdQuestionnaires();

    List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires();

    List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns();
}
