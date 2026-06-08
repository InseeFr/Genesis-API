package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
public abstract class RawDataConverter {
    private final SurveyUnitService surveyUnitService;

    @Autowired
    public RawDataConverter(SurveyUnitService surveyUnitService) {
        this.surveyUnitService = surveyUnitService;
    }

    /**
     * @param questionnaireOrCollectionInstrumentId Questionnaire/Collection instrument id
     * @param interrogationIds list of interrogation ids
     * @return a Map containing latest survey unit models for each interrogation ids
     */
    protected Map<String, Map<DataState, SurveyUnitModel>> getLastSurveyUnitModels(
            String questionnaireOrCollectionInstrumentId,
            List<String> interrogationIds
    ) {
        Set<String> interrogationIdsSet = new HashSet<>(interrogationIds);

        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByInterrogationIds(
                questionnaireOrCollectionInstrumentId,
                interrogationIdsSet
        );

        Map<String, Map<DataState, SurveyUnitModel>> surveyUnitModelsByInterrogationIdAndState = new HashMap<>();

        for (String interrogationId : interrogationIdsSet){
            List<SurveyUnitModel> surveyUnitModelsForInterrogationId = surveyUnitModels.stream().filter(
                    surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(interrogationId)
            ).toList();
            if(surveyUnitModelsForInterrogationId.isEmpty()){
                continue;
            }
            addSurveyUnitsOfInterrogationByState(
                    interrogationId,
                    surveyUnitModelsForInterrogationId,
                    surveyUnitModelsByInterrogationIdAndState
            );
        }

        return surveyUnitModelsByInterrogationIdAndState;
    }

    private static void addSurveyUnitsOfInterrogationByState(
            String interrogationId,
            List<SurveyUnitModel> surveyUnitModelsForInterrogationId,
            Map<String, Map<DataState, SurveyUnitModel>> surveyUnitModelsByInterrogationIdAndState
    ) {
        surveyUnitModelsByInterrogationIdAndState.put(interrogationId, new HashMap<>());
        for(SurveyUnitModel surveyUnitOfInterrogation : surveyUnitModelsForInterrogationId){
            if(surveyUnitOfInterrogation.getState() == null){
                continue;
            }
            surveyUnitModelsByInterrogationIdAndState.get(interrogationId).put(
                    surveyUnitOfInterrogation.getState(),
                    surveyUnitOfInterrogation
            );
        }
    }

    /**
     * <p>Adds missing variables/iterations to raw data map based on last survey unit in database</p>
     * <p>e.g. raw: var1 absent AND response: var1 present -> var1 added with null value</p>
     *
     * @param rawDataMap dataMap (COLLECTED or EXTERNAL)
     * @param dataState null if EXTERNAL
     */
    private static void fillRawDataMapWithAbsentVariables(
            @NotNull SurveyUnitModel lastSurveyUnit,
            Map<String, Object> rawDataMap,
            @Nullable DataState dataState
    ) {
        List<VariableModel> lastSurveyUnitVariables = dataState == null ?
                lastSurveyUnit.getExternalVariables() : lastSurveyUnit.getCollectedVariables();

        //Extract variable names
        Set<String> lastSurveyUnitVariablesNames = lastSurveyUnitVariables.stream()
                .map(VariableModel::varId).collect(Collectors.toSet());

        for(String lastSurveyUnitVariableName : lastSurveyUnitVariablesNames){
            List<VariableModel> variableModelsForSameVariable = lastSurveyUnitVariables.stream().filter(
                    variableAlreadyPresent ->
                            variableAlreadyPresent.varId().equals(lastSurveyUnitVariableName)
            ).toList();

            //If multiple iterations
            if(variableModelsForSameVariable.size() > 1){
                fillRawDataMapWithIterations(
                        variableModelsForSameVariable,
                        rawDataMap,
                        dataState
                );
                continue;
            }
            addNullVariableToRawDataMapIfAbsent(lastSurveyUnitVariableName, rawDataMap);
        }
    }

