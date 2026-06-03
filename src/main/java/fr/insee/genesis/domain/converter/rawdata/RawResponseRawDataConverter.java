package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.parser.rawdata.RawResponsePayloadParser;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService.getValueString;

@Component
@Slf4j
public class RawResponseRawDataConverter extends RawDataConverter {

    private final RawResponsePayloadParser rawResponsePayloadParser;

    public RawResponseRawDataConverter(SurveyUnitService surveyUnitService, RawResponsePayloadParser rawResponsePayloadParser) {
        super(surveyUnitService);
        this.rawResponsePayloadParser = rawResponsePayloadParser;
    }

    /**
     * Like convertRawResponseAndCollectEmptyModels but ignoring the empty raw responses
     */
    public List<SurveyUnitModel> convertRawResponse(
            String collectionInstrumentId,
            List<RawResponseModel> rawResponseModels,
            VariablesMap variablesMap
    ) {
        return convertRawResponseAndCollectEmptyModels(
                collectionInstrumentId,
                rawResponseModels,
                variablesMap,
                new ArrayList<>()
        );
    }

    /**
     * Converts RawResponseModels into SurveyUnitModels
     * @param collectionInstrumentId Collection instrument id of raw responses to convert
     * @param rawResponseModels raw responses to convert
     * @param variablesMap variables map of the collection instrument
     * @param emptySurveyUnitModels A list of survey units that will be filled with empty raw responses
     * @return a list of SurveyUnitModels converted from raw responses
     */
    public List<SurveyUnitModel> convertRawResponseAndCollectEmptyModels(
            String collectionInstrumentId,
            List<RawResponseModel> rawResponseModels,
            VariablesMap variablesMap,
            List<SurveyUnitModel> emptySurveyUnitModels
    ) {
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        Map<String, SurveyUnitModel> lastSurveyUnitModelsByInterrogationId = getLastSurveyUnitModels(
                collectionInstrumentId,
                rawResponseModels.stream().map(RawResponseModel::interrogationId).collect(Collectors.toList())
        );

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (RawResponseModel rawResponseModel : rawResponseModels) {
                SurveyUnitModel surveyUnitModel = buildSurveyUnitModel(rawResponseModel, dataState);

                convertCollectedVariables(
                        rawResponseModel,
                        lastSurveyUnitModelsByInterrogationId.get(rawResponseModel.interrogationId()),
                        surveyUnitModel,
                        dataState,
                        variablesMap
                );

                if (dataState == DataState.COLLECTED) {
                    convertExternalVariables(rawResponseModel, surveyUnitModel, variablesMap);
                }

                boolean hasNoVariable = surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (surveyUnitModel.getState() == DataState.COLLECTED) {
                        log.warn(
                                "No collected or external variable for interrogation {}, raw data is ignored.",
                                rawResponseModel.interrogationId()
                        );
                    }
                    emptySurveyUnitModels.add(surveyUnitModel);
                    continue;
                }

                surveyUnitModels.add(surveyUnitModel);
            }
        }

        return surveyUnitModels;
    }

    private SurveyUnitModel buildSurveyUnitModel(RawResponseModel rawResponseModel, DataState dataState) {
        String questionnaireStateString =
                rawResponsePayloadParser.getStringField(rawResponseModel, "questionnaireState");

        RawResponseDto.QuestionnaireStateEnum questionnaireStateEnum = null;
        try {
            questionnaireStateEnum = RawResponseDto.QuestionnaireStateEnum.valueOf(questionnaireStateString);
        } catch (IllegalArgumentException _) {
            log.warn("'{}' is not a valid questionnaire state according to filiere model", questionnaireStateString);
        } catch (NullPointerException _) {
            //Nothing to do
        }

        return SurveyUnitModel.builder()
                .collectionInstrumentId(rawResponseModel.collectionInstrumentId())
                .majorModelVersion(rawResponsePayloadParser.getStringField(rawResponseModel, "majorModelVersion"))
                .mode(rawResponseModel.mode())
                .interrogationId(rawResponseModel.interrogationId())
                .usualSurveyUnitId(rawResponsePayloadParser.getStringField(rawResponseModel, "usualSurveyUnitId"))
                .questionnaireState(questionnaireStateEnum)
                .validationDate(rawResponsePayloadParser.getValidationDate(rawResponseModel))
                .isCapturedIndirectly(rawResponsePayloadParser.getIsCapturedIndirectly(rawResponseModel))
                .state(dataState)
                .fileDate(rawResponseModel.recordDate())
                .recordDate(Instant.now())
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();
    }

    private void convertCollectedVariables(
            RawResponseModel rawResponseModel,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawResponseModel.payload();
        dataMap = JsonUtils.asMap(dataMap.get("data"));
        Map<String, Object> collectedMap  = JsonUtils.asMap(dataMap.get("COLLECTED"));

        if (collectedMap == null || collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", rawResponseModel.interrogationId());
            }
            return;
        }

        String stateKey = dataState.toString();
        List<VariableModel> collectedVariables = dstSurveyUnitModel.getCollectedVariables();

        for (Map.Entry<String, Object> entry : collectedMap.entrySet()) {
            processCollectedVariable(
                    entry,
                    stateKey,
                    variablesMap,
                    lastSurveyUnitModel,
                    dstSurveyUnitModel,
                    collectedVariables
            );
        }
    }

    public static void processCollectedVariable(
            Map.Entry<String, Object> entry,
            String stateKey,
            VariablesMap variablesMap,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            List<VariableModel> variableModelList
    ) {
        if (Constants.PAIRWISES.equals(entry.getKey())) {
            handlePairwiseCollectedVariable(entry, DataState.valueOf(stateKey), variablesMap, dstSurveyUnitModel);
            return;
        }

        Map<String, Object> states = JsonUtils.asMap(entry.getValue());
        if (states == null || states.get(stateKey) == null) {
            convertNullVar(entry, lastSurveyUnitModel, variableModelList);
            return;
        }

        Object value = states.get(stateKey);
        if (value instanceof List<?> list) {
            convertListVar(list, entry, variablesMap, variableModelList);
            return;
        }
        convertOneVar(entry, getValueString(value), variablesMap, 1, variableModelList);
    }

    private static void convertNullVar(
            Map.Entry<String, Object> variableEntry,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            List<VariableModel> destination
    ) {
        //TODO
    }

    private static void convertListVar(
            Object valuesForState,
            Map.Entry<String, Object> variableEntry,
            VariablesMap variablesMap,
            List<VariableModel> destination
    ) {
        List<String> values = JsonUtils.asStringList(valuesForState);

        if (!values.isEmpty()) {
            int iteration = 1;
            for (String value : values) {
                if (value != null && !value.isEmpty()) {
                    convertOneVar(variableEntry, value, variablesMap, iteration, destination);
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

    private void convertExternalVariables(
            RawResponseModel rawResponseModel,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawResponseModel.payload();
        dataMap = JsonUtils.asMap(dataMap.get("data"));
        Map<String, Object> externalMap = JsonUtils.asMap(dataMap.get("EXTERNAL"));

        if (externalMap != null && !externalMap.isEmpty()) {
            for (Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()) {
                Object valueObject = externalVariableEntry.getValue();

                if (valueObject instanceof List<?>) {
                    convertListVar(
                            valueObject,
                            externalVariableEntry,
                            variablesMap,
                            dstSurveyUnitModel.getExternalVariables()
                    );
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
    }

    @SuppressWarnings("unchecked")
    static void handlePairwiseCollectedVariable(
            Map.Entry<String, Object> collectedVariable,
            DataState dataState,
            VariablesMap variablesMap,
            SurveyUnitModel dstSurveyUnitModel
    ) {
        Object value = getValueForState(collectedVariable, dataState.toString());

        if (isInvalidPairwiseVariable(value, variablesMap)) {
            return;
        }

        List<?> individuals = (List<?>) value;
        String groupName = variablesMap.getVariable(Constants.PAIRWISE_PREFIX + 1).getGroupName();

        for (int individualIndex = 0; individualIndex < individuals.size(); individualIndex++) {
            List<String> individualLinks = (List<String>) individuals.get(individualIndex);

            for (int linkIndex = 1; linkIndex < Constants.MAX_LINKS_ALLOWED; linkIndex++) {
                dstSurveyUnitModel.getCollectedVariables().add(
                        buildPairwiseVariable(individualLinks, linkIndex, individualIndex + 1, groupName)
                );
            }
        }
    }

    private static VariableModel buildPairwiseVariable(
            List<String> individualLinks,
            int linkIndex,
            int iteration,
            String groupName
    ) {
        String value = Constants.NO_PAIRWISE_VALUE;

        if (linkIndex <= individualLinks.size()) {
            String v = individualLinks.get(linkIndex - 1);
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

    private static Object getValueForState(
            Map.Entry<String, Object> collectedVariable,
            String stateKey
    ) {
        Map<String, Object> states = JsonUtils.asMap(collectedVariable.getValue());
        return states != null ? states.get(stateKey) : null;
    }

    private static boolean isInvalidPairwiseVariable(Object value, VariablesMap variablesMap) {
        return !(value instanceof List<?>) || !variablesMap.hasVariable(Constants.PAIRWISE_PREFIX + 1);
    }
}
