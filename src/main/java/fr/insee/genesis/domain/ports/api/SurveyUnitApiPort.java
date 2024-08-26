package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.dtos.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitId;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public interface SurveyUnitApiPort {

    void saveSurveyUnits(List<SurveyUnitDto> suList);

    List<SurveyUnitDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitDto> findByIdUE(String idUE);

    Stream<SurveyUnitDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitDto> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire);

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
