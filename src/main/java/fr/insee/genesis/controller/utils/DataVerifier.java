package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
public class DataVerifier {

    //Concerned DataStates
    static final List<DataState> concernedDataStates = Arrays.asList(DataState.COLLECTED, DataState.EDITED);

    private DataVerifier() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Verify data format in all surveyUnits DTOs
     * If there is at least 1 incorrect variable for a survey unit, a new SurveyUnitUpdateDto is created with "FORCED" status
     * The new surveyUnitUpdates are added to the list
     * @param suDtosList list of SurveyUnitUpdateDtos to verify
     * @param variablesMap VariablesMap containing definitions of each variable
     */
    public static void verifySurveyUnits(List<SurveyUnitUpdateDto> suDtosList, VariablesMap variablesMap){
        List<SurveyUnitUpdateDto> suDtosListForced = new ArrayList<>();
        SurveyUnitUpdateDto collectedSuDto = null;
        List<String[]> incorrectCOLLECTEDCollectedVariablesTuples = null;

        for(SurveyUnitUpdateDto suDto : suDtosList){
            if (suDto.getState().equals(DataState.COLLECTED)){
                collectedSuDto = suDto;
            }
            if(concernedDataStates.contains(suDto.getState())){
                //Pairs(variable name, incorrect value index)
                List<String[]> incorrectCollectedVariablesTuples = verifyVariables(suDto.getCollectedVariables(), variablesMap);
                List<String[]> incorrectExternalVariablesTuples = verifyVariables(suDto.getExternalVariables(), variablesMap);

                // If variable has at least 1 incorrect value
                // create new survey unit
                // change the incorrect value to COLLECTED if multiple value
                // exclude variable or change to COLLECTED if only value
                if(incorrectCollectedVariablesTuples.size() + incorrectExternalVariablesTuples.size() > 0
                        && suDtosListForced.stream().filter(surveyUnitUpdateDto -> surveyUnitUpdateDto.getIdUE().equals(suDto.getIdUE())).findFirst().isEmpty()
                ){
                    SurveyUnitUpdateDto newSuDtoForced = suDto.buildForcedSurveyUnitUpdate();

                    if (!incorrectCollectedVariablesTuples.isEmpty()) {
                        if(suDto.getState() == DataState.COLLECTED) {
                            incorrectCOLLECTEDCollectedVariablesTuples = incorrectCollectedVariablesTuples;
                            collectedVariablesManagement(suDto, newSuDtoForced, incorrectCollectedVariablesTuples, null, null);
                        }else
                            collectedVariablesManagement(suDto, newSuDtoForced, incorrectCollectedVariablesTuples, collectedSuDto , incorrectCOLLECTEDCollectedVariablesTuples);
                    }

                    if (!incorrectExternalVariablesTuples.isEmpty())
                        externalVariablesManagement(suDto, newSuDtoForced, incorrectExternalVariablesTuples);

                    suDtosListForced.add(newSuDtoForced);
                }
                if(incorrectCollectedVariablesTuples.size() + incorrectExternalVariablesTuples.size() == 0
                        && suDto.getState().equals(DataState.EDITED)) {
                    // Don't create FORCED if EDITED is valid
                    suDtosListForced.removeIf(surveyUnitUpdateDto -> surveyUnitUpdateDto.getIdUE().equals(suDto.getIdUE()));
                }
            }
        }
        suDtosList.addAll(suDtosListForced);
    }

    /**
     * @param variablesToVerify variables to verify
     * @param variablesMap variable definitions
     * @return a list of pairs (variable name, index)
     */
    private static List<String[]> verifyVariables(List<? extends VariableDto> variablesToVerify, VariablesMap variablesMap){
        // List of tuples
        List<String[]> incorrectVariables = new ArrayList<>();
        if(variablesToVerify != null){
            for (VariableDto variable : variablesToVerify){
                if (variablesMap.getVariable(variable.getIdVar()) != null) {
                    Variable variableDefinition = variablesMap.getVariable(variable.getIdVar());
                    int valueIndex = 0;
                    for (String value : variable.getValues()) {
                        if(isParseError(value, variableDefinition.getType())){
                            incorrectVariables.add(new String[]{variable.getIdVar(), Integer.toString(valueIndex)});
                        }
                        valueIndex++;
                    }
                }
            }
        }
        return incorrectVariables;
    }

