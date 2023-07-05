package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;

import java.util.List;

public class SurveyUnitUpdateImpl implements SurveyUnitUpdateApiPort {

    private SurveyUnitUpdatePersistencePort surveyUnitUpdatePersistencePort;

    public SurveyUnitUpdateImpl(SurveyUnitUpdatePersistencePort surveyUnitUpdatePersistencePort) {
        this.surveyUnitUpdatePersistencePort = surveyUnitUpdatePersistencePort;
    }

    @Override
    public void saveSurveyUnits(List<SurveyUnitUpdateDto> suDtos) {
        surveyUnitUpdatePersistencePort.saveAll(suDtos);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest) {
        return surveyUnitUpdatePersistencePort.findByIds(idUE, idQuest);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdUE(String idUE) {
        return surveyUnitUpdatePersistencePort.findByIdUE(idUE);
    }

    @Override
    public List<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.findByIdQuestionnaire(idQuestionnaire);
    }


}
