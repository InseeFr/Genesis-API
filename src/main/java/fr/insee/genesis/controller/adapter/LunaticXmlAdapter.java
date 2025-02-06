package fr.insee.genesis.controller.adapter;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCollectedData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlOtherData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.utils.LoopIdentifier;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class LunaticXmlAdapter {

    /**
     * Convert a Lunatic XML survey unit into a genesis survey unit model
     * @param su Lunatic XML survey unit to convert
     * @param variablesMap variable definitions (used for loops)
     * @param idCampaign survey id
     * @return Genesis SurveyUnitModels for each data state
     */
    public static List<SurveyUnitModel> convert(LunaticXmlSurveyUnit su, VariablesMap variablesMap, String idCampaign, Mode mode){
        //Get COLLECTED Data and external variables
        List<SurveyUnitModel> surveyUnitModelList = new ArrayList<>();
        SurveyUnitModel surveyUnitModel = getStateDataFromSurveyUnit(su, variablesMap, idCampaign, DataState.COLLECTED, mode);
        getExternalDataFromSurveyUnit(su, surveyUnitModel, variablesMap);

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
     * @param dataState state of the SurveyUnitModel to generate
     * @return SurveyUnitModel with a specific state
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
     * Gets data from a specific state and put it into Model's data
     * @param su XML survey unit to extract from
     * @param surveyUnitModel Model to aliment
     * @param variablesMap variables definitions (used for loops)
     * @param dataState data state from XML
     * @return the SurveyUnitModel containing data, null if no data and not COLLECTED
     */
    private static SurveyUnitModel getCollectedDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitModel surveyUnitModel, VariablesMap variablesMap, DataState dataState) {
        List<VariableModel> variableModels = new ArrayList<>();

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
            if(valueTypeList == null) {
                continue; //Go to next data
            }
            for (int i = 1; i <= valueTypeList.size(); i++) {
                if (valueTypeList.get(i-1).getValue()!=null) {
                    variableModels.add(VariableModel.builder()
                            .varId(lunaticXmlCollectedData.getVariableName())
                            .value(valueTypeList.get(i-1).getValue())
                            .scope(LoopIdentifier.getLoopIdentifier(lunaticXmlCollectedData.getVariableName(), variablesMap))
                            .parentId(LoopIdentifier.getRelatedVariableName(lunaticXmlCollectedData.getVariableName(), variablesMap))
                            .iteration(i)
                            .build());
                    dataCount++;
                }
            }
        }
        surveyUnitModel.setCollectedVariables(variableModels);

        //Return null if no data and not COLLECTED
        if(dataCount > 0 || dataState.equals(DataState.COLLECTED)){
            return surveyUnitModel;
        }
        return null;
    }



    /**
     * Extract external data from XML survey unit and put it into Model
     * @param su XML survey unit
     * @param surveyUnitModel Model to aliment
     */
    private static void getExternalDataFromSurveyUnit(LunaticXmlSurveyUnit su, SurveyUnitModel surveyUnitModel, VariablesMap variablesMap) {
        List<VariableModel> variableModels = new ArrayList<>();

        for(LunaticXmlOtherData lunaticXmlExternalData : su.getData().getExternal()){
            List<ValueType> valueTypeList = lunaticXmlExternalData.getValues();
            if(valueTypeList == null) {
                continue; //Go to next data
            }
            for (int i = 1; i <= valueTypeList.size(); i++) {
                if (valueTypeList.get(i-1).getValue()!=null) {
                    variableModels.add(VariableModel.builder()
                            .varId(lunaticXmlExternalData.getVariableName())
                            .value(valueTypeList.get(i-1).getValue())
                            .scope(LoopIdentifier.getLoopIdentifier(lunaticXmlExternalData.getVariableName(), variablesMap))
                            .iteration(i)
                            .parentId(LoopIdentifier.getRelatedVariableName(lunaticXmlExternalData.getVariableName(), variablesMap))
                            .build());
                }
            }
        }
        surveyUnitModel.setExternalVariables(variableModels);
    }
}
