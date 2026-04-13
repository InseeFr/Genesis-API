package fr.insee.genesis.domain.utils;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
public final class RawResponseConverter {

    private RawResponseConverter() {}

    /**
     * Converts a list of RawResponseModel into SurveyUnitModels.
     * Empty models (no variables) have their process dates updated immediately.
     */
    public static List<SurveyUnitModel> convertRawData(
            List<RawResponseModel> rawResponseModels,
            VariablesMap variablesMap
    ) {
        List<SurveyUnitModel> result = new ArrayList<>();
        List<SurveyUnitModel> emptyModels = new ArrayList<>();

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (RawResponseModel raw : rawResponseModels) {
                SurveyUnitModel model = buildModel(raw, dataState, variablesMap);

                boolean hasNoVariable = model.getCollectedVariables().isEmpty()
                        && model.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (dataState == DataState.COLLECTED) {
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.",
                                raw.interrogationId());
                    }
                    emptyModels.add(model);
                } else {
                    result.add(model);
                }
            }
        }

        // Verify and add FORCED models if needed
        DataVerifier.verifySurveyUnits(result, variablesMap);

        return result;
    }

    private static SurveyUnitModel buildModel(
            RawResponseModel raw,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        RawResponseDto.QuestionnaireStateEnum questionnaireState =
                parseQuestionnaireState(RawResponsePayloadParser.getStringField(raw, "questionnaireState"));

        SurveyUnitModel model = SurveyUnitModel.builder()
                .collectionInstrumentId(raw.collectionInstrumentId())
                .majorModelVersion(RawResponsePayloadParser.getStringField(raw, "majorModelVersion"))
                .mode(raw.mode())
                .interrogationId(raw.interrogationId())
                .usualSurveyUnitId(RawResponsePayloadParser.getStringField(raw, "usualSurveyUnitId"))
                .questionnaireState(questionnaireState)
                .validationDate(RawResponsePayloadParser.getValidationDate(raw))
                .isCapturedIndirectly(RawResponsePayloadParser.getIsCapturedIndirectly(raw))
                .state(dataState)
                .fileDate(raw.recordDate())
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();

        convertCollectedVariables(raw, model, dataState, variablesMap);

        if (dataState == DataState.COLLECTED) {
            convertExternalVariables(raw, model, variablesMap);
        }

        return model;
    }

    private static RawResponseDto.QuestionnaireStateEnum parseQuestionnaireState(String value) {
        if (value == null) return null;
        try {
            return RawResponseDto.QuestionnaireStateEnum.valueOf(value);
        } catch (IllegalArgumentException e) {
            log.warn("'{}' is not a valid questionnaire state according to filiere model", value);
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertCollectedVariables(
            RawResponseModel raw,
            SurveyUnitModel dst,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = (Map<String, Object>) raw.payload().get("data");
        Map<String, Object> collectedMap = JsonUtils.asMap(
                (Map<String, Object>) dataMap.get("COLLECTED")
        );

        if (collectedMap == null || collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", raw.interrogationId());
            }
            return;
        }

        String stateKey = dataState.toString();
        for (Map.Entry<String, Object> entry : collectedMap.entrySet()) {
            processCollectedVariable(entry, stateKey, variablesMap, dst, dst.getCollectedVariables());
        }
    }

    public static void processCollectedVariable(
            Map.Entry<String, Object> entry,
            String stateKey,
            VariablesMap variablesMap,
            SurveyUnitModel dst,
            List<VariableModel> target
    ) {
        if (Constants.PAIRWISES.equals(entry.getKey())) {
            handlePairwise(entry, DataState.valueOf(stateKey), variablesMap, dst);
            return;
        }

        Map<String, Object> states = JsonUtils.asMap(entry.getValue());
        if (states == null) return;

        Object value = states.get(stateKey);
        if (value == null) return;

        if (value instanceof List<?> list) {
            convertListVar(list, entry, variablesMap, target);
        } else {
            convertOneVar(entry, getValueString(value), variablesMap, 1, target);
        }
    }

    @SuppressWarnings("unchecked")
    private static void convertExternalVariables(
            RawResponseModel raw,
            SurveyUnitModel dst,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = (Map<String, Object>) raw.payload().get("data");
        Map<String, Object> externalMap = JsonUtils.asMap(
                (Map<String, Object>) dataMap.get("EXTERNAL")
        );

        if (externalMap == null || externalMap.isEmpty()) return;

        for (Map.Entry<String, Object> entry : externalMap.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof List<?>) {
                convertListVar(value, entry, variablesMap, dst.getExternalVariables());
            } else if (value != null) {
                convertOneVar(entry, value.toString(), variablesMap, 1, dst.getExternalVariables());
            }
        }
    }

    @SuppressWarnings("unchecked")
    static void handlePairwise(
            Map.Entry<String, Object> entry,
            DataState dataState,
            VariablesMap variablesMap,
            SurveyUnitModel dst
    ) {
        Map<String, Object> states = JsonUtils.asMap(entry.getValue());
        Object value = states != null ? states.get(dataState.toString()) : null;

        if (!(value instanceof List<?>) || !variablesMap.hasVariable(Constants.PAIRWISE_PREFIX + 1)) {
            return;
        }

        List<?> individuals = (List<?>) value;
        String groupName = variablesMap.getVariable(Constants.PAIRWISE_PREFIX + 1).getGroupName();

        for (int i = 0; i < individuals.size(); i++) {
            List<String> links = (List<String>) individuals.get(i);
            for (int linkIdx = 1; linkIdx < Constants.MAX_LINKS_ALLOWED; linkIdx++) {
                dst.getCollectedVariables().add(buildPairwiseVariable(links, linkIdx, i + 1, groupName));
            }
        }
    }

    private static VariableModel buildPairwiseVariable(
            List<String> links, int linkIndex, int iteration, String groupName
    ) {
        String value = Constants.NO_PAIRWISE_VALUE;
        if (linkIndex <= links.size()) {
            String v = links.get(linkIndex - 1);
            value = (v == null || v.isBlank()) ? Constants.SAME_AXIS_VALUE : v;
        }
        return VariableModel.builder()
                .varId(Constants.PAIRWISE_PREFIX + linkIndex)
                .value(value)
                .scope(groupName)
                .iteration(iteration)
                .parentId(Constants.ROOT_GROUP_NAME)
                .build();
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
            log.warn("Variable {} not present in metadata, assigning to {}", variableName, Constants.ROOT_GROUP_NAME);
            return Constants.ROOT_GROUP_NAME;
        }
        return variablesMap.getVariable(variableName).getGroupName();
    }

    static String getValueString(Object value) {
        return value == null ? null : value.toString();
    }
}
