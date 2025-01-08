package fr.insee.genesis.controller.adapter;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCollectedData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.utils.LoopIdentifier;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@UtilityClass
public class LunaticXmlAdapter {

    /**
     * Convert a Lunatic XML survey unit into a genesis survey unit DTO
     * @param su Lunatic XML survey unit to convert
     * @param variablesMap variable definitions (used for loops)
     * @param idCampaign survey id
     * @return a genesis survey unit DTO
     */
    public static List<SurveyUnitModel> convert(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign, Mode mode){
        //Get COLLECTED Data and external variables
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        SurveyUnitModel surveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.COLLECTED, mode);
        getExternalDataFromSurveyUnit(su, surveyUnitModel);

        surveyUnitModelList.add(surveyUnitModel);

        //Get data from other states
        SurveyUnitModel editedSurveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.EDITED,mode);
        if(editedSurveyUnitModel != null){
            surveyUnitModelList.add(editedSurveyUnitModel);
        }

        SurveyUnitModel inputedSurveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.INPUTED,mode);
        if(inputedSurveyUnitModel != null){
            surveyUnitModelList.add(inputedSurveyUnitModel);
        }

        SurveyUnitModel forcedSurveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.FORCED,mode);
        if(forcedSurveyUnitModel != null){
            surveyUnitModelList.add(forcedSurveyUnitModel);
        }

        SurveyUnitModel previousSurveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.PREVIOUS,mode);
        if(previousSurveyUnitModel != null){
            surveyUnitModelList.add(previousSurveyUnitModel);
        }


        return surveyUnitModelList;
    }

    /**
     * Collects data from XML survey unit depending on the data state
     * @param su source XML Survey Unit
     * @param variablesMap variable definitions (used for loops)
     * @param idCampaign survey id
     * @param dataState state of the DTO to generate
     * @return Survey Unit DTO with a specific state
     */
    private static SurveyUnitModel getStateDataFromSurveyUnit(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign, DataState dataState, Mode mode) {
        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                .questionnaireId(su.getQuestionnaireModelId())
                .campaignId(idCampaign)
                .interrogationId(su.getId())
                .state(dataState)
                .mode(mode)
                .recordDate(LocalDateTime.now())
                .fileDate(su.getFileDate())
                .build();

        return getCollectedDataFromSurveyUnit(su, surveyUnitModel, variablesMap, dataState);
    }


    /**
     * Gets data from a specific state and put it into DTO's data
     * @param su XML survey unit to extract from
     * @param surveyUnitModel DTO to aliment
     * @param variablesMap variables definitions (used for loops)
     * @param dataState data state from XML
     * @return the DTO containing data, null if no data and not COLLECTED
     */
    private static SurveyUnitModel getCollectedDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitModel surveyUnitModel, VariablesMap variablesMap, DataState dataState) {
        List<CollectedVariable> variablesUpdate = new ArrayList<>();

        int dataCount = 0;
        for (LunaticXmlCollectedData lunaticXmlCollectedData : su.getData().getCollected()){
            List<ValueType> valueTypeList;
            switch (dataState){
                case COLLECTED:
                    valueTypeList = lunaticXmlCollectedData.getCollected();
                    break;
                case EDITED :
                    valueTypeList = lunaticXmlCollectedData.getEdited();
                    break;
                case FORCED :
                    valueTypeList = lunaticXmlCollectedData.getForced();
                    break;
                case INPUTED:
                    valueTypeList = lunaticXmlCollectedData.getInputed();
                    break;
                case PREVIOUS:
                    valueTypeList = lunaticXmlCollectedData.getPrevious();
                    break;
                default:
                    return null;
            }
            if(valueTypeList != null) {
                for (int i = 1; i <= valueTypeList.size(); i++) {
                    List<String> variableValues = new ArrayList<>();
                    if (valueTypeList.get(i-1).getValue()!=null) {
                        variableValues.add(valueTypeList.get(i-1).getValue());
                        variablesUpdate.add(CollectedVariable.collectedVariableBuilder()
                                .idVar(lunaticXmlCollectedData.getVariableName())
                                .values(variableValues)
                                .idLoop(LoopIdentifier.getLoopIdentifier(lunaticXmlCollectedData.getVariableName(), variablesMap, i))
                                .idParent(LoopIdentifier.getRelatedVariableName(lunaticXmlCollectedData.getVariableName(), variablesMap))
                                .build());
                        dataCount++;
                    }
                }
            }
        }
        surveyUnitModel.setCollectedVariables(variablesUpdate);

        //Return null if no data and not COLLECTED
        if(dataCount > 0 || dataState.equals(DataState.COLLECTED)){
            return surveyUnitModel;
        }
        return null;
    }



    /**
     * Extract external data from XML survey unit and put it into DTO
     * @param su XML survey unit
     * @param surveyUnitModel DTO to aliment
     */
    private static void getExternalDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitModel surveyUnitModel) {
        List<Variable> externalVariables = new ArrayList<>();
        su.getData().getExternal().forEach(lunaticXmlExternalData ->
                externalVariables.add(Variable.builder()
                        .idVar(lunaticXmlExternalData.getVariableName())
                        .values(getValuesFromValueTypeList(lunaticXmlExternalData.getValues()))
                        .build())
        );
        surveyUnitModel.setExternalVariables(externalVariables);
    }

    private static List<String> getValuesFromValueTypeList(List<ValueType> valueTypeList) {
        if (!valueTypeList.isEmpty()){
            return valueTypeList.stream()
                    .map(ValueType::getValue)
                    .filter(Objects::nonNull)
                    .toList();
        }
        return List.of();
    }

}