    /**
     * Use the correct parser and try to parse
     * @param value value to verify
     * @param type type of the variable
     * @return true if the value is not conform to the variable type
     */
    private static boolean isParseError(String value, VariableType type){
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
                    log.warn("Can't parse date " + value);
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

    /**
     * Changes values flagged as incorrect in update variables from source and fill the destination's update variables
     * A third survey unit can be used to replace the incorrect variable from source by the correct one from the third
     * @param sourceSurveyUnitUpdateDto source Survey Unit
     * @param destinationSurveyUnitUpdateDto destination Survey Unit
     * @param incorrectCollectedVariablesTuples (incorrect variable name, incorrect value index) pairs
     * @param replacementSurveyUnitUpdateDto the Survey Unit used for incorrect variable replacement
     * @param incorrectReplacementCollectedVariablesTuples incorrect variable tuples for the replacement survey unit
     */
    private static void collectedVariablesManagement(
            SurveyUnitUpdateDto sourceSurveyUnitUpdateDto
            ,SurveyUnitUpdateDto destinationSurveyUnitUpdateDto
            ,List<String[]> incorrectCollectedVariablesTuples
            ,SurveyUnitUpdateDto replacementSurveyUnitUpdateDto
            ,List<String[]> incorrectReplacementCollectedVariablesTuples
    ){
        //Variable names extraction
        Set<String> incorrectVariablesNames = new HashSet<>();
        getIncorrectVariableNames(incorrectCollectedVariablesTuples, incorrectVariablesNames);

        int collectedVariableIndex = 0;
        for (CollectedVariableDto variable : sourceSurveyUnitUpdateDto.getCollectedVariables()){
            if(incorrectVariablesNames.contains(variable.getIdVar())){
                //Copy variable
                CollectedVariableDto newVariable = CollectedVariableDto.collectedVariableBuilder()
                        .idVar(variable.getIdVar())
                        .idLoop(variable.getIdLoop())
                        .idParent(variable.getIdParent())
                        .values(new ArrayList<>(variable.getValues()))
                        .build();
                //Change incorrect value(s) to empty
                for(int incorrectValueIndex : getIncorrectValuesIndexes(incorrectCollectedVariablesTuples, variable.getIdVar())){
                    if(replacementSurveyUnitUpdateDto == null
                        || !getIncorrectValuesIndexes(incorrectReplacementCollectedVariablesTuples, variable.getIdVar()).contains(incorrectValueIndex)
                    )
                        newVariable.getValues().set(incorrectValueIndex,"");
                    else
                        newVariable.getValues().set(incorrectValueIndex, replacementSurveyUnitUpdateDto.getCollectedVariables().get(collectedVariableIndex).getValues().get(incorrectValueIndex));
                }
                destinationSurveyUnitUpdateDto.getCollectedVariables().add(newVariable);
            }
            collectedVariableIndex++;
        }
    }

    /**
     * changes values flagged as incorrect in update variables from source and fill the destination's external variables
     * @param sourceSurveyUnitUpdateDto source Survey Unit
     * @param destinationSurveyUnitUpdateDto destination Survey Unit
     * @param incorrectExternalVariablesTuples (incorrect variable name, incorrect value index) pairs
     */
    private static void externalVariablesManagement(
            SurveyUnitUpdateDto sourceSurveyUnitUpdateDto
            ,SurveyUnitUpdateDto destinationSurveyUnitUpdateDto
            ,List<String[]> incorrectExternalVariablesTuples
    ){
        //Variable names extraction
        Set<String> incorrectVariablesNames = new HashSet<>();
        getIncorrectVariableNames(incorrectExternalVariablesTuples, incorrectVariablesNames);

        for (VariableDto variable : sourceSurveyUnitUpdateDto.getExternalVariables()){
            if(incorrectVariablesNames.contains(variable.getIdVar())){
                //Copy variable
                VariableDto newVariable = VariableDto.builder()
                        .idVar(variable.getIdVar())
                        .values(new ArrayList<>(variable.getValues()))
                        .build();
                //Change incorrect value(s) to empty
                for(int incorrectValueIndex : getIncorrectValuesIndexes(incorrectExternalVariablesTuples, variable.getIdVar()))
                    newVariable.getValues().set(incorrectValueIndex,"");
                destinationSurveyUnitUpdateDto.getExternalVariables().add(newVariable);
            }
        }
    }

    /**
     * Go through a list of tuples(variable name, incorrect value index) and add variable names to a set
     * @param incorrectVariablesPairs tuples to iterate through
     * @param incorrectVariablesNames set to add to
     */
    private static void getIncorrectVariableNames(List<String[]> incorrectVariablesPairs, Set<String> incorrectVariablesNames) {
        for(String[] tuple : incorrectVariablesPairs){
            incorrectVariablesNames.add(tuple[0]);
        }
    }

    /**
     * @param incorrectVariablesPairs tuples to iterate through
     * @param incorrectVariableName name of the variable to look for
     * @return the indexes of incorrect values
     */
    private static List<Integer> getIncorrectValuesIndexes(List<String[]> incorrectVariablesPairs, String incorrectVariableName) {
        List<Integer> incorrectValuesIndexes = new ArrayList<>();
        for(String[] tuple : incorrectVariablesPairs){
            if(tuple[0].equals(incorrectVariableName)){
                incorrectValuesIndexes.add(Integer.valueOf(tuple[1]));
            }
        }
        return incorrectValuesIndexes;
    }

}
