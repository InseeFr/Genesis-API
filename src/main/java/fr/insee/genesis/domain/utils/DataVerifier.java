package fr.insee.genesis.domain.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
@UtilityClass
public class DataVerifier {

    //DataStates priority
    static final EnumMap<DataState,Integer> dataStatesPriority = new EnumMap<>(DataState.class);
    static {
        dataStatesPriority.put(DataState.INPUTED, 1);
        dataStatesPriority.put(DataState.EDITED, 2);
        dataStatesPriority.put(DataState.FORCED, 3);
        dataStatesPriority.put(DataState.COLLECTED, 4);
        dataStatesPriority.put(DataState.PREVIOUS, 5);
    }

    /**
     * Verify data format in all surveyUnits models
     * If there is at least 1 incorrect variable for a survey unit, a new SurveyUnitModel is created with "FORCED"
     * status
     * The new surveyUnits are added to the list
     * @param surveyUnitModelsList list of SurveyUnitModels to verify
     * @param variablesMap VariablesMap containing definitions of each variable
     */
    public static void verifySurveyUnits(List<SurveyUnitModel> surveyUnitModelsList, VariablesMap variablesMap){
        List<SurveyUnitModel> surveyUnitModelsListFormatted = new ArrayList<>(); // Created FORCED SU models

        for(String interrogationId : getInterrogationIds(surveyUnitModelsList)) { // For each id of the list
            List<SurveyUnitModel> srcSurveyUnitModelsOfInterrogationId = surveyUnitModelsList.stream().filter(element -> element.getInterrogationId().equals(interrogationId)).toList();
            List<VariableModel> correctedCollectedVariables = new ArrayList<>();
            List<VariableModel> correctedExternalVariables = new ArrayList<>();

            //Get corrected variables
            collectedVariablesManagement(srcSurveyUnitModelsOfInterrogationId, variablesMap, correctedCollectedVariables);
            externalVariablesManagement(srcSurveyUnitModelsOfInterrogationId, variablesMap, correctedExternalVariables);

            //Create FORCED if any corrected variable
            if(!correctedCollectedVariables.isEmpty() || !correctedExternalVariables.isEmpty()){
                SurveyUnitModel newFormattedSurveyUnitModel = createFormattedSurveyUnitModel(surveyUnitModelsList, interrogationId, correctedCollectedVariables, correctedExternalVariables);
                surveyUnitModelsListFormatted.add(newFormattedSurveyUnitModel);
            }
        }
        surveyUnitModelsList.addAll(surveyUnitModelsListFormatted);
    }

    private static SurveyUnitModel createFormattedSurveyUnitModel(
            List<SurveyUnitModel> surveyUnitModelsList,
            String interrogationId,
            List<VariableModel> correctedCollectedVariables,
            List<VariableModel> correctedExternalVariables
    ) {
        SurveyUnitModel sampleSurveyUnitModel = surveyUnitModelsList.stream().filter(element -> element.getInterrogationId().equals(interrogationId)).toList().getFirst();
        SurveyUnitModel newFormattedSurveyUnitModel = SurveyUnitModel.builder()
                .collectionInstrumentId(sampleSurveyUnitModel.getCollectionInstrumentId())
                .campaignId(sampleSurveyUnitModel.getCampaignId())
                .interrogationId(interrogationId)
                .usualSurveyUnitId(sampleSurveyUnitModel.getUsualSurveyUnitId())
                .state(DataState.FORMATTED)
                .mode(sampleSurveyUnitModel.getMode())
                .recordDate(LocalDateTime.now().plusSeconds(1)) // Add 1 second to avoid same recordDate as COLLECTED
                .fileDate(sampleSurveyUnitModel.getFileDate())
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        for(VariableModel correctedCollectedVariable : correctedCollectedVariables){
            newFormattedSurveyUnitModel.getCollectedVariables().add(
                VariableModel.builder()
                        .varId(correctedCollectedVariable.varId())
                        .value(correctedCollectedVariable.value())
                        .scope(correctedCollectedVariable.scope())
                        .iteration(correctedCollectedVariable.iteration())
                        .parentId(correctedCollectedVariable.parentId())
                        .build()
            );
        }

        for(VariableModel correctedExternalVariable : correctedExternalVariables){
            newFormattedSurveyUnitModel.getExternalVariables().add(
                    VariableModel.builder()
                            .varId(correctedExternalVariable.varId())
                            .value(correctedExternalVariable.value())
                            .scope(correctedExternalVariable.scope())
                            .iteration(correctedExternalVariable.iteration())
                            .parentId(correctedExternalVariable.parentId())
                            .build()
            );
        }
        return newFormattedSurveyUnitModel;
    }

    /**
     * Fetch individual interrogationIds of variable from the list
     * @param surveyUnitModelsList source list
     * @return a set of interrogationIds
     */

    private static Set<String> getInterrogationIds(List<SurveyUnitModel> surveyUnitModelsList) {
        Set<String> interrogationIds = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : surveyUnitModelsList){
            interrogationIds.add(surveyUnitModel.getInterrogationId());
        }