    /**
     * Checks each iteration of a last response variable and adds null to raw data if iteration is absent
     * @param alreadyPresentVariableIterations Variables models for same variable in last response
     * @param rawDataMap data map (COLLECTED or EXTERNAL)
     * @param dataState null if EXTERNAL
     */
    @SuppressWarnings("unchecked")
    private static void fillRawDataMapWithIterations(
            List<VariableModel> alreadyPresentVariableIterations,
            Map<String, Object> rawDataMap,
            @Nullable DataState dataState
    ){
        if(alreadyPresentVariableIterations == null || alreadyPresentVariableIterations.isEmpty()){
            return;
        }
        String variableName = alreadyPresentVariableIterations.getFirst().varId();

        int maxResponseIteration = alreadyPresentVariableIterations.stream()
                .map(VariableModel::iteration)
                .max(Integer::compareTo).orElse(0);

        List<Object> rawValuesList = getRawDataListForVariable(
                variableName,
                rawDataMap,
                dataState
        );

        //Add null element at end of raw list until max iteration
        fillListUntilMaxIteration(rawValuesList, maxResponseIteration);

        //Replace list in raw data map
        if(dataState == null){
            rawDataMap.put(variableName, rawValuesList);
            return;
        }
        Map<String, Object> stateMap = (Map<String, Object>) rawDataMap.get(variableName);
        stateMap.put(dataState.toString(), rawValuesList);
    }

    /**
     * @param variableName Name of the variable contained in last response
     * @param rawDataMap data map (COLLECTED or EXTERNAL)
     * @param dataState null if EXTERNAL
     * @return The list of values to put in raw data
     */
    @SuppressWarnings("unchecked")
    private static List<Object> getRawDataListForVariable(
            String variableName,
            Map<String, Object> rawDataMap,
            DataState dataState
    ) {
        //Instantiate list in rawDataMap if variable exists in response but not in raw
        if(!rawDataMap.containsKey(variableName)){
            return instantiateEmptyListInRawDataMap(variableName, rawDataMap, dataState);
        }

        if(dataState == null){
            return rawDataMap.get(variableName) instanceof List ?
                    (List<Object>) rawDataMap.get(variableName)
                    //If single value in raw but multiple in response, return a list with the single raw value
                    : instantiateListInRawDataMapWithSingleValue(variableName, rawDataMap, null);
        }
        Map<String, Object> stateMap = (Map<String, Object>) rawDataMap.get(variableName);
        return stateMap.get(dataState.toString()) instanceof List ?
                (List<Object>) stateMap.get(dataState.toString())
                : instantiateListInRawDataMapWithSingleValue(variableName, rawDataMap, dataState);
    }

    /**
     * @param variableName Name of the variable to add in map
     * @param rawDataMap COLLECTED or EXTERNAL map to fill
     * @param dataState null if in EXTERNAL map, COLLECTED if not
     * @return the instantiated list
     */
    private static List<Object> instantiateEmptyListInRawDataMap(
            String variableName,
            Map<String, Object> rawDataMap,
            @Nullable DataState dataState
    ) {
        List<Object> valueList = new ArrayList<>();
        if(dataState == null){
            rawDataMap.put(variableName, valueList);
            return valueList;
        }
        Map<String, Object> statesMap = new HashMap<>();
        statesMap.put(dataState.toString(), valueList);
        rawDataMap.put(variableName, statesMap);

        return valueList;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> instantiateListInRawDataMapWithSingleValue(
            String variableName,
            Map<String, Object> rawDataMap,
            DataState dataState
    ) {
        List<Object> singleValueList = new ArrayList<>(1);
        if(dataState == null){
            singleValueList.add(rawDataMap.get(variableName));
            return singleValueList;
        }
        Map<String, Object> stateMap = (Map<String, Object>) rawDataMap.get(variableName);
        singleValueList.add(stateMap.get(dataState.toString()));
        return singleValueList;
    }

    /**
     * Fills the list of values with null until max iteration
     * @param rawValuesList raw data values for a variable
     * @param maxResponseIteration max iteration in last survey unit for said variable
     */
    private static void fillListUntilMaxIteration(List<Object> rawValuesList, int maxResponseIteration) {
        for(int i = 1; i <= maxResponseIteration; i++){
            if(rawValuesList.size() >= i){
                continue;
            }
            rawValuesList.addLast(null);
        }
    }

    private static void addNullVariableToRawDataMapIfAbsent(
            String variableName,
            Map<String, Object> rawDataMap
    ) {
        if (!rawDataMap.containsKey(variableName)){
            rawDataMap.put(variableName, null);
        }
    }

    protected void convertCollectedVariables(
            Map<String, Object> dataOrPayloadMap,
            String interrogationId,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        //Get data map from payload
        Map<String, Object> dataMap = dataOrPayloadMap;
        if (rawDataModelType == RawDataModelType.FILIERE) {
            dataMap = JsonUtils.asMap(dataOrPayloadMap.get("data"));
        }
        Map<String, Object> collectedMap  = new HashMap<>(JsonUtils.asMap(dataMap.get("COLLECTED")));

        if(lastSurveyUnitModel != null && lastSurveyUnitModel.getState().equals(dataState)) {
            fillRawDataMapWithAbsentVariables(lastSurveyUnitModel, collectedMap, dataState);
        }

        if (collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", interrogationId);
            }
            return;
        }

        String stateKey = dataState.toString();
        List<VariableModel> collectedVariables = dstSurveyUnitModel.getCollectedVariables();

        for (Map.Entry<String, Object> entry : collectedMap.entrySet()) {
            processCollectedVariableForState(
                    entry,
                    stateKey,
                    variablesMap,
                    lastSurveyUnitModel,
                    dstSurveyUnitModel,
                    collectedVariables
            );
        }
    }

