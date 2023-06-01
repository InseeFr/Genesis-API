package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;

import java.util.List;


public interface SurveyUnitUpdateApiPort {

    void saveSurveyUnits(List<SurveyUnitUpdateDto> suList);

}
