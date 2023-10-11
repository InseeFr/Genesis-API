package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;


public interface SurveyUnitUpdateApiPort {

    void saveSurveyUnits(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitUpdateDto> findLatestByIds(String idUE, String idQuest);

    List<SurveyUnitDto> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire);
}
