package fr.insee.genesis.domain.utils;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static fr.insee.genesis.domain.utils.LunaticJsonRawDataParser.parseIsCapturedIndirectly;
import static fr.insee.genesis.domain.utils.LunaticJsonRawDataParser.parseValidationDate;

@Slf4j
public final class LunaticJsonRawDataConverter {

    private LunaticJsonRawDataConverter() {}

    /**
     * Converts a list of LunaticJsonRawDataModel into SurveyUnitModels.
     * Empty models (no variables) have their process dates updated immediately.
     */
    public static List<SurveyUnitModel> convertRawData(
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap
    ) {
        List<SurveyUnitModel> result = new ArrayList<>();
        List<SurveyUnitModel> emptyModels = new ArrayList<>();

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                SurveyUnitModel model = buildModel(rawData, dataState, variablesMap);

                boolean hasNoVariable = model.getCollectedVariables().isEmpty()
                        && model.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (dataState == DataState.COLLECTED) {
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.",
                                rawData.interrogationId());
                    }
                    emptyModels.add(model);
                } else {
                    result.add(model);
                }
            }
        }

        DataVerifier.verifySurveyUnits(result, variablesMap);

        return result;
    }

    private static SurveyUnitModel buildModel(
            LunaticJsonRawDataModel rawData,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        RawDataModelType modelType = resolveModelType(rawData);

        SurveyUnitModel model = SurveyUnitModel.builder()
                .campaignId(rawData.campaignId())
                .collectionInstrumentId(rawData.questionnaireId())
                .mode(rawData.mode())
                .interrogationId(rawData.interrogationId())
                .usualSurveyUnitId(rawData.idUE())
                .validationDate(parseValidationDate(rawData))
                .isCapturedIndirectly(parseIsCapturedIndirectly(rawData))
                .state(dataState)
                .fileDate(rawData.recordDate())
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        convertCollectedVariables(rawData, model, dataState, modelType, variablesMap);

        if (dataState == DataState.COLLECTED) {
            convertExternalVariables(rawData, model, modelType, variablesMap);
        }

        return model;
    }

    private static RawDataModelType resolveModelType(LunaticJsonRawDataModel rawData) {
        return rawData.data().containsKey("data")
                ? RawDataModelType.FILIERE
                : RawDataModelType.LEGACY;
    }

    @SuppressWarnings("unchecked")
    private static void convertCollectedVariables(
            LunaticJsonRawDataModel rawData,
            SurveyUnitModel dst,
            DataState dataState,
            RawDataModelType modelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawData.data();
        if (modelType == RawDataModelType.FILIERE) {
            dataMap = (Map<String, Object>) dataMap.get("data");
        }

        Map<String, Object> collectedMap = JsonUtils.asMap(
                (Map<String, Object>) dataMap.get("COLLECTED")
        );

        if (collectedMap == null || collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", rawData.interrogationId());
            }
            return;
        }

        String stateKey = dataState.toString();
        for (Map.Entry<String, Object> entry : collectedMap.entrySet()) {
            RawResponseConverter.processCollectedVariable(
                    entry, stateKey, variablesMap, dst, dst.getCollectedVariables()
            );
        }
    }


    @SuppressWarnings("unchecked")
    private static void convertExternalVariables(
            LunaticJsonRawDataModel rawData,
            SurveyUnitModel dst,
            RawDataModelType modelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawData.data();
        if (modelType == RawDataModelType.FILIERE) {
            dataMap = (Map<String, Object>) dataMap.get("data");
        }

        Map<String, Object> externalMap = JsonUtils.asMap(
                (Map<String, Object>) dataMap.get("EXTERNAL")
        );

        if (externalMap == null || externalMap.isEmpty()) return;

        for (Map.Entry<String, Object> entry : externalMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List<?>) {
                convertListVar(value, entry, variablesMap, dst.getExternalVariables());
            } else if (value != null) {
                convertOneVar(entry, getValueString(value), variablesMap, 1,
                        dst.getExternalVariables());
            }
        }
    }

    public static String getValueString(Object value) {
        if (value instanceof Double || value instanceof Float) {
            BigDecimal bd = new BigDecimal(value.toString());
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return String.valueOf(value);
    }


    private static void convertListVar(
            Object values,
            Map.Entry<String, Object> entry,
            VariablesMap variablesMap,
            List<VariableModel> target
    ) {
        List<String> list = JsonUtils.asStringList(values);
        int iteration = 1;
        for (String value : list) {
            if (value != null && !value.isEmpty()) {
                convertOneVar(entry, value, variablesMap, iteration, target);
            }
            iteration++;
        }
    }

    private static void convertOneVar(
            Map.Entry<String, Object> entry,
            String value,
            VariablesMap variablesMap,
            int iteration,
            List<VariableModel> target
    ) {
        target.add(VariableModel.builder()
                .varId(entry.getKey())
                .value(value)
                .scope(resolveScope(variablesMap, entry.getKey()))
                .iteration(iteration)
                .parentId(GroupUtils.getParentGroupName(entry.getKey(), variablesMap))
                .build());
    }

    private static String resolveScope(VariablesMap variablesMap, String variableName) {
        if (variablesMap.getVariable(variableName) == null) {
            log.warn("Variable {} not present in metadata, assigning to {}",
                    variableName, Constants.ROOT_GROUP_NAME);
            return Constants.ROOT_GROUP_NAME;
        }
        return variablesMap.getVariable(variableName).getGroupName();
    }

}