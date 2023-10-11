package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.ExternalVariableDto;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableStateDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;

import java.util.ArrayList;
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

    @Override
    public List<SurveyUnitUpdateDto> findLatestByIds(String idUE, String idQuest) {
        List<SurveyUnitUpdateDto> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtos = surveyUnitUpdatePersistencePort.findByIds(idUE, idQuest);
        //Sorting update by date (lastest updates first)
        surveyUnitUpdateDtos.sort((o1, o2) -> o2.getDate().compareTo(o1.getDate()));

        //We had all the variables of the oldest update
        latestUpdatesbyVariables.add(surveyUnitUpdateDtos.get(0));

        //We keep the name of already added variables to skip them in older updates
        List<String> addedVariables = new ArrayList<>();
        SurveyUnitUpdateDto latestUpdate = surveyUnitUpdateDtos.get(0);

        latestUpdate.getVariablesUpdate().forEach(variableStateDto -> addedVariables.add(variableStateDto.getIdVar()));
        latestUpdate.getExternalVariables().forEach(externalVariableDto -> addedVariables.add(externalVariableDto.getIdVar()));

        surveyUnitUpdateDtos.forEach(surveyUnitUpdateDto -> {
            List<VariableStateDto> variablesToKeep = new ArrayList<>();
            List<ExternalVariableDto> externalToKeep = new ArrayList<>();
            // We iterate over the variables of the update and add them to the list if they are not already added
            surveyUnitUpdateDto.getVariablesUpdate().forEach(variableStateDto -> {
                if (!addedVariables.contains(variableStateDto.getIdVar())){
                    variablesToKeep.add(variableStateDto);
                    addedVariables.add(variableStateDto.getIdVar());
                }
            });
            if (surveyUnitUpdateDto.getExternalVariables() != null){
                surveyUnitUpdateDto.getExternalVariables().forEach(externalVariableDto -> {
                    if (!addedVariables.contains(externalVariableDto.getIdVar())) {
                        externalToKeep.add(externalVariableDto);
                        addedVariables.add(externalVariableDto.getIdVar());
                    }
                });
            }

            // If there are new variables, we add the update to the list of latest updates
            if (!variablesToKeep.isEmpty() || !externalToKeep.isEmpty()){
                surveyUnitUpdateDto.setVariablesUpdate(variablesToKeep);
                surveyUnitUpdateDto.setExternalVariables(externalToKeep);
                latestUpdatesbyVariables.add(surveyUnitUpdateDto);
            }
        });

        return latestUpdatesbyVariables;
    }

    @Override
    public List<SurveyUnitDto> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitUpdatePersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        return surveyUnits.stream().distinct().toList();
    }




}
