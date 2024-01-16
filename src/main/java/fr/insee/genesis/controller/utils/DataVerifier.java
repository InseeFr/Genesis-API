package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
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
     * If there is at least 1 incorrect variable for a survey unit, a new SurveyUnitUpdateDto is created with "FORCED" status
     * The new surveyUnitUpdates are added to the list
     * @param suDtosList list of SurveyUnitUpdateDtos to verify
     * @param variablesMap VariablesMap containing definitions of each variable
     */
    public static void verifySurveyUnits(List<SurveyUnitUpdateDto> suDtosList, VariablesMap variablesMap){
        List<SurveyUnitUpdateDto> suDtosListForced = new ArrayList<>(); // Created FORCED SU DTOs

        for(String idUE : getIdUEs(suDtosList)) { // For each id of the list
            List<SurveyUnitUpdateDto> srcSuDtosOfIdUE = suDtosList.stream().filter(element -> element.getIdUE().equals(idUE)).toList();
            List<CollectedVariableDto> correctedCollectedVariables = new ArrayList<>();
            List<VariableDto> correctedExternalVariables = new ArrayList<>();

            //Get corrected variables
            collectedVariablesManagement(srcSuDtosOfIdUE, variablesMap, correctedCollectedVariables);
            externalVariablesManagement(srcSuDtosOfIdUE, variablesMap, correctedExternalVariables);

            //Create FORCED if any corrected variable
            if(!correctedCollectedVariables.isEmpty() || !correctedExternalVariables.isEmpty()){
                SurveyUnitUpdateDto newForcedSuDto = createForcedDto(suDtosList, idUE, correctedCollectedVariables, correctedExternalVariables);
                suDtosListForced.add(newForcedSuDto);
            }
        }
        suDtosList.addAll(suDtosListForced);
    }

    private static SurveyUnitUpdateDto createForcedDto(
            List<SurveyUnitUpdateDto> suDtosList,
            String idUE,
            List<CollectedVariableDto> correctedCollectedVariables,
            List<VariableDto> correctedExternalVariables
    ) {
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

        for(CollectedVariableDto correctedCollectedVariable : correctedCollectedVariables){
            newForcedSuDto.getCollectedVariables().add(
                    new CollectedVariableDto(correctedCollectedVariable.getIdVar(),
                            correctedCollectedVariable.getValues()
                            ,correctedCollectedVariable.getIdLoop()
                            ,correctedCollectedVariable.getIdParent()
                    )
            );
        }

        for(VariableDto correctedExternalVariable : correctedExternalVariables){
            newForcedSuDto.getExternalVariables().add(
                    VariableDto.builder()
                            .idVar(correctedExternalVariable.getIdVar())
                            .values(correctedExternalVariable.getValues())
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
    private static void collectedVariablesManagement(List<SurveyUnitUpdateDto> srcSuDtosOfIdUE, VariablesMap variablesMap, List<CollectedVariableDto> correctedCollectedVariables){
        Set<String> variableNames = new HashSet<>();
        List<CollectedVariableDto> variablesToVerify = new ArrayList<>();

        //Sort from more priority to less
        List<SurveyUnitUpdateDto> sortedSuDtos = srcSuDtosOfIdUE.stream().sorted(Comparator.comparing(surveyUnitUpdateDto -> dataStatesPriority.get(surveyUnitUpdateDto.getState()))).toList();

        //Get more priority variables to verify
        for(SurveyUnitUpdateDto srcSuDto : sortedSuDtos){
            for(CollectedVariableDto collectedVariableDto : srcSuDto.getCollectedVariables()){
                if(!variableNames.contains(collectedVariableDto.getIdVar())){
                    variableNames.add(collectedVariableDto.getIdVar());
                    variablesToVerify.add(collectedVariableDto);
                }
            }
        }

        //Verify variables
        for(CollectedVariableDto collectedVariableToVerify : variablesToVerify){
            CollectedVariableDto correctedCollectedVariable = verifyCollectedVariable(
                    collectedVariableToVerify,
                    variablesMap.getVariable(collectedVariableToVerify.getIdVar())
            );

            if(correctedCollectedVariable != null){
                correctedCollectedVariables.add(correctedCollectedVariable);
            }
        }
    }

    private static CollectedVariableDto verifyCollectedVariable(CollectedVariableDto collectedVariable, Variable variableDefinition) {
        List<String> newValues = new ArrayList<>();
        boolean isInvalid = false;

        for (String value : collectedVariable.getValues()){
            if(isParseError(value, variableDefinition.getType())){
                isInvalid = true;
                newValues.add("");
            }else{
                newValues.add(value);
            }
        }

        return isInvalid ? new CollectedVariableDto(
                collectedVariable.getIdVar(),
                newValues,
                collectedVariable.getIdLoop(),
                collectedVariable.getIdParent()
                ) : null;
    }

    private static void externalVariablesManagement(List<SurveyUnitUpdateDto> srcSuDtosOfIdUE, VariablesMap variablesMap, List<VariableDto> correctedExternalVariables) {
        //COLLECTED only
        Optional<SurveyUnitUpdateDto> collectedSuDtoOpt = srcSuDtosOfIdUE.stream().filter(
                suDto -> suDto.getState().equals(DataState.COLLECTED)
        ).findFirst();

        //Verify variables
        if(collectedSuDtoOpt.isPresent()){
            for(VariableDto variable: collectedSuDtoOpt.get().getExternalVariables()){
                VariableDto correctedExternalVariable = verifyExternalVariable(
                        variable,
                        variablesMap.getVariable(variable.getIdVar())
                );
                if (correctedExternalVariable != null) {
                    correctedExternalVariables.add(correctedExternalVariable);
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
    private static VariableDto verifyExternalVariable(VariableDto externalVariable, Variable variableDefinition) {
        List<String> newValues = new ArrayList<>();
        boolean isInvalid = false;

        for (String value : externalVariable.getValues()){
            if(isParseError(value, variableDefinition.getType())){
                isInvalid = true;
                newValues.add("");
            }else{
                newValues.add(value);
            }
        }

        return isInvalid ? VariableDto.builder()
                .idVar(externalVariable.getIdVar())
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
