package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public abstract class RawDataConverter {
    private SurveyUnitService surveyUnitService;

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
    protected static void fillRawDataMapWithAbsentVariables(
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
}
