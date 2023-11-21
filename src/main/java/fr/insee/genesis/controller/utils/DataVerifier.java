package fr.insee.genesis.controller.utils;

import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.domain.dtos.ExternalVariableDto;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableStateDto;

import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DataVerifier {
    private DataVerifier() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Verify data format in all surveyUnits DTOs
     * If there is at least 1 incorrect variable, a new SurveyUnitUpdateDto is created with "FORCED" status
     * The new surveyUnitUpdate is added to the list
     * @param suDtosList list of SurveyUnitUpdateDtos to verify
     * @param variablesMap VariablesMap containing definitions of each variable
     */
    public static void verifySurveyUnits(List<SurveyUnitUpdateDto> suDtosList, VariablesMap variablesMap){
        //To avoid live edit of suDtoList while reading it
        List<SurveyUnitUpdateDto> suDtosListCopy = new ArrayList<>(suDtosList);

        for(SurveyUnitUpdateDto suDto : suDtosListCopy){
            //Pairs(variable name, incorrect value index)
            List<String[]> incorrectUpdateVariablesTuples = verifyUpdateVariables(suDto.getVariablesUpdate(), variablesMap);
            List<String[]> incorrectExternalVariablesTuples = verifyExternalVariables(suDto.getExternalVariables(), variablesMap);

            // If variable has at least 1 incorrect value
            // create new survey unit
            // change the incorrect value to empty if multiple value
            // exclude variable if only value
            if(incorrectUpdateVariablesTuples.size() + incorrectExternalVariablesTuples.size() > 0) {
                SurveyUnitUpdateDto newSuDtoForced = suDto.buildForcedSurveyUnitUpdate();

                if(!incorrectUpdateVariablesTuples.isEmpty())
                    updateVariablesManagement(suDto,newSuDtoForced,incorrectUpdateVariablesTuples);

                if(!incorrectExternalVariablesTuples.isEmpty())
                    externalVariablesManagement(suDto,newSuDtoForced,incorrectExternalVariablesTuples);

                suDtosList.add(newSuDtoForced);
            }

        }
    }

    /**
     * @param variablesToVerify update variables to verify
     * @param variablesMap variable definitions
     * @return a list of pairs (variable name, index)
     */
    private static List<String[]> verifyUpdateVariables(List<VariableStateDto> variablesToVerify, VariablesMap variablesMap){
        List<String[]> incorrectVariables = new ArrayList<>();
        for (VariableStateDto variable : variablesToVerify){
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

        return incorrectVariables;
    }

    /**
     * @param variablesToVerify external variables to verify
     * @param variablesMap variable definitions
     * @return a list of pairs (variable name, index)
     */
    private static List<String[]> verifyExternalVariables(List<ExternalVariableDto> variablesToVerify, VariablesMap variablesMap){
        // List of tuples (
        List<String[]> incorrectVariables = new ArrayList<>();

        for (ExternalVariableDto variable : variablesToVerify){
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
                try{
                    LocalDateTime.parse(value);
                }catch (DateTimeParseException e){
                    return true;
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
     * changes values flagged as incorrect in update variables from source and fill the destination's update variables
     * @param sourceSurveyUnitUpdateDto source Survey Unit
     * @param destinationSurveyUnitUpdateDto destination Survey Unit
     * @param incorrectUpdateVariablesTuples (incorrect variable name, incorrect value index) pairs
     */
    private static void updateVariablesManagement(
            SurveyUnitUpdateDto sourceSurveyUnitUpdateDto
            ,SurveyUnitUpdateDto destinationSurveyUnitUpdateDto
            ,List<String[]> incorrectUpdateVariablesTuples
    ){
        //Variable names extraction
        Set<String> incorrectVariablesNames = new HashSet<>();
        getIncorrectVariableNames(incorrectUpdateVariablesTuples, incorrectVariablesNames);

        for (VariableStateDto variable : sourceSurveyUnitUpdateDto.getVariablesUpdate()){
            if(incorrectVariablesNames.contains(variable.getIdVar())){
                //Copy variable
                VariableStateDto newVariable = VariableStateDto.builder()
                        .idVar(variable.getIdVar())
                        .idLoop(variable.getIdLoop())
                        .idParent(variable.getIdParent())
                        .values(new ArrayList<>(variable.getValues()))
                        .build();
                //Change incorrect value(s) to empty
                for(int incorrectValueIndex : getIncorrectValuesIndexes(incorrectUpdateVariablesTuples, variable.getIdVar()))
                    newVariable.getValues().set(incorrectValueIndex,"");
                destinationSurveyUnitUpdateDto.getVariablesUpdate().add(newVariable);
            }
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

        for (ExternalVariableDto variable : sourceSurveyUnitUpdateDto.getExternalVariables()){
            if(incorrectVariablesNames.contains(variable.getIdVar())){
                //Copy variable
                ExternalVariableDto newVariable = ExternalVariableDto.builder()
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

    //TODO Tester une nouvelle fois puis commit push et si OK pull request

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