    static void processCollectedVariableForState(
            Map.Entry<String, Object> entry,
            String stateKey,
            VariablesMap variablesMap,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            List<VariableModel> variableModelList
    ) {
        if (Constants.PAIRWISES.equals(entry.getKey())) {
            handlePairwiseCollectedVariable(entry, DataState.valueOf(stateKey), variablesMap, dstSurveyUnitModel);
            return;
        }

        Map<String, Object> variableStates = JsonUtils.asMap(entry.getValue());
        //If variable value absent for state or null value
        if (variableStates == null || variableStates.get(stateKey) == null) {
            convertNullVar(entry.getKey(), lastSurveyUnitModel, 1, variablesMap, variableModelList, true);
            return;
        }

        Object value = variableStates.get(stateKey);
        if (value instanceof List<?> list) {
            convertListVar(list, lastSurveyUnitModel, entry, variablesMap, variableModelList, true);
            return;
        }
        convertOneVar(entry.getKey(), getValueString(value), variablesMap, 1, variableModelList);
    }

    @SuppressWarnings("unchecked")
    static void handlePairwiseCollectedVariable(
            Map.Entry<String, Object> collectedVariable,
            DataState dataState,
            VariablesMap variablesMap,
            SurveyUnitModel dstSurveyUnitModel
    ) {
        Object value = getValueForState(collectedVariable, dataState.toString());

        if (isInvalidPairwiseVariable(value, variablesMap)) {
            return;
        }

        List<?> individuals = (List<?>) value;
        String groupName = variablesMap.getVariable(Constants.PAIRWISE_PREFIX + 1).getGroupName();

        for (int individualIndex = 0; individualIndex < individuals.size(); individualIndex++) {
            List<String> individualLinks = (List<String>) individuals.get(individualIndex);

            for (int linkIndex = 1; linkIndex < Constants.MAX_LINKS_ALLOWED; linkIndex++) {
                dstSurveyUnitModel.getCollectedVariables().add(
                        buildPairwiseVariable(individualLinks, linkIndex, individualIndex + 1, groupName)
                );
            }
        }
    }

    private static VariableModel buildPairwiseVariable(
            List<String> individualLinks,
            int linkIndex,
            int iteration,
            String groupName
    ) {
        String value = Constants.NO_PAIRWISE_VALUE;

        if (linkIndex <= individualLinks.size()) {
            String v = individualLinks.get(linkIndex - 1);
            value = (v == null || v.isBlank()) ? Constants.SAME_AXIS_VALUE : v;
        }

        return VariableModel.builder()
                .varId(Constants.PAIRWISE_PREFIX + linkIndex)
                .value(value)
                .scope(groupName)
                .iteration(iteration)
                .parentId(Constants.ROOT_GROUP_NAME)
                .build();
    }

    private static Object getValueForState(
            Map.Entry<String, Object> collectedVariable,
            String stateKey
    ) {
        Map<String, Object> states = JsonUtils.asMap(collectedVariable.getValue());
        return states != null ? states.get(stateKey) : null;
    }

    private static boolean isInvalidPairwiseVariable(Object value, VariablesMap variablesMap) {
        return !(value instanceof List<?>) || !variablesMap.hasVariable(Constants.PAIRWISE_PREFIX + 1);
    }

