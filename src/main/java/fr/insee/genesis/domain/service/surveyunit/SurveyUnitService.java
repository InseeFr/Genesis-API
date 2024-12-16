package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.controller.dto.*;
import fr.insee.genesis.domain.model.surveyunit.*;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Stream;

@Service
public class SurveyUnitService implements SurveyUnitApiPort {

    @Qualifier("surveyUnitMongoAdapter")
    private final SurveyUnitPersistencePort surveyUnitPersistencePort;

    @Autowired
    public SurveyUnitService(SurveyUnitPersistencePort surveyUnitPersistencePort) {
        this.surveyUnitPersistencePort = surveyUnitPersistencePort;
    }

    @Override
    public void saveSurveyUnits(List<SurveyUnitModel> suDtos) {
        surveyUnitPersistencePort.saveAll(suDtos);
    }

    @Override
    public List<SurveyUnitModel> findByIdsUEAndQuestionnaire(String idUE, String idQuest) {
        return surveyUnitPersistencePort.findByIds(idUE, idQuest);
    }

    @Override
    public List<SurveyUnitModel> findByIdUE(String idUE) {
        return surveyUnitPersistencePort.findByIdUE(idUE);
    }

    @Override
    public Stream<SurveyUnitModel> findByIdQuestionnaire(String idQuestionnaire) {
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
    public List<SurveyUnitModel> findLatestByIdAndByIdQuestionnaire(String idUE, String idQuest) {
        List<SurveyUnitModel> latestUpdatesbyVariables = new ArrayList<>();
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByIds(idUE, idQuest);
        List<Mode> modes = getDistinctsModes(surveyUnitModels);
        modes.forEach(mode ->{
            List<SurveyUnitModel> suByMode = surveyUnitModels.stream()
                    .filter(surveyUnitDto -> surveyUnitDto.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();

            //We had all the variables of the oldest update
            latestUpdatesbyVariables.add(suByMode.getFirst());
            //We keep the name of already added variables to skip them in older updates
            List<IdLoopTuple> addedVariables = new ArrayList<>();
            SurveyUnitModel latestUpdate = suByMode.getFirst();

            if(latestUpdate.getCollectedVariables() == null){
                latestUpdate.setCollectedVariables(new ArrayList<>());
            }
            if(latestUpdate.getExternalVariables() == null){
                latestUpdate.setExternalVariables(new ArrayList<>());
            }
            latestUpdate.getCollectedVariables().forEach(colVar -> addedVariables.add(new IdLoopTuple(colVar.getIdVar(), colVar.getIdLoop())));
            latestUpdate.getExternalVariables().forEach(extVar -> addedVariables.add(new IdLoopTuple(extVar.getIdVar(), "")));

            suByMode.forEach(surveyUnitModel -> {
                List<CollectedVariable> variablesToKeep = new ArrayList<>();
                List<Variable> externalToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                surveyUnitModel.getCollectedVariables().stream()
                        .filter(colVar -> !addedVariables.contains(new IdLoopTuple(colVar.getIdVar(), colVar.getIdLoop())))
                        .forEach(colVar -> {
                           variablesToKeep.add(colVar);
                           addedVariables.add(new IdLoopTuple(colVar.getIdVar(), colVar.getIdLoop()));
                        });
                if (surveyUnitModel.getExternalVariables() != null){
                    surveyUnitModel.getExternalVariables().stream()
                         .filter(extVar -> !addedVariables.contains(new IdLoopTuple(extVar.getIdVar(), "")))
                         .forEach(extVar -> {
                            externalToKeep.add(extVar);
                            addedVariables.add(new IdLoopTuple(extVar.getIdVar(), ""));
                         });
                }

                // If there are new variables, we add the update to the list of latest updates
                if (!variablesToKeep.isEmpty() || !externalToKeep.isEmpty()){
                    surveyUnitModel.setCollectedVariables(variablesToKeep);
                    surveyUnitModel.setExternalVariables(externalToKeep);
                    latestUpdatesbyVariables.add(surveyUnitModel);
                }
            });
        });
        return latestUpdatesbyVariables;
    }

    @Override
    public SurveyUnitDto findLatestValuesByStateByIdAndByIdQuestionnaire(String idUE, String idQuest) {
        SurveyUnitDto surveyUnitDto = SurveyUnitDto.builder()
                .surveyUnitId(idUE)
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        //Extract variables
        Map<IdLoopTuple, VariableDto> collectedVariableMap = new HashMap<>();
        Map<String, VariableDto> externalVariableMap = new HashMap<>();
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByIds(idUE, idQuest);
        List<Mode> modes = getDistinctsModes(surveyUnitModels);
        modes.forEach(mode -> {
            List<SurveyUnitModel> suByMode = surveyUnitModels.stream()
                    .filter(surveyUnitModel -> surveyUnitModel.getMode().equals(mode))
                    .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                    .toList();
            suByMode.forEach(surveyUnitModel -> extractVariables(surveyUnitModel, collectedVariableMap,externalVariableMap));
        });
        collectedVariableMap.keySet().forEach(variableTuple -> surveyUnitDto.getCollectedVariables().add(collectedVariableMap.get(variableTuple)));
        externalVariableMap.keySet().forEach(variableName -> surveyUnitDto.getExternalVariables().add(externalVariableMap.get(variableName)));
        return surveyUnitDto;
    }

    @Override
    public List<SurveyUnitId> findDistinctIdUEsByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<SurveyUnitId> suIds = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitDto -> suIds.add(new SurveyUnitId(surveyUnitDto.getIdUE())));
        return suIds.stream().distinct().toList();
    }

