package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Stream;

public class SurveyUnitService implements SurveyUnitApiPort {

    private final SurveyUnitPersistencePort surveyUnitPersistencePort;

    public SurveyUnitService(SurveyUnitPersistencePort surveyUnitPersistencePort) {
        this.surveyUnitPersistencePort = surveyUnitPersistencePort;
    }

    @Override
    public void saveSurveyUnits(List<SurveyUnitDto> suDtos) {
        surveyUnitPersistencePort.saveAll(suDtos);
    }

    @Override
    public List<SurveyUnitDto> findByIdsUEAndQuestionnaire(String idUE, String idQuest) {
        return surveyUnitPersistencePort.findByIds(idUE, idQuest);
    }

    @Override
    public List<SurveyUnitDto> findByIdUE(String idUE) {
        return surveyUnitPersistencePort.findByIdUE(idUE);
    }

    @Override
    public Stream<SurveyUnitDto> findByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitPersistencePort.findByIdQuestionnaire(idQuestionnaire);
    }

    /**
     * In this method we want to get the latest update for each variable of a survey unit
     * But we need to separate the updates by mode
     * So we will calculate the latest state for a given collection mode
     * @param idUE : Survey unit id
     * @param idQuest : Questionnaire id
     * @return the latest update for each variable of a survey unit
     */
    @Override
    public List<SurveyUnitDto> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest) {
        List<SurveyUnitDto> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitDto> surveyUnitDtos = surveyUnitPersistencePort.findByIds(idUE, idQuest);
        List<Mode> modes = getDistinctsModes(surveyUnitDtos);
        modes.forEach(mode ->{
            List<SurveyUnitDto> suByMode = surveyUnitDtos.stream()
                    .filter(surveyUnitDto -> surveyUnitDto.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();

            //We had all the variables of the oldest update
            latestUpdatesbyVariables.add(suByMode.getFirst());
            //We keep the name of already added variables to skip them in older updates
            List<String> addedVariables = new ArrayList<>();
            SurveyUnitDto latestUpdate = suByMode.getFirst();

            latestUpdate.getCollectedVariables().forEach(variableStateDto -> addedVariables.add(variableStateDto.getIdVar()));
            latestUpdate.getExternalVariables().forEach(externalVariableDto -> addedVariables.add(externalVariableDto.getIdVar()));

            suByMode.forEach(surveyUnitDto -> {
                List<CollectedVariableDto> variablesToKeep = new ArrayList<>();
                List<VariableDto> externalToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                surveyUnitDto.getCollectedVariables().stream()
                        .filter(variableStateDto -> !addedVariables.contains(variableStateDto.getIdVar()))
                        .forEach(variableStateDto -> {
                           variablesToKeep.add(variableStateDto);
                           addedVariables.add(variableStateDto.getIdVar());
                        });
                if (surveyUnitDto.getExternalVariables() != null){
                    surveyUnitDto.getExternalVariables().stream()
                         .filter(externalVariableDto -> !addedVariables.contains(externalVariableDto.getIdVar()))
                         .forEach(externalVariableDto -> {
                            externalToKeep.add(externalVariableDto);
                            addedVariables.add(externalVariableDto.getIdVar());
                         });
                }

                // If there are new variables, we add the update to the list of latest updates
                if (!variablesToKeep.isEmpty() || !externalToKeep.isEmpty()){
                    surveyUnitDto.setCollectedVariables(variablesToKeep);
                    surveyUnitDto.setExternalVariables(externalToKeep);
                    latestUpdatesbyVariables.add(surveyUnitDto);
                }
            });
        });
        return latestUpdatesbyVariables;
    }

    @Override
    public List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<SurveyUnitId> suIds = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> suIds.add(new SurveyUnitId(surveyUnitDto.getIdUE())));
        return suIds.stream().distinct().toList();
    }

    @Override
    public List<SurveyUnitDto> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        return surveyUnits.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitDto> surveyUnits = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByIdCampaign(String idCampaign) {
        List<SurveyUnitDto> surveyUnits = surveyUnitPersistencePort.findIdUEsByIdCampaign(idCampaign);
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public Long deleteByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitPersistencePort.deleteByIdQuestionnaire(idQuestionnaire);
    }

    @Override
    public long countResponses() {
        return surveyUnitPersistencePort.count();
    }

    @Override
    public Set<String> findIdQuestionnairesByIdCampaign(String idCampaign) {
            return surveyUnitPersistencePort.findIdQuestionnairesByIdCampaign(idCampaign);
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        return surveyUnitPersistencePort.findDistinctIdCampaigns();
    }

    @Override
    public List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> campaignsWithQuestionnaireList = new ArrayList<>();
        for(String idCampaign : findDistinctIdCampaigns()){
            Set<String> questionnaires = findIdQuestionnairesByIdCampaign(idCampaign);
            campaignsWithQuestionnaireList.add(new CampaignWithQuestionnaire(idCampaign,questionnaires));
        }
        return campaignsWithQuestionnaireList;
    }

    @Override
    public long countResponsesByIdCampaign(String idCampaign){
        return surveyUnitPersistencePort.countByIdCampaign(idCampaign);
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        return surveyUnitPersistencePort.findDistinctIdQuestionnaires();
    }

    @Override
    public List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList = new ArrayList<>();
        for(String idQuestionnaire : findDistinctIdQuestionnaires()){
            Set<String> campaigns = surveyUnitPersistencePort.findIdCampaignsByIdQuestionnaire(idQuestionnaire);
            questionnaireWithCampaignList.add(new QuestionnaireWithCampaign(
                    idQuestionnaire,
                    campaigns)
            );

        }
        return questionnaireWithCampaignList;
    }

    private static List<Mode> getDistinctsModes(List<SurveyUnitDto> surveyUnits) {
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

}