    private static void convertNullVar(
            String variableName,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            int iteration,
            VariablesMap variablesMap,
            List<VariableModel> destination,
            boolean isCollected //true if in collected variables, false if in external
    ) {
        //Do nothing if last response is null or variable is not present in last response
        if (lastSurveyUnitModel == null || isVariableAbsentInSurveyUnitModel(
                variableName, iteration, lastSurveyUnitModel, isCollected
        )) {
            return;
        }
        convertOneVar(variableName, null, variablesMap, iteration, destination);
    }

    private static boolean isVariableAbsentInSurveyUnitModel(
            String variableName,
            int iteration,
            SurveyUnitModel lastSurveyUnitModel,
            boolean isCollected
    ) {
        //Check in collected variables
        if(isCollected){
            return lastSurveyUnitModel.getCollectedVariables().stream().noneMatch(
                    variableModel -> variableModel.varId().equals(variableName)
                            && variableModel.iteration().equals(iteration)
            );
        }
        //Check in external variables
        return lastSurveyUnitModel.getExternalVariables().stream().noneMatch(
                variableModel -> variableModel.varId().equals(variableName)
                        && variableModel.iteration().equals(iteration)
        );
    }

    private static void convertOneVar(
            String variableName,
            String value,
            VariablesMap variablesMap,
            int iteration,
            List<VariableModel> destination
    ) {
        VariableModel variableModel = VariableModel.builder()
                .varId(variableName)
                .value(value)
                .scope(getIdLoop(variablesMap, variableName))
                .iteration(iteration)
                .parentId(GroupUtils.getParentGroupName(variableName, variablesMap))
                .build();

        destination.add(variableModel);
    }

    private static String getIdLoop(VariablesMap variablesMap, String variableName) {
        Variable variable = variablesMap.getVariable(variableName);
        if (variable == null) {
            log.warn("Variable {} not present in metadata, assigning to {}", variableName, Constants.ROOT_GROUP_NAME);
            return Constants.ROOT_GROUP_NAME;
        }
        return variable.getGroupName();
    }

    private static void convertListVar(
            Object valuesForState,
            @Nullable SurveyUnitModel lastSurveyUnit,
            Map.Entry<String, Object> variableEntry,
            VariablesMap variablesMap,
            List<VariableModel> destination,
            boolean isCollected
    ) {
        List<String> values = JsonUtils.asStringList(valuesForState);

        if (!values.isEmpty()) {
            int iteration = 1;
            for (String value : values) {
                if(value == null || value.isEmpty()){
                    convertNullVar(
                            variableEntry.getKey(),
                            lastSurveyUnit,
                            iteration,
                            variablesMap,
                            destination,
                            isCollected);
                    iteration++;
                    continue;
                }
                convertOneVar(variableEntry.getKey(), value, variablesMap, iteration, destination);
                iteration++;
            }
        }
    }

    protected void convertExternalVariables(
            Map<String, Object> dataOrPayloadMap,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        //Get data map from payload if filiere model
        Map<String, Object> dataMap = dataOrPayloadMap;
        if (rawDataModelType == RawDataModelType.FILIERE) {
            dataMap = JsonUtils.asMap(dataOrPayloadMap.get("data"));
        }
        Map<String, Object> externalMap = null;
        if(dataMap.containsKey("EXTERNAL")) {
            externalMap = new HashMap<>(JsonUtils.asMap(dataMap.get("EXTERNAL")));
        }

        if(lastSurveyUnitModel != null){
            fillRawDataMapWithAbsentVariables(lastSurveyUnitModel, externalMap, null);
        }
        if(externalMap == null){
            return;
        }

        for (Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()) {
            Object valueObject = externalVariableEntry.getValue();

            if (valueObject == null) {
                convertNullVar(
                        externalVariableEntry.getKey(),
                        lastSurveyUnitModel,
                        1,
                        variablesMap,
                        dstSurveyUnitModel.getExternalVariables(),
                        false);
                continue;
            }
            if (valueObject instanceof List<?>) {
                convertListVar(
                        valueObject,
                        lastSurveyUnitModel,
                        externalVariableEntry,
                        variablesMap,
                        dstSurveyUnitModel.getExternalVariables(),
                        false
                );
                continue;
            }
            convertOneVar(
                    externalVariableEntry.getKey(),
                    valueObject.toString(),
                    variablesMap,
                    1,
                    dstSurveyUnitModel.getExternalVariables()
            );
        }
    }

    static String getValueString(Object value) {
        if (value instanceof Double || value instanceof Float) {
            BigDecimal bd = new BigDecimal(value.toString());
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return String.valueOf(value);
    }
}
