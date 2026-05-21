package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.parser.rawdata.LunaticJsonRawDataPayloadParser;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class LunaticJsonRawDataConverter {

    private final LunaticJsonRawDataPayloadParser payloadParser;

    public List<SurveyUnitModel> convertRawData(
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap
    ) {
        return convertRawDataAndCollectEmptyModels(rawDataList, variablesMap, new ArrayList<>());
    }

    public List<SurveyUnitModel> convertRawDataAndCollectEmptyModels(
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap,
            List<SurveyUnitModel> emptySurveyUnitModels
    ) {
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                RawDataModelType rawDataModelType = getRawDataModelType(rawData);

                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .collectionInstrumentId(rawData.questionnaireId())
                        .mode(rawData.mode())
                        .interrogationId(rawData.interrogationId())
                        .usualSurveyUnitId(rawData.idUE())
                        .validationDate(payloadParser.getValidationDate(rawData))
                        .isCapturedIndirectly(payloadParser.getIsCapturedIndirectly(rawData))
                        .state(dataState)
                        .rawRecordDate(rawData.recordDate())
                        .recordDate(Instant.now())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                convertRawDataCollectedVariables(rawData, surveyUnitModel, dataState, rawDataModelType, variablesMap);

                if (dataState == DataState.COLLECTED) {
                    convertRawDataExternalVariables(rawData, surveyUnitModel, rawDataModelType, variablesMap);
                }

                boolean hasNoVariable = surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (surveyUnitModel.getState() == DataState.COLLECTED) {
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.",
                                rawData.interrogationId());
                    }
                    emptySurveyUnitModels.add(surveyUnitModel);
                    continue;
                }

                surveyUnitModels.add(surveyUnitModel);
            }
        }

        return surveyUnitModels;
    }

    private static RawDataModelType getRawDataModelType(LunaticJsonRawDataModel rawData) {
        return rawData.data().containsKey("data")
                ? RawDataModelType.FILIERE
                : RawDataModelType.LEGACY;
    }

    private void convertRawDataCollectedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = srcRawData.data();
        if (rawDataModelType == RawDataModelType.FILIERE) {
            dataMap = JsonUtils.asMap(dataMap.get("data"));
        }

        Map<String, Object>  collectedMap = JsonUtils.asMap(dataMap.get("COLLECTED"));

        if (collectedMap == null || collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", srcRawData.interrogationId());
            }
            return;
        }

        String stateKey = dataState.toString();
        List<VariableModel> destination = dstSurveyUnitModel.getCollectedVariables();

        for (Map.Entry<String, Object> collectedVariable : collectedMap.entrySet()) {
            RawResponseConverter.processCollectedVariable(
                    collectedVariable,
                    stateKey,
                    variablesMap,
                    dstSurveyUnitModel,
                    destination
            );
        }
    }


    private static void convertRawDataExternalVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = srcRawData.data();
        if (rawDataModelType == RawDataModelType.FILIERE) {
            dataMap =  JsonUtils.asMap(dataMap.get("data"));
        }

        Map<String, Object> externalMap =  JsonUtils.asMap(dataMap.get("EXTERNAL"));
        if (externalMap != null && !externalMap.isEmpty()) {
            convertToExternalVar(dstSurveyUnitModel, variablesMap, externalMap);
        }
    }

    private static void convertToExternalVar(
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap,
            Map<String, Object> externalMap
    ) {
        for (Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()) {
            Object valueObject = externalVariableEntry.getValue();

            if (valueObject instanceof List<?>) {
                convertListVar(valueObject, externalVariableEntry, variablesMap, dstSurveyUnitModel.getExternalVariables());
                continue;
            }

            if (valueObject != null) {
                convertOneVar(
                        externalVariableEntry,
                        valueObject.toString(),
                        variablesMap,
                        1,
                        dstSurveyUnitModel.getExternalVariables()
                );
            }
        }
    }

    private static void convertListVar(
            Object valuesForState,
            Map.Entry<String, Object> collectedVariable,
            VariablesMap variablesMap,
            List<VariableModel> destination
    ) {
        List<String> values = JsonUtils.asStringList(valuesForState);
        if (!values.isEmpty()) {
            int iteration = 1;
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    convertOneVar(collectedVariable, value, variablesMap, iteration, destination);
                }
                iteration++;
            }
        }
    }

    private static void convertOneVar(
            Map.Entry<String, Object> variableEntry,
            String value,
            VariablesMap variablesMap,
            int iteration,
            List<VariableModel> destination
    ) {
        VariableModel variableModel = VariableModel.builder()
                .varId(variableEntry.getKey())
                .value(value)
                .scope(getIdLoop(variablesMap, variableEntry.getKey()))
                .iteration(iteration)
                .parentId(GroupUtils.getParentGroupName(variableEntry.getKey(), variablesMap))
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
}
