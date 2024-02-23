package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.*;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Stream;

public class SurveyUnitUpdateImpl implements SurveyUnitUpdateApiPort {

    private final SurveyUnitUpdatePersistencePort surveyUnitUpdatePersistencePort;

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
    public List<SurveyUnitUpdateDto> findByIdUEsAndIdQuestionnaire(List<SurveyUnitDto> idUEs, String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.findByIdUEsAndIdQuestionnaire(idUEs, idQuestionnaire);
    }

    @Override
    public Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.findByIdQuestionnaire(idQuestionnaire);
    }

    @Override
    public List<SurveyUnitUpdateDto> findLatestByIdAndByMode(String idUE, String idQuest) {
        //In this method we want to get the latest update for each variable of a survey unit
        //But we need to separate the updates by mode
        //So we will calculate the latest state for a given collection mode
        List<SurveyUnitUpdateDto> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtos = surveyUnitUpdatePersistencePort.findByIds(idUE, idQuest);
        List<Mode> modes = getDistinctsModes(surveyUnitUpdateDtos);
        modes.forEach(mode ->{
            List<SurveyUnitUpdateDto> suByMode = new ArrayList<>();
            surveyUnitUpdateDtos.forEach(surveyUnitUpdateDto -> {
                if (surveyUnitUpdateDto.getMode().equals(mode)){
                    suByMode.add(surveyUnitUpdateDto);
                }
            });
            //Sorting update by date (lastest updates first by date of upload in database)
            suByMode.sort((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate()));
            //We had all the variables of the oldest update
            latestUpdatesbyVariables.add(suByMode.get(0));
            //We keep the name of already added variables to skip them in older updates
            List<String> addedVariables = new ArrayList<>();
            SurveyUnitUpdateDto latestUpdate = suByMode.get(0);

            latestUpdate.getCollectedVariables().forEach(variableStateDto -> addedVariables.add(variableStateDto.getIdVar()));
            latestUpdate.getExternalVariables().forEach(externalVariableDto -> addedVariables.add(externalVariableDto.getIdVar()));

            suByMode.forEach(surveyUnitUpdateDto -> {
                List<CollectedVariableDto> variablesToKeep = new ArrayList<>();
                List<VariableDto> externalToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                surveyUnitUpdateDto.getCollectedVariables().forEach(variableStateDto -> {
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
                    surveyUnitUpdateDto.setCollectedVariables(variablesToKeep);
                    surveyUnitUpdateDto.setExternalVariables(externalToKeep);
                    latestUpdatesbyVariables.add(surveyUnitUpdateDto);
                }
            });
        });
        return latestUpdatesbyVariables;
    }

    @Override
    public List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitUpdatePersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<SurveyUnitId> suIds = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> suIds.add(new SurveyUnitId(surveyUnitDto.getIdUE())));
        return suIds.stream().distinct().toList();
    }

    @Override
    public List<SurveyUnitDto> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitUpdatePersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        return surveyUnits.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitUpdatePersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public Long deleteByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.deleteByIdQuestionnaire(idQuestionnaire);
    }

    private static List<Mode> getDistinctsModes(List<SurveyUnitUpdateDto> surveyUnits) {
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

}
