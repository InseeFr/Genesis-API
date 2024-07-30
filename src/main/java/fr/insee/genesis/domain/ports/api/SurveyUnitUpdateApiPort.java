package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.dtos.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;
import java.util.Set;
import java.util.stream.Stream;


public interface SurveyUnitUpdateApiPort {

    void saveSurveyUnits(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitUpdateDto> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest);

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