        return interrogationIds;
    }

    /**
     * Adds the collected variables for the FORCED document
     * @param srcSurveyUnitModelsOfInterrogationId source Survey Unit documents associated with InterrogationId
     * @param variablesMap variables definitions
     * @param correctedCollectedVariables FORCED document variables
     */
    private static void collectedVariablesManagement(List<SurveyUnitModel> srcSurveyUnitModelsOfInterrogationId, VariablesMap variablesMap, List<VariableModel> correctedCollectedVariables){
        Map<String,List<Integer>> variableIterations = new LinkedHashMap<>();

        List<VariableStateTuple> variablesToVerify = new ArrayList<>();

        //Sort from more priority to less
        List<SurveyUnitModel> sortedSurveyUnitModels = srcSurveyUnitModelsOfInterrogationId.stream().sorted(Comparator.comparing(surveyUnitModel -> dataStatesPriority.get(surveyUnitModel.getState()))).toList();

        //Get more priority variables to verify
        for(SurveyUnitModel srcSurveyUnitModel : sortedSurveyUnitModels){
            for(VariableModel collectedVariable : srcSurveyUnitModel.getCollectedVariables()){
                addIteration(collectedVariable, variableIterations, variablesToVerify, srcSurveyUnitModel.getState());
            }
        }

        //Verify variables
        for(VariableStateTuple collectedVariableToVerify : variablesToVerify){
            if(variablesMap.hasVariable(collectedVariableToVerify.variableModel().varId()))
            {
                VariableModel correctedCollectedVariable = verifyVariable(
                        collectedVariableToVerify.variableModel(),
                        variablesMap.getVariable(collectedVariableToVerify.variableModel().varId()),
                        collectedVariableToVerify.dataState()
                );

                if(correctedCollectedVariable != null){
                    correctedCollectedVariables.add(correctedCollectedVariable);
                }
            }
        }
    }

    private static void addIteration(VariableModel variableToCheck,
                                     Map<String, List<Integer>> variableIterations,
                                     List<VariableStateTuple> variablesToVerify,
                                     DataState dataState
                                     ) {
        String varIdToCheck = variableToCheck.varId();
        Integer iterationToCheck = variableToCheck.iteration();

        if(!variableIterations.containsKey(varIdToCheck)
                || !variableIterations.get(varIdToCheck).contains(iterationToCheck)){
            List<Integer> iterations = variableIterations.containsKey(varIdToCheck) ?
                    variableIterations.get(varIdToCheck)
                    : new ArrayList<>();
            if(!iterations.contains(iterationToCheck)){
                iterations.add(iterationToCheck);
            }
            variableIterations.put(
                    varIdToCheck,
                    iterations
            );

            variablesToVerify.add(new VariableStateTuple(variableToCheck, dataState));
        }
    }

    private static VariableModel verifyVariable(
            VariableModel variableModel,
            fr.insee.bpm.metadata.model.Variable variableDefinition,
            DataState dataState
    ) {
        if(isParseError(variableModel.value(), variableDefinition.getType(),dataState)){
            return VariableModel.builder()
                    .varId(variableModel.varId())
                    .value("")
                    .scope(variableModel.scope())
                    .iteration(variableModel.iteration())
                    .parentId(variableModel.parentId())
                    .build();
        }
        return null;
    }

    private static void externalVariablesManagement(List<SurveyUnitModel> srcSuModels, VariablesMap variablesMap, List<VariableModel> correctedExternalVariables) {
        //External variables are in COLLECTED documents only
        Optional<SurveyUnitModel> surveyUnitModelOptional = srcSuModels.stream().filter(
                surveyUnitModel -> surveyUnitModel.getState().equals(DataState.COLLECTED)
        ).findFirst();

        //Verify variables
        if(surveyUnitModelOptional.isPresent()){
            DataState state = surveyUnitModelOptional.get().getState();
            for(VariableModel externalVariable: surveyUnitModelOptional.get().getExternalVariables()){
                if(variablesMap.hasVariable(externalVariable.varId())) {
                    VariableModel correctedExternalVariable = verifyVariable(
                            externalVariable,
                            variablesMap.getVariable(externalVariable.varId()),
                            state
                    );
                    if (correctedExternalVariable != null) {
                        correctedExternalVariables.add(correctedExternalVariable);
                    }
                }
            }
        }
    }

    /**
     * Use the correct parser and try to parse
     * @param value value to verify
     * @param type type of the variable
     * @param state state of the data where the variable is contained in
     * @return true if the value is not conform to the variable type
     */
    private static boolean isParseError(String value, VariableType type, DataState state){
        //Allow null values
        if(value == null){
            return !(state.equals(DataState.EDITED)
                    || state.equals(DataState.FORCED)
                    || state.equals(DataState.FORMATTED)); //Return false if datastate one of those
        }
        switch(type){
            case BOOLEAN:
                if(!value.equals("true")
                        &&!value.equals("false")
                        &&!value.isEmpty()) {
                    return true;
                }
                break;
            case DATE:
                Pattern pattern = Pattern.compile(Constants.DATE_REGEX);
                Matcher matcher = pattern.matcher(value);
                if(!matcher.find()){
                    // We only monitor parsing date errors, so we always return false
                    log.debug("Can't parse date {}", value);
                    return false;
                }
                break;
            case INTEGER:
                try{
                    Integer.parseInt(value);
                }catch (NumberFormatException e){
                    return true;
                }
                break;
            case NUMBER:
                try{
                    Double.parseDouble(value);
                }catch (NumberFormatException e){
                    return true;
                }
                break;
            default:
                break;
        }
        return false;
    }
}
