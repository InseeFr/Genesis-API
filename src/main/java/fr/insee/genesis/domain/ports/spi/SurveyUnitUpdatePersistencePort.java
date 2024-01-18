package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;

public interface SurveyUnitUpdatePersistencePort {

    void saveAll(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire);

    List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);

    List<SurveyUnitDto> findIdUEsByIdQuestionnaire(String idQuestionnaire);

    Long deleteByIdQuestionnaire(String idQuestionnaire);


}
