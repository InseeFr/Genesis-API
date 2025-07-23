package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.VariableDto;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VarIdScopeTuple;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.exceptions.GenesisException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
    public List<SurveyUnitModel> findByIdsInterrogationAndQuestionnaire(String interrogationId, String questionnaireId) {
        return surveyUnitPersistencePort.findByIds(interrogationId, questionnaireId);
    }

    @Override
    public List<SurveyUnitModel> findByInterrogationId(String interrogationId) {
        return surveyUnitPersistencePort.findByInterrogationId(interrogationId);
    }

    @Override
    public Stream<SurveyUnitModel> findByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.findByQuestionnaireId(questionnaireId);
    }


    /**
     * In this method we want to get the latest update for each variable of a survey unit
     * But we need to separate the updates by mode
     * So we will calculate the latest state for a given collection mode
     * @param interrogationId : Survey unit id
     * @param questionnaireId : Questionnaire id
     * @return the latest update for each variable of a survey unit
     */
    @Override
    public List<SurveyUnitModel> findLatestByIdAndByQuestionnaireId(String interrogationId, String questionnaireId) {
        List<Mode> enumModes = findModesByQuestionnaireId(questionnaireId);
        // => convertion of "List<Mode>" -> "List<String>" for query using lamda
        List<String> modes = enumModes.stream().map(Mode::getModeName).toList();
        List<InterrogationId> interrogationIds = List.of(new InterrogationId(interrogationId));

        List<SurveyUnitModel> responses = new ArrayList<>();
        for(String mode : modes) {
            List<List<SurveyUnitModel>> surveyUnitModels = findLatestByIdAndByQuestionnaireIdAndModeOrdered(questionnaireId, mode, interrogationIds);
            for(List<SurveyUnitModel> singleSurveyUnitModel : surveyUnitModels) {
                responses.addAll(singleSurveyUnitModel);
            }
        }

        return responses;
    }


    /**
     * In this method we want to get the latest update for each variable of a survey unit
     * But we need to separate the updates by mode
     * So we will calculate the latest state for a given collection mode
     * @param questionnaireId : Questionnaire id
     * @param mode : collect mode
     * @param interrogationIds : !!!A LIST OF!!! Survey unit ids
     * @return the latest update for each variable of a survey unit
     * @author Adrien Marchal
     */
    @Override
    public List<List<SurveyUnitModel>> findLatestByIdAndByQuestionnaireIdAndModeOrdered(String questionnaireId, String mode,
                                                                                        List<InterrogationId> interrogationIds) {
        //return object
        List<List<SurveyUnitModel>> listLatestUpdatesbyVariables = new ArrayList<>();

        //1) QUERY
        // => convertion of "List<InterrogationId>" -> "List<String>" for query using lamda
        List<String> queryInParam = interrogationIds.stream().map(InterrogationId::getInterrogationId).collect(Collectors.toList());

        //Get !!!all versions!!! of a set of "interrogationIds"
        List<SurveyUnitModel> allResponsesVersionsSet = surveyUnitPersistencePort.findBySetOfIdsAndQuestionnaireIdAndMode(questionnaireId, mode, queryInParam);

        //2) FILTER BY interrogationId AND ORDER BY DATE (MOST RECENT FIRST, oldest last)
        interrogationIds.forEach(interrogationId -> {
            List<SurveyUnitModel> allResponsesVersionsForSingleInterrId = allResponsesVersionsSet.stream()
                            .filter(surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId.getInterrogationId()))
                            .sorted((o1, o2) -> o2.getRecordDate().compareTo(o1.getRecordDate())) //Sorting update by date (latest updates first by date of upload in database)
                            .toList();

            List<SurveyUnitModel> LatestUpdates = extractLatestUpdates(allResponsesVersionsForSingleInterrId);

            //3) add to result (: keep same existing process)
            listLatestUpdatesbyVariables.add(LatestUpdates);
        });

        return listLatestUpdatesbyVariables;
    }


    private List<SurveyUnitModel> extractLatestUpdates(List<SurveyUnitModel> allResponsesVersionsForSingleInterrId) {
        //return
        List<SurveyUnitModel> latestUpdatesbyVariables = new ArrayList<>();

        //We keep the name of already added variables to skip them in older updates
        List<VarIdScopeTuple> addedVariables = new ArrayList<>();

        //No useless process if there is only ONE or NONE element (performances optimisations)
        if(allResponsesVersionsForSingleInterrId.size() <= 1) {
            //We add all the variables of the LATEST update
            latestUpdatesbyVariables.add(allResponsesVersionsForSingleInterrId.getFirst());
            return latestUpdatesbyVariables;
        }

        //ELSE -> CASE WHERE THERE ARE MORE THAN ONE VERSION!
        allResponsesVersionsForSingleInterrId.forEach(surveyUnitModel -> {
            List<VariableModel> collectedVariablesToKeep = new ArrayList<>();
            List<VariableModel> externalVariablesToKeep = new ArrayList<>();
            // We iterate over the variables of the update and add them to the list if they are not already added
            if (surveyUnitModel.getCollectedVariables() != null) {
                surveyUnitModel.getCollectedVariables().stream()
                        .filter(colVar -> !addedVariables.contains(new VarIdScopeTuple(colVar.varId(), colVar.scope(), colVar.iteration())))
                        .forEach(colVar -> {
                            collectedVariablesToKeep.add(colVar);
                            addedVariables.add(new VarIdScopeTuple(colVar.varId(), colVar.scope(), colVar.iteration()));
                        });
            }
            if (surveyUnitModel.getExternalVariables() != null){
                surveyUnitModel.getExternalVariables().stream()
                        .filter(extVar -> !addedVariables.contains(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration())))
                        .forEach(extVar -> {
                            externalVariablesToKeep.add(extVar);
                            addedVariables.add(new VarIdScopeTuple(extVar.varId(), extVar.scope(), extVar.iteration()));
                        });
            }

            // If there are new variables, we add the update to the list of latest updates
            if (!collectedVariablesToKeep.isEmpty() || !externalVariablesToKeep.isEmpty()){
                surveyUnitModel.setCollectedVariables(collectedVariablesToKeep);
                surveyUnitModel.setExternalVariables(externalVariablesToKeep);
                latestUpdatesbyVariables.add(surveyUnitModel);
            }
        });

        return latestUpdatesbyVariables;
    }


    @Override
    public SurveyUnitDto findLatestValuesByStateByIdAndByQuestionnaireId(String interrogationId, String questionnaireId) {
        SurveyUnitDto surveyUnitDto = SurveyUnitDto.builder()
                .interrogationId(interrogationId)
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        //Extract variables
        Map<VarIdScopeTuple, VariableDto> collectedVariableMap = new HashMap<>();
        Map<VarIdScopeTuple, VariableDto> externalVariableMap = new HashMap<>();
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByIds(interrogationId, questionnaireId);
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

    /**
     * !!!WARNING!!! : A CALL WITH THIS ENDPOINT ON A BIG COLLECTION (> 300k) MAY KILL THE GENESIS-API APP.!!!
     */
    @Override
    public List<InterrogationId> findDistinctInterrogationIdsByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByQuestionnaireId(questionnaireId);
        List<InterrogationId> suIds = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> suIds.add(new InterrogationId(surveyUnitModel.getInterrogationId())));
        return suIds.stream().distinct().toList();
    }


    /**
     * @author Adrien Marchal
     * Calculations made to establish the data a worker will be responsible of, among the whole data to be processed.
     * (needed for distributed process / horizontal scaling)
     */
    @Override
    public List<InterrogationId> findDistinctPageableInterrogationIdsByQuestionnaireId(String questionnaireId, long totalSize,
                                                                                       long blockSize, long page) {
        long calculatedTotalSize;
        if(totalSize == 0) {
            calculatedTotalSize = countInterrogationIdsByQuestionnaireId(questionnaireId);
        } else {
            calculatedTotalSize = totalSize;
        }

        //Check arguments
        long skip = page * blockSize;
        if(page < 0 || skip > calculatedTotalSize) {
            //return empty list
            return List.of();
        }
        long calculatedBlockSize = (skip + blockSize) > calculatedTotalSize ? calculatedTotalSize - skip : blockSize;

        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findPageableInterrogationIdsByQuestionnaireId(questionnaireId, skip, calculatedBlockSize);
        List<InterrogationId> suIds = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> suIds.add(new InterrogationId(surveyUnitModel.getInterrogationId())));
        return suIds.stream().distinct().toList();
    }


    /**
     * @author Adrien Marchal
     */
    @Override
    public long countInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.countInterrogationIdsByQuestionnaireId(questionnaireId);
    }


    /**
     * !!!WARNING!!! : A CALL WITH THIS ENDPOINT ON A BIG COLLECTION (> 300k) MAY KILL THE GENESIS-API APP.!!!
     */
    @Override
    public List<SurveyUnitModel> findInterrogationIdsAndModesByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findInterrogationIdsByQuestionnaireId(questionnaireId);
        return surveyUnitModels.stream().distinct().toList();
    }


    @Override
    public List<Mode> findModesByQuestionnaireId(String questionnaireId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findModesByQuestionnaireId(questionnaireId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    @Override
    public List<Mode> findModesByCampaignId(String campaignId) {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findModesByCampaignId(campaignId);
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }


    @Override
    public Long deleteByQuestionnaireId(String questionnaireId) {
        return surveyUnitPersistencePort.deleteByQuestionnaireId(questionnaireId);
    }

    @Override
    public long countResponses() {
        return surveyUnitPersistencePort.count();
    }


    /**
     * @author Adrien Marchal
     */
    @Override
    public Set<String> findQuestionnaireIdsByCampaignId(String campaignId) {
        return surveyUnitPersistencePort.findQuestionnaireIdsByCampaignId(campaignId);
    }


    @Override
    public Set<String> findDistinctCampaignIds() {
        return surveyUnitPersistencePort.findDistinctCampaignIds();
    }


    @Override
    public List<CampaignWithQuestionnaire> findCampaignsWithQuestionnaires() {
        List<CampaignWithQuestionnaire> campaignsWithQuestionnaireList = new ArrayList<>();
        for(String campaignId : findDistinctCampaignIds()){
            Set<String> questionnaires = findQuestionnaireIdsByCampaignId(campaignId);
            campaignsWithQuestionnaireList.add(new CampaignWithQuestionnaire(campaignId,questionnaires));
        }
        return campaignsWithQuestionnaireList;
    }


    @Override
    public long countResponsesByCampaignId(String campaignId){
        return surveyUnitPersistencePort.countByCampaignId(campaignId);
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return surveyUnitPersistencePort.findDistinctQuestionnaireIds();
    }

    @Override
    public List<QuestionnaireWithCampaign> findQuestionnairesWithCampaigns() {
        List<QuestionnaireWithCampaign> questionnaireWithCampaignList = new ArrayList<>();
        for(String questionnaireId : findDistinctQuestionnaireIds()){
            Set<String> campaigns = surveyUnitPersistencePort.findCampaignIdsByQuestionnaireId(questionnaireId);
            questionnaireWithCampaignList.add(new QuestionnaireWithCampaign(
                    questionnaireId,
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
                    .campaignId(surveyUnitInputDto.getCampaignId())
                    .mode(surveyUnitInputDto.getMode())
                    .questionnaireId(surveyUnitInputDto.getQuestionnaireId())
                    .interrogationId(surveyUnitInputDto.getInterrogationId())
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
                        .varId(editedVariableDto.getVariableName())
                        .value(editedVariableDto.getVariableStateInputDto().getValue())
                        .parentId(GroupUtils.getParentGroupName(editedVariableDto.getVariableName(), variablesMap))
                        .scope(variablesMap.getVariable(editedVariableDto.getVariableName()).getGroupName())
                        .iteration(editedVariableDto.getIteration())
                        .build();

                surveyUnitModel.getCollectedVariables().add(collectedVariable);

            }
            surveyUnitModels.add(surveyUnitModel);
        }

        return surveyUnitModels;
    }

    @Override
    public String findQuestionnaireIdByInterrogationId(String interrogationId) throws GenesisException {
        List<SurveyUnitModel> surveyUnitModels = surveyUnitPersistencePort.findByInterrogationId(interrogationId);
        if (surveyUnitModels.isEmpty()){
            throw new GenesisException(404,String.format("The interrogationId %s is not in database",interrogationId));
        }
        Set<String> questionnaireIds = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : surveyUnitModels){
            questionnaireIds.add(surveyUnitModel.getQuestionnaireId());
        }
        if(questionnaireIds.size() > 1){
            throw new GenesisException(207,String.format("Multiple questionnaires for %s :%n%s",
                    interrogationId,
                    String.join("\n", questionnaireIds)
            ));
        }

        return questionnaireIds.iterator().next(); //Return first (and supposed only) element of set
    }

    @Override
    public Set<String> findCampaignIdsFrom(SurveyUnitInputDto dto) {
        List<SurveyUnitModel> responses = findByIdsInterrogationAndQuestionnaire(
                dto.getInterrogationId(),
                dto.getQuestionnaireId()
        );
        return responses.stream()
                .map(SurveyUnitModel::getCampaignId)
                .collect(Collectors.toSet());
    }

    //Utils
    private static List<Mode> getDistinctsModes(List<SurveyUnitModel> surveyUnitModels) {
        List<Mode> sources = new ArrayList<>();
        surveyUnitModels.forEach(surveyUnitModel -> sources.add(surveyUnitModel.getMode()));
        return sources.stream().distinct().toList();
    }

    /**
     * Extract collected variables from a model class to a variable map
     * @param surveyUnitModel survey unit model
     * @param collectedVariableMap Collected variable DTO map to populate
     * @param externalVariableMap External variable DTO map to populate
     */
    private void extractVariables(SurveyUnitModel surveyUnitModel,
                                           Map<VarIdScopeTuple, VariableDto> collectedVariableMap,
                                           Map<VarIdScopeTuple, VariableDto> externalVariableMap) {

        if(surveyUnitModel.getCollectedVariables() == null){
            surveyUnitModel.setCollectedVariables(new ArrayList<>());
        }
        for (VariableModel collectedVariable : surveyUnitModel.getCollectedVariables()) {
            VarIdScopeTuple loopIdTuple = new VarIdScopeTuple(collectedVariable.varId(), collectedVariable.scope(),
                    collectedVariable.iteration());
            VariableDto variableDto = collectedVariableMap.get(loopIdTuple);

            //Create variable into map if not exists
            if (variableDto == null) {
                variableDto = VariableDto.builder()
                        .variableName(collectedVariable.varId())
                        .scope(collectedVariable.scope())
                        .iteration(collectedVariable.iteration())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                collectedVariableMap.put(loopIdTuple, variableDto);
            }
            //Extract variable state
            if (isMostRecentForSameState(surveyUnitModel, variableDto)) {
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(collectedVariable.value())
                                .date(surveyUnitModel.getRecordDate())
                                .build()
                );
            }
        }

        if(surveyUnitModel.getExternalVariables() == null){
            surveyUnitModel.setExternalVariables(new ArrayList<>());
        }
        for(VariableModel externalVariable : surveyUnitModel.getExternalVariables()){
            VarIdScopeTuple loopIdTuple = new VarIdScopeTuple(externalVariable.varId(), externalVariable.scope(), externalVariable.iteration());
            VariableDto variableDto = externalVariableMap.get(loopIdTuple);

            //Create variable into map if not exists
            if(variableDto == null){
                variableDto = VariableDto.builder()
                        .variableName(externalVariable.varId())
                        .scope(externalVariable.scope())
                        .iteration(externalVariable.iteration())
                        .variableStateDtoList(new ArrayList<>())
                        .build();
                externalVariableMap.put(loopIdTuple, variableDto);
            }
            //Extract variable state
            if(isMostRecentForSameState(surveyUnitModel, variableDto)){
                variableDto.getVariableStateDtoList().add(
                        VariableStateDto.builder()
                                .state(surveyUnitModel.getState())
                                .active(isLastVariableState(surveyUnitModel, variableDto))
                                .value(externalVariable.value())
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
