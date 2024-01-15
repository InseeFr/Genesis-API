package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.CorrectedExternalVariable;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.CorrectedCollectedVariable;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Slf4j
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
        List<SurveyUnitUpdateDto> suDtosListForced = new ArrayList<>(); // Created FORCED SU DTOs

        for(String idUE : getIdUEs(suDtosList)) { // For each id of the list
            List<SurveyUnitUpdateDto> srcSuDtosOfIdUE = suDtosList.stream().filter(element -> element.getIdUE().equals(idUE)).toList();
            List<CorrectedCollectedVariable> correctedCollectedVariables = new ArrayList<>();
            List<CorrectedExternalVariable> correctedExternalVariables = new ArrayList<>();

            variablesManagement(srcSuDtosOfIdUE, variablesMap, correctedCollectedVariables, correctedExternalVariables);

            //Create FORCED if any corrected variable
            if(!correctedCollectedVariables.isEmpty() || !correctedExternalVariables.isEmpty()){
                SurveyUnitUpdateDto sampleSuDto = suDtosList.stream().filter(element -> element.getIdUE().equals(idUE)).toList().get(0);
                SurveyUnitUpdateDto newForcedSuDto = SurveyUnitUpdateDto.builder()
                        .idQuest(sampleSuDto.getIdQuest())
                        .idCampaign(sampleSuDto.getIdCampaign())
                        .idUE(idUE)
                        .state(DataState.FORCED)
                        .mode(sampleSuDto.getMode())
                        .recordDate(LocalDateTime.now())
                        .fileDate(sampleSuDto.getFileDate())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                for(CorrectedCollectedVariable correctedCollectedVariable : correctedCollectedVariables){
                    newForcedSuDto.getCollectedVariables().add(
                            new CollectedVariableDto(correctedCollectedVariable.getIdVar(),
                                    correctedCollectedVariable.getValues()
                            ,correctedCollectedVariable.getIdLoop()
                            ,correctedCollectedVariable.getIdParent()
                            )
                    );
                }

                for(CorrectedExternalVariable correctedExternalVariable : correctedExternalVariables){
                    newForcedSuDto.getExternalVariables().add(
                            VariableDto.builder()
                                    .idVar(correctedExternalVariable.getIdVar())
                                    .values(correctedExternalVariable.getValues())
                                    .build()
                            );
                }

                suDtosListForced.add(newForcedSuDto);
            }

        }
        suDtosList.addAll(suDtosListForced);
    }

    private static void variablesManagement(List<SurveyUnitUpdateDto> srcSuDtosOfIdUE,
                                            VariablesMap variablesMap,
                                            List<CorrectedCollectedVariable> correctedCollectedVariables,
                                            List<CorrectedExternalVariable> correctedExternalVariables) {

        collectedVariablesManagement(srcSuDtosOfIdUE, variablesMap, correctedCollectedVariables);
        //External variables management
        for (SurveyUnitUpdateDto srcSuDtoOfIdUE : srcSuDtosOfIdUE){
            if(srcSuDtoOfIdUE.getState().equals(DataState.COLLECTED))
                correctedExternalVariables.addAll(verifyExternalVariables(srcSuDtoOfIdUE.getExternalVariables(), variablesMap));
        }
    }

    /**
     * Fetch individual IdUEs of variable from the list
     * @param suDtosList source list
     * @return a set of IdUEs
     */

    private static Set<String> getIdUEs(List<SurveyUnitUpdateDto> suDtosList) {
        Set<String> idUEs = new HashSet<>();
        for(SurveyUnitUpdateDto surveyUnitUpdateDto : suDtosList){
            idUEs.add(surveyUnitUpdateDto.getIdUE());
        }

        return idUEs;
    }

    /**
     * Adds the collected variables for the FORCED document
     * @param srcSuDtosOfIdUE source Survey Unit documents associated with IdUE
     * @param variablesMap variables definitions
     * @param correctedCollectedVariables FORCED document variables
     */
    private static void collectedVariablesManagement(List<SurveyUnitUpdateDto> srcSuDtosOfIdUE, VariablesMap variablesMap, List<CorrectedCollectedVariable> correctedCollectedVariables){
        HashMap<String, CollectedVariableDto> validVariablesMap = new HashMap<>();

        for (SurveyUnitUpdateDto srcSuDtoOfIdUE : srcSuDtosOfIdUE) {
            //Verify and gets the corrected invalid variables
            List<CorrectedCollectedVariable> correctedCollectedVariablesOfSrcSuDto = verifyCollectedVariables(srcSuDtoOfIdUE.getCollectedVariables(), variablesMap, srcSuDtoOfIdUE.getState());

            //For each source variable of SuDto
            for (CollectedVariableDto srcCollectedVariable : srcSuDtoOfIdUE.getCollectedVariables()) {
                //Get the corrected version of that variable of that SuDto
                CorrectedCollectedVariable suDtoCorrectedCollectedVariable = getCorrectedCollectedVariableFromList(srcCollectedVariable, correctedCollectedVariablesOfSrcSuDto);
                //Get the corrected variable from the final list
                CorrectedCollectedVariable forcedCorrectedVariable = getCorrectedCollectedVariableFromList(srcCollectedVariable, correctedCollectedVariables);

                //If SuDTO variable valid and not invalid yet
                if(suDtoCorrectedCollectedVariable == null && forcedCorrectedVariable == null){
                    validVariablesMap.put(srcCollectedVariable.getIdVar(), srcCollectedVariable);
                }

                //If SuDTO variable invalid and not invalid yet
                if(suDtoCorrectedCollectedVariable != null && forcedCorrectedVariable == null){
                    //If valid value already found
                    if(validVariablesMap.containsKey(suDtoCorrectedCollectedVariable.getIdVar())){
                        CollectedVariableDto validVariable = validVariablesMap.get(suDtoCorrectedCollectedVariable.getIdVar());
                        replaceInvalidValues(validVariable,suDtoCorrectedCollectedVariable);
                    }
                    correctedCollectedVariables.add(suDtoCorrectedCollectedVariable);
                }

                //If SuDTO variable invalid and has been invalidated before
                if(suDtoCorrectedCollectedVariable != null && forcedCorrectedVariable != null){
                    mergeCorrectedCollectedVariablesValues(suDtoCorrectedCollectedVariable, forcedCorrectedVariable);
                }

                //If SuDTO variable valid and has been invalidated before
                if(suDtoCorrectedCollectedVariable == null && forcedCorrectedVariable != null){
                    correctedCollectedVariables.remove(forcedCorrectedVariable);
                }
            }
        }
    }

    /**
     * Replaces invalid/empty values with the SU DTO ones
     * @param src source Variable from SU DTO
     * @param dst FORCED Variable
     */
    private static void replaceInvalidValues(CollectedVariableDto src, CorrectedCollectedVariable dst) {
        for(int valueIndex = 0; valueIndex < dst.getValues().size(); valueIndex++){
            if(dst.getValues().get(valueIndex).isEmpty()){
                dst.getValues().set(valueIndex, src.getValues().get(valueIndex));
            }
        }
    }

    /**
     * Gets the maximum priority rank of CorrectedCollectedVariable values
     * @param correctedCollectedVariable CorrectedCollectedVariable to look for
     * @return The maximum priority rank
     */
    private static Integer getMaxPriority(CorrectedCollectedVariable correctedCollectedVariable) {
        Integer maxPriority = null;
        for(int valueIndex : correctedCollectedVariable.getValueIndexDataStateMap().keySet()){
            DataState dataState = correctedCollectedVariable.getValueIndexDataStateMap().get(valueIndex);
            int priority = dataStatesPriority.get(dataState);
            if(maxPriority == null || maxPriority > priority){
                maxPriority = priority;
            }
        }
        return maxPriority;
    }

    /**
     * @param variablesToVerify variables to verify
     * @param variablesMap variable definitions
     * @param dataState data state of the source SurveyUnitUpdateDto
     * @return a list of CorrectedCollectedVariables containing variables to add to the FORCED dto
     */
    private static List<CorrectedCollectedVariable> verifyCollectedVariables(List<? extends CollectedVariableDto> variablesToVerify, VariablesMap variablesMap, DataState dataState){
        List<CorrectedCollectedVariable> correctedCollectedVariables = new ArrayList<>();
        if(variablesToVerify != null){
            for (CollectedVariableDto variable : variablesToVerify){
                if (variablesMap.getVariable(variable.getIdVar()) != null) {
                    Variable variableDefinition = variablesMap.getVariable(variable.getIdVar());

                    CorrectedCollectedVariable correctedCollectedVariable = verifyCollectedVariable(variable, variableDefinition, dataState);
                    if(correctedCollectedVariable != null){
                        correctedCollectedVariables.add(correctedCollectedVariable);
                    }
                }
            }
        }
        return correctedCollectedVariables;
    }



    /**
     * Verify one collected variable
     * @param collectedVariable collected variable DTO to verify
     * @param variableDefinition variable definition of the variable
     * @param dataState data state of the source SurveyUnitUpdateDto
     * @return a CorrectedCollectedVariable if there is any parsing error, null otherwise
     */
    private static CorrectedCollectedVariable verifyCollectedVariable(CollectedVariableDto collectedVariable, Variable variableDefinition, DataState dataState) {
        CorrectedCollectedVariable correctedCollectedVariable = null;

        int valueIndex = 0;
        for (String value : collectedVariable.getValues()) {
            if(isParseError(value, variableDefinition.getType())){
                if(correctedCollectedVariable == null)
                    correctedCollectedVariable = new CorrectedCollectedVariable(
                            collectedVariable.getIdVar(),
                            new ArrayList<>(collectedVariable.getValues()),
                            collectedVariable.getIdLoop(),
                            collectedVariable.getIdParent(),
                            dataState);

                correctedCollectedVariable.getValues().set(valueIndex,"");
                correctedCollectedVariable.getIncorrectValueIndexes().add(valueIndex);
            }
            valueIndex++;
        }



        return correctedCollectedVariable;
    }

    /**
     * Merges two CorrectedCollectedVariable objects values from src to dst while applying rules
     * @param src source CorrectedCollectedVariable
     * @param dst destination CorrectedCollectedVariable
     */

    private static void mergeCorrectedCollectedVariablesValues(CorrectedCollectedVariable src, CorrectedCollectedVariable dst) {
        //For each dst value
        int dstValueIndex = 0;
        while(dstValueIndex < dst.getValues().size()){
            if(src.getValues().size() > dstValueIndex)
                mergeCorrectedCollectedVariableValue(src,dst,dstValueIndex);
            dstValueIndex++;
        }

        //Add src value if dst values list smaller than src
        while(src.getValues().size() > dstValueIndex){
            String srcValue = src.getValues().get(dstValueIndex);
            dst.getValues().add(srcValue);
            dstValueIndex++;
        }

    }

    /**
     * Merges two values from src to dst while applying rules
     * @param src source CorrectedCollectedVariable
     * @param dst destination CorrectedCollectedVariable
     * @param dstValueIndex index of value
     */
    private static void mergeCorrectedCollectedVariableValue(CorrectedCollectedVariable src, CorrectedCollectedVariable dst, int dstValueIndex) {
        String srcValue = src.getValues().get(dstValueIndex);
        boolean isSrcValueCorrect = !src.getIncorrectValueIndexes().contains(dstValueIndex);
        boolean isDstValueCorrect = !dst.getIncorrectValueIndexes().contains(dstValueIndex);

        //Replace if dst invalid and src valid
        if(!srcValue.isEmpty()
                && !isDstValueCorrect
                && isSrcValueCorrect) {
            replaceCorrectedCollectedVariableValue(src,dst,dstValueIndex);
        }

        //Replace if src priority and valid
        int srcPriority = dataStatesPriority.get(src.getValueIndexDataStateMap().get(dstValueIndex));
        int dstPriority = dataStatesPriority.get(dst.getValueIndexDataStateMap().get(dstValueIndex));

        if(srcPriority < dstPriority && isSrcValueCorrect){
            replaceCorrectedCollectedVariableValue(src,dst,dstValueIndex);
        }

        //Replace by empty if both values invalid
        if(!isSrcValueCorrect && !isDstValueCorrect){
            dst.getValues().set(dstValueIndex,"");
            dst.getIncorrectValueIndexes().remove(dstValueIndex);
            dst.getValueIndexDataStateMap().put(dstValueIndex,src.getValueIndexDataStateMap().get(dstValueIndex));
        }
    }

    /**
     * replaces one value from src CorrectedCollectedVariable to dst
     * @param src source CorrectedCollectedVariable
     * @param dst destination CorrectedCollectedVariable
     * @param index index of the value to replace
     */
    private static void replaceCorrectedCollectedVariableValue(CorrectedCollectedVariable src, CorrectedCollectedVariable dst, int index) {
        dst.getValues().set(index, src.getValues().get(index));
        dst.getIncorrectValueIndexes().remove(index);
        dst.getValueIndexDataStateMap().put(index,src.getValueIndexDataStateMap().get(index));
    }

    /**
     * Gets the CorrectedCollectedVariable object from the list
     * @param collectedVariable variable to look for
     * @param correctedCollectedVariables list of CorrectedCollectedVariables
     * @return the CorrectedCollectedVariable from the list, null if not found
     */
    private static CorrectedCollectedVariable getCorrectedCollectedVariableFromList(CollectedVariableDto collectedVariable, List<CorrectedCollectedVariable> correctedCollectedVariables) {
        for(CorrectedCollectedVariable correctedCollectedVariableInList : correctedCollectedVariables){
            if(correctedCollectedVariableInList.getIdVar().equals(collectedVariable.getIdVar()))
                return correctedCollectedVariableInList;
        }

        return null;
    }

    /**
     * @param variablesToVerify variables to verify
     * @param variablesMap variable definitions
     * @return a list of CorrectedExternalVariables containing variables to add to the FORCED dto
     */
    private static List<CorrectedExternalVariable> verifyExternalVariables(List<? extends VariableDto> variablesToVerify, VariablesMap variablesMap){
        List<CorrectedExternalVariable> correctedExternalVariables = new ArrayList<>();
        if(variablesToVerify != null){
            for (VariableDto variable : variablesToVerify){
                if (variablesMap.getVariable(variable.getIdVar()) != null) {
                    Variable variableDefinition = variablesMap.getVariable(variable.getIdVar());

                    CorrectedExternalVariable correctedExternalVariable = verifyExternalVariable(variable, variableDefinition);
                    if(correctedExternalVariable != null){
                        correctedExternalVariables.add(correctedExternalVariable);
                    }
                }
            }
        }
        return correctedExternalVariables;
    }

    /**
     * Verify one external variable
     * @param externalVariable external variable DTO to verify
     * @param variableDefinition variable definition of the variable
     * @return a CorrectedExternalVariable if there is any parsing error, null otherwise
     */
    private static CorrectedExternalVariable verifyExternalVariable(VariableDto externalVariable, Variable variableDefinition) {
        CorrectedExternalVariable correctedExternalVariable = null;

        int valueIndex = 0;
        for (String value : externalVariable.getValues()) {
            if(isParseError(value, variableDefinition.getType())){
                if(correctedExternalVariable == null)
                    correctedExternalVariable = new CorrectedExternalVariable(externalVariable.getIdVar(),
                            externalVariable.getValues());

                correctedExternalVariable.getValues().set(valueIndex,"");
                correctedExternalVariable.getIncorrectValueIndexes().add(valueIndex);
            }
            valueIndex++;
        }

        return correctedExternalVariable;
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
}
