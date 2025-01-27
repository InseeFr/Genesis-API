package fr.insee.genesis.domain.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
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
     * If there is at least 1 incorrect variable for a survey unit, a new SurveyUnitDto is created with "FORCED" status
     * The new surveyUnits are added to the list
     * @param suDtosList list of SurveyUnitDtos to verify
     * @param variablesMap VariablesMap containing definitions of each variable
     */
    public static void verifySurveyUnits(List<SurveyUnitModel> suDtosList, VariablesMap variablesMap){
        List<SurveyUnitModel> suDtosListForced = new ArrayList<>(); // Created FORCED SU DTOs

        for(String idUE : getIdUEs(suDtosList)) { // For each id of the list
            List<SurveyUnitModel> srcSuDtosOfIdUE = suDtosList.stream().filter(element -> element.getIdUE().equals(idUE)).toList();
            List<VariableModel> correctedCollectedVariables = new ArrayList<>();
            List<VariableModel> correctedExternalVariables = new ArrayList<>();

            //Get corrected variables
            collectedVariablesManagement(srcSuDtosOfIdUE, variablesMap, correctedCollectedVariables);
            externalVariablesManagement(srcSuDtosOfIdUE, variablesMap, correctedExternalVariables);

            //Create FORCED if any corrected variable
            if(!correctedCollectedVariables.isEmpty() || !correctedExternalVariables.isEmpty()){
                SurveyUnitModel newForcedSuDto = createForcedDto(suDtosList, idUE, correctedCollectedVariables, correctedExternalVariables);
                suDtosListForced.add(newForcedSuDto);
            }
        }
        suDtosList.addAll(suDtosListForced);
    }

    private static SurveyUnitModel createForcedDto(
            List<SurveyUnitModel> suDtosList,
            String idUE,
            List<VariableModel> correctedCollectedVariables,
            List<VariableModel> correctedExternalVariables
    ) {
        SurveyUnitModel sampleSuDto = suDtosList.stream().filter(element -> element.getIdUE().equals(idUE)).toList().getFirst();
        SurveyUnitModel newForcedSuDto = SurveyUnitModel.builder()
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

        for(VariableModel correctedCollectedVariable : correctedCollectedVariables){
            newForcedSuDto.getCollectedVariables().add(
                VariableModel.builder()
                        .idVar(correctedCollectedVariable.idVar())
                        .values(correctedCollectedVariable.values())
                        .idLoop(correctedCollectedVariable.idLoop())
                        .idParent(correctedCollectedVariable.idParent())
                        .build()
            );
        }

        for(VariableModel correctedExternalVariable : correctedExternalVariables){
            newForcedSuDto.getExternalVariables().add(
                    VariableModel.builder()
                            .idVar(correctedExternalVariable.idVar())
                            .values(correctedExternalVariable.values())
                            .idLoop(correctedExternalVariable.idLoop())
                            .idParent(correctedExternalVariable.idParent())
                            .build()
            );
        }
        return newForcedSuDto;
    }

    /**
     * Fetch individual IdUEs of variable from the list
     * @param suDtosList source list
     * @return a set of IdUEs
     */

    private static Set<String> getIdUEs(List<SurveyUnitModel> suDtosList) {
        Set<String> idUEs = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : suDtosList){
            idUEs.add(surveyUnitModel.getIdUE());
        }

        return idUEs;
    }

    /**
     * Adds the collected variables for the FORCED document
     * @param srcSuDtosOfIdUE source Survey Unit documents associated with IdUE
     * @param variablesMap variables definitions
     * @param correctedCollectedVariables FORCED document variables
     */
    private static void collectedVariablesManagement(List<SurveyUnitModel> srcSuDtosOfIdUE, VariablesMap variablesMap, List<VariableModel> correctedCollectedVariables){
        Set<String> variableNames = new HashSet<>();
        List<VariableModel> variablesToVerify = new ArrayList<>();

        //Sort from more priority to less
        List<SurveyUnitModel> sortedSuDtos = srcSuDtosOfIdUE.stream().sorted(Comparator.comparing(surveyUnitDto -> dataStatesPriority.get(surveyUnitDto.getState()))).toList();

        //Get more priority variables to verify
        for(SurveyUnitModel srcSuDto : sortedSuDtos){
            for(VariableModel collectedVariable : srcSuDto.getCollectedVariables()){
                if(!variableNames.contains(collectedVariable.idVar())){
                    variableNames.add(collectedVariable.idVar());
                    variablesToVerify.add(collectedVariable);
                }
            }
        }

        //Verify variables
        for(VariableModel collectedVariableToVerify : variablesToVerify){
            if(variablesMap.hasVariable(collectedVariableToVerify.idVar()))
            {
                VariableModel correctedCollectedVariable = verifyCollectedVariable(
                        collectedVariableToVerify,
                        variablesMap.getVariable(collectedVariableToVerify.idVar())
                );

                if(correctedCollectedVariable != null){
                    correctedCollectedVariables.add(correctedCollectedVariable);
                }
            }
        }
    }

    private static VariableModel verifyCollectedVariable(VariableModel collectedVariable, fr.insee.bpm.metadata.model.Variable variableDefinition) {
        List<String> newValues = new ArrayList<>();
        boolean isInvalid = false;

        for (String value : collectedVariable.values()){
            if(isParseError(value, variableDefinition.getType())){
                isInvalid = true;
                newValues.add("");
            }else{
                newValues.add(value);
            }
        }


        return isInvalid ? VariableModel.builder()
                .idVar(collectedVariable.idVar())
                .values(newValues)
                .idLoop(collectedVariable.idLoop())
                .idParent(collectedVariable.idParent())
                .build() : null;
    }

    private static void externalVariablesManagement(List<SurveyUnitModel> srcSuDtosOfIdUE, VariablesMap variablesMap, List<VariableModel> correctedExternalVariables) {
        //COLLECTED only
        Optional<SurveyUnitModel> collectedSuDtoOpt = srcSuDtosOfIdUE.stream().filter(
                suDto -> suDto.getState().equals(DataState.COLLECTED)
        ).findFirst();

        //Verify variables
        if(collectedSuDtoOpt.isPresent()){
            for(VariableModel variable: collectedSuDtoOpt.get().getExternalVariables()){
                if(variablesMap.hasVariable(variable.idVar())) {
                    VariableModel correctedExternalVariable = verifyExternalVariable(
                            variable,
                            variablesMap.getVariable(variable.idVar())
                    );
                    if (correctedExternalVariable != null) {
                        correctedExternalVariables.add(correctedExternalVariable);
                    }
                }
            }
        }
    }

    /**
     * Verify one external variable
     * @param externalVariable external variable DTO to verify
     * @param variableDefinition variable definition of the variable
     * @return a corrected external variable if there is any parsing error, null otherwise
     */
    private static VariableModel verifyExternalVariable(VariableModel externalVariable, fr.insee.bpm.metadata.model.Variable variableDefinition) {
        List<String> newValues = new ArrayList<>();
        boolean isInvalid = false;

        for (String value : externalVariable.values()){
            if(isParseError(value, variableDefinition.getType())){
                isInvalid = true;
                newValues.add("");
            }else{
                newValues.add(value);
            }
        }

        return isInvalid ? VariableModel.builder()
                .idVar(externalVariable.idVar())
                .values(newValues)
                .build() : null;
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
