package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitId;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.VariableDto;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.IdLoopTuple;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.utils.LoopIdentifier;
import fr.insee.genesis.exceptions.GenesisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    public void saveSurveyUnits(List<SurveyUnitModel> surveyUnitModels) {
        surveyUnitPersistencePort.saveAll(surveyUnitModels);
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
            latestUpdate.getCollectedVariables().forEach(colVar -> addedVariables.add(new IdLoopTuple(colVar.idVar(),
                    colVar.idLoop())));
            latestUpdate.getExternalVariables().forEach(extVar -> addedVariables.add(new IdLoopTuple(extVar.idVar(), "")));

            suByMode.forEach(surveyUnitModel -> {
                List<VariableModel> collectedVariablesToKeep = new ArrayList<>();
                List<VariableModel> externalVariablesToKeep = new ArrayList<>();
                // We iterate over the variables of the update and add them to the list if they are not already added
                surveyUnitModel.getCollectedVariables().stream()
                        .filter(colVar -> !addedVariables.contains(new IdLoopTuple(colVar.idVar(), colVar.idLoop())))
                        .forEach(colVar -> {
                            collectedVariablesToKeep.add(colVar);
                           addedVariables.add(new IdLoopTuple(colVar.idVar(), colVar.idLoop()));
                        });
                if (surveyUnitModel.getExternalVariables() != null){
                    surveyUnitModel.getExternalVariables().stream()
                         .filter(extVar -> !addedVariables.contains(new IdLoopTuple(extVar.idVar(), "")))
                         .forEach(extVar -> {
                            externalVariablesToKeep.add(extVar);
                            addedVariables.add(new IdLoopTuple(extVar.idVar(), ""));
                         });
                }

                // If there are new variables, we add the update to the list of latest updates
                if (!collectedVariablesToKeep.isEmpty() || !externalVariablesToKeep.isEmpty()){
                    surveyUnitModel.setCollectedVariables(collectedVariablesToKeep);
                    surveyUnitModel.setExternalVariables(externalVariablesToKeep);
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
        Map<IdLoopTuple, VariableDto> externalVariableMap = new HashMap<>();
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

    @Override
    public List<SurveyUnitModel> parseEditedVariables(
            SurveyUnitInputDto surveyUnitInputDto,
            String userIdentifier,
            VariablesMap variablesMap
    ) throws GenesisException {

        List<DataState> statesReceived = surveyUnitInputDto.getCollectedVariables().stream()
                .map(colVar -> colVar.getVariableStateInputDto().getState())
                .distinct()
                .toList();

        if (statesReceived.contains(DataState.COLLECTED)){
            throw new GenesisException(400,"You can not persist in database a new value with the state COLLECTED");
        }

        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        for (DataState state : statesReceived){
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .idCampaign(surveyUnitInputDto.getCampaignId())
                    .mode(surveyUnitInputDto.getMode())
                    .idQuest(surveyUnitInputDto.getIdQuestionnaire())
                    .idUE(surveyUnitInputDto.getSurveyUnitId())
                    .state(state)
                    .recordDate(LocalDateTime.now())
                    .collectedVariables(new ArrayList<>())
                    .externalVariables(new ArrayList<>())
                    .modifiedBy(userIdentifier)
                    .build();

            //Keep only variable dtos who has the corresponding state
            List<VariableInputDto> editedCollectedVariables = surveyUnitInputDto.getCollectedVariables().stream()
                    .filter(colVar -> colVar.getVariableStateInputDto().getState() == state).toList();

            //Collected variables management
            for(VariableInputDto editedVariableDto : editedCollectedVariables){
                VariableModel collectedVariable = VariableModel.builder()
                        .idVar(editedVariableDto.getVariableName())
                        .values(new ArrayList<>())
                        .idParent(LoopIdentifier.getRelatedVariableName(editedVariableDto.getVariableName(), variablesMap))
                        .idLoop(editedVariableDto.getIdLoop())
                        .build();

                collectedVariable.values().add(editedVariableDto.getVariableStateInputDto().getValue());

                surveyUnitModel.getCollectedVariables().add(collectedVariable);

            }
            surveyUnitModels.add(surveyUnitModel);
        }

        return surveyUnitModels;
    }

    //Utils
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
                                           Map<IdLoopTuple, VariableDto> externalVariableMap) {

        if(surveyUnitModel.getCollectedVariables() == null){
            surveyUnitModel.setCollectedVariables(new ArrayList<>());
        }
        for (VariableModel collectedVariable : surveyUnitModel.getCollectedVariables()) {
            IdLoopTuple idLoopTuple = new IdLoopTuple(collectedVariable.idVar(), collectedVariable.idLoop());
            VariableDto variableDto = collectedVariableMap.get(idLoopTuple);

            //Create variable into map if not exists
            if (variableDto == null) {
                variableDto = VariableDto.builder()
                        .variableName(collectedVariable.idVar())
                        .idLoop(collectedVariable.idLoop())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                collectedVariableMap.put(idLoopTuple, variableDto);
            }
            //Extract variable state
            if (!collectedVariable.values().isEmpty() && isMostRecentForSameState(surveyUnitModel, variableDto)) {
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(collectedVariable.values().getFirst())
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }

        if(surveyUnitModel.getExternalVariables() == null){
            surveyUnitModel.setExternalVariables(new ArrayList<>());
        }
        for(VariableModel externalVariable : surveyUnitModel.getExternalVariables()){
            IdLoopTuple idLoopTuple = new IdLoopTuple(externalVariable.idVar(), externalVariable.idLoop());
            VariableDto variableDto = externalVariableMap.get(idLoopTuple);

            //Create variable into map if not exists
            if(variableDto == null){
                variableDto = VariableDto.builder()
                        .variableName(externalVariable.idVar())
                        .idLoop(externalVariable.idLoop())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                externalVariableMap.put(idLoopTuple, variableDto);
            }
            //Extract variable state
            if(!externalVariable.values().isEmpty() && isMostRecentForSameState(surveyUnitModel, variableDto)){
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(externalVariable.values().getFirst())
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
