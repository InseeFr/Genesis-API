package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.CampaignWithQuestionnaire;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.SurveyUnitId;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitUpdateApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitUpdatePersistencePort;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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
    public Stream<SurveyUnitUpdateDto> findByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.findByIdQuestionnaire(idQuestionnaire);
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
    public List<SurveyUnitUpdateDto> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest) {
        List<SurveyUnitUpdateDto> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtos = surveyUnitUpdatePersistencePort.findByIds(idUE, idQuest);
        List<Mode> modes = getDistinctsModes(surveyUnitUpdateDtos);
        modes.forEach(mode ->{
            List<SurveyUnitUpdateDto> suByMode = surveyUnitUpdateDtos.stream()
                    .filter(surveyUnitUpdateDto -> surveyUnitUpdateDto.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();

            //We had all the variables of the oldest update
            latestUpdatesbyVariables.add(suByMode.getFirst());
            //We keep the name of already added variables to skip them in older updates
            List<String> addedVariables = new ArrayList<>();
            SurveyUnitUpdateDto latestUpdate = suByMode.getFirst();

            latestUpdate.getCollectedVariables().forEach(variableStateDto -> addedVariables.add(variableStateDto.getIdVar()));
            latestUpdate.getExternalVariables().forEach(externalVariableDto -> addedVariables.add(externalVariableDto.getIdVar()));

            suByMode.forEach(surveyUnitUpdateDto -> {
                List<CollectedVariableDto> variablesToKeep = new ArrayList<>();
                List<VariableDto> externalToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                surveyUnitUpdateDto.getCollectedVariables().stream()
                        .filter(variableStateDto -> !addedVariables.contains(variableStateDto.getIdVar()))
                        .forEach(variableStateDto -> {
                           variablesToKeep.add(variableStateDto);
                           addedVariables.add(variableStateDto.getIdVar());
                        });
                if (surveyUnitUpdateDto.getExternalVariables() != null){
                    surveyUnitUpdateDto.getExternalVariables().stream()
                         .filter(externalVariableDto -> !addedVariables.contains(externalVariableDto.getIdVar()))
                         .forEach(externalVariableDto -> {
                            externalToKeep.add(externalVariableDto);
                            addedVariables.add(externalVariableDto.getIdVar());
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
    public List<Mode> findModesByIdCampaign(String idCampaign) {
        List<SurveyUnitDto> surveyUnits = surveyUnitUpdatePersistencePort.findIdUEsByIdCampaign(idCampaign);
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public Long deleteByIdQuestionnaire(String idQuestionnaire) {
        return surveyUnitUpdatePersistencePort.deleteByIdQuestionnaire(idQuestionnaire);
    }

    @Override
    public long countResponses() {
        return surveyUnitUpdatePersistencePort.count();
    }

    @Override
    public Set<String> findIdQuestionnairesByIdCampaign(String idCampaign) {
            return surveyUnitUpdatePersistencePort.findIdQuestionnairesByIdCampaign(idCampaign);
    }

    @Override
    public Set<String> findDistinctIdCampaigns() {
        return surveyUnitUpdatePersistencePort.findDistinctIdCampaigns();
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
        return surveyUnitUpdatePersistencePort.countByIdCampaign(idCampaign);
    }

    @Override
    public Set<String> findDistinctIdQuestionnaires() {
        return surveyUnitUpdatePersistencePort.findDistinctIdQuestionnaires();
    }

    @Override
    public List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList = new ArrayList<>();
        for(String idQuestionnaire : findDistinctIdQuestionnaires()){
            Set<String> campaigns = surveyUnitUpdatePersistencePort.findIdCampaignsByIdQuestionnaire(idQuestionnaire);
            questionnaireWithCampaignList.add(new QuestionnaireWithCampaign(
                    idQuestionnaire,
                    campaigns)
            );

        }
        return questionnaireWithCampaignList;
    }

    private static List<Mode> getDistinctsModes(List<SurveyUnitUpdateDto> surveyUnits) {
        List<Mode> sources = new ArrayList<>();
        surveyUnits.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

}
