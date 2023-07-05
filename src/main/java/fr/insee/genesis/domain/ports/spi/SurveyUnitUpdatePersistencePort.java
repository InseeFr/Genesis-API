package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;

public interface SurveyUnitUpdatePersistencePort {

    void saveAll(List<SurveyUnitUpdateDto> suList);

    List<SurveyUnitUpdateDto> findByIds(String idUE, String idQuest);

    List<SurveyUnitUpdateDto> findByIdUE(String idUE);

    List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire);


}