    @Override
    public List<SurveyUnitModel> findIdUEsAndModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        return surveyUnitModels.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByIdQuestionnaire(String idQuestionnaire) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findIdUEsByIdQuestionnaire(idQuestionnaire);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByIdCampaign(String idCampaign) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findIdUEsByIdCampaign(idCampaign);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
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

    private static List<Mode> getDistinctsModes(List<SurveyUnitModel> surveyUnitModels) {
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitDto -> sources.add(surveyUnitDto.getMode()));
        return sources.stream().distinct().toList();
    }

    /**
     * Extract collected variables from a model class to a variable map
     * @param surveyUnitModel survey unit model
     * @param collectedVariableMap Collected variable DTO map to populate
     * @param externalVariableMap External variable DTO map to populate
     */
    private void extractVariables(SurveyUnitModel surveyUnitModel,
                                           Map<IdLoopTuple, VariableDto> collectedVariableMap,
                                           Map<String, VariableDto> externalVariableMap) {

        if(surveyUnitModel.getCollectedVariables() == null){
            surveyUnitModel.setCollectedVariables(new ArrayList<>());
        }
        for (CollectedVariable collectedVariable : surveyUnitModel.getCollectedVariables()) {
            IdLoopTuple idLoopTuple = new IdLoopTuple(collectedVariable.getIdVar(), collectedVariable.getIdLoop());
            VariableDto variableDto = collectedVariableMap.get(idLoopTuple);

            //Create variable into map if not exists
            if (variableDto == null) {
                variableDto = VariableDto.builder()
                        .variableName(collectedVariable.getIdVar())
                        .idLoop(collectedVariable.getIdLoop())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                collectedVariableMap.put(idLoopTuple, variableDto);
            }
            //Extract variable state
            if (!collectedVariable.getValues().isEmpty() && isMostRecentForSameState(surveyUnitModel, variableDto)) {
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(collectedVariable.getValues().getFirst())
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }

        if(surveyUnitModel.getExternalVariables() == null){
            surveyUnitModel.setExternalVariables(new ArrayList<>());
        }
        for(Variable externalVariable : surveyUnitModel.getExternalVariables()){
            VariableDto variableDto = externalVariableMap.get(externalVariable.getIdVar());

            //Create variable into map if not exists
            if(variableDto == null){
                variableDto = VariableDto.builder()
                        .variableName(externalVariable.getIdVar())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                externalVariableMap.put(externalVariable.getIdVar(), variableDto);
            }
            //Extract variable state
            if(!externalVariable.getValues().isEmpty() && isMostRecentForSameState(surveyUnitModel, variableDto)){
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(externalVariable.getValues().getFirst())
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }
    }


    /**
     * Check if there is any other more recent variable value for a same state in Variable DTO
     * @param surveyUnitModel model containing variable
     * @param variableDto DTO to check in
     * @return true if there's no more recent variable for the same state
     */
    private boolean isMostRecentForSameState(SurveyUnitModel surveyUnitModel, VariableDto variableDto) {
        List<VariableStateDto> variableStatesSameState = variableDto.getVariableStateDtoList().stream().filter(
                variableStateDto -> variableStateDto.getState().equals(surveyUnitModel.getState())
        )
                .sorted((o1, o2) -> o2.getDate().compareTo(o1.getDate()))
                .toList();
        if(variableStatesSameState.isEmpty()){
            //Variable doesn't contain state
            return true;
        }
        LocalDateTime mostRecentStateDateTime = variableStatesSameState.getFirst().getDate();
        return mostRecentStateDateTime.isBefore(surveyUnitModel.getRecordDate());
    }

    /**
     * Check if model is more recent that any variable state in variable DTO regardless of state
     * Used for active variable
     * @param surveyUnitModel model used to compare
     * @param variableDto variable to check
     * @return false if there is any variable state that comes from a more recent model already
     */
    private boolean isLastVariableState(SurveyUnitModel surveyUnitModel, VariableDto variableDto) {
        for(VariableStateDto variableStateDTO : variableDto.getVariableStateDtoList()){
            if(variableStateDTO.getDate().isAfter(surveyUnitModel.getRecordDate())){
                return false;
            }
            variableStateDTO.setActive(false);
        }
        return true;
    }
}
