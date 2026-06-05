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
import java.util.HashMap;
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

        Map<String, Map<DataState, SurveyUnitModel>> lastSurveyUnitModelsByInterrogationIdAndState = getLastSurveyUnitModels(
                collectionInstrumentId,
                rawResponseModels.stream().map(RawResponseModel::interrogationId).collect(Collectors.toList())
        );

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (RawResponseModel rawResponseModel : rawResponseModels) {
                SurveyUnitModel surveyUnitModel = buildSurveyUnitModel(rawResponseModel, dataState);
                SurveyUnitModel lastSurveyUnitModelForDataState = null;
                if(lastSurveyUnitModelsByInterrogationIdAndState.containsKey(rawResponseModel.interrogationId())){
                    lastSurveyUnitModelForDataState = lastSurveyUnitModelsByInterrogationIdAndState
                            .get(rawResponseModel.interrogationId())
                            .get(dataState);
                }

                convertCollectedVariables(
                        rawResponseModel,
                        lastSurveyUnitModelForDataState,
                        surveyUnitModel,
                        dataState,
                        variablesMap
                );

                if (dataState == DataState.COLLECTED) {
                    convertExternalVariables(
                            rawResponseModel,
                            lastSurveyUnitModelForDataState,
                            surveyUnitModel,
                            variablesMap
                    );
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
        Map<String, Object> collectedMap  = new HashMap<>(JsonUtils.asMap(dataMap.get("COLLECTED")));

        if(lastSurveyUnitModel != null && lastSurveyUnitModel.getState().equals(dataState)) {
            fillRawDataMapWithAbsentVariables(lastSurveyUnitModel, collectedMap, dataState);
        }

        if (collectedMap.isEmpty()) {
            if (dataState == DataState.COLLECTED) {
                log.warn("No collected data for interrogation {}", rawResponseModel.interrogationId());
            }
            return;
        }

        String stateKey = dataState.toString();
        List<VariableModel> collectedVariables = dstSurveyUnitModel.getCollectedVariables();

        for (Map.Entry<String, Object> entry : collectedMap.entrySet()) {
            processCollectedVariableForState(
                    entry,
                    stateKey,
                    variablesMap,
                    lastSurveyUnitModel,
                    dstSurveyUnitModel,
                    collectedVariables
            );
        }
    }

    public static void processCollectedVariableForState(
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

        Map<String, Object> variableStates = JsonUtils.asMap(entry.getValue());
        //If variable value absent for state or null value
        if (variableStates == null || variableStates.get(stateKey) == null) {
            convertNullVar(entry.getKey(), lastSurveyUnitModel, 1, variablesMap, variableModelList, true);
            return;
        }

        Object value = variableStates.get(stateKey);
        if (value instanceof List<?> list) {
            convertListVar(list, lastSurveyUnitModel, entry, variablesMap, variableModelList, true);
            return;
        }
        convertOneVar(entry.getKey(), getValueString(value), variablesMap, 1, variableModelList);
    }

    private static void convertNullVar(
            String variableName,
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            int iteration,
            VariablesMap variablesMap,
            List<VariableModel> destination,
            boolean isCollected //true if in collected variables, false if in external
    ) {
        //Do nothing if last response is null or variable is not present in last response
        if (lastSurveyUnitModel == null || isVariableAbsentInSurveyUnitModel(
                variableName, iteration, lastSurveyUnitModel, isCollected
        )) {
            return;
        }
        convertOneVar(variableName, null, variablesMap, iteration, destination);
    }

    private static boolean isVariableAbsentInSurveyUnitModel(
            String variableName,
            int iteration,
            SurveyUnitModel lastSurveyUnitModel,
            boolean isCollected
    ) {
        //Check in collected variables
        if(isCollected){
            return lastSurveyUnitModel.getCollectedVariables().stream().noneMatch(
                    variableModel -> variableModel.varId().equals(variableName)
                    && variableModel.iteration().equals(iteration)
            );
        }
        //Check in external variables
        return lastSurveyUnitModel.getExternalVariables().stream().noneMatch(
                variableModel -> variableModel.varId().equals(variableName)
                        && variableModel.iteration().equals(iteration)
        );
    }

    private static void convertListVar(
            Object valuesForState,
            @Nullable SurveyUnitModel lastSurveyUnit,
            Map.Entry<String, Object> variableEntry,
            VariablesMap variablesMap,
            List<VariableModel> destination,
            boolean isCollected
    ) {
        List<String> values = JsonUtils.asStringList(valuesForState);

        if (!values.isEmpty()) {
            int iteration = 1;
            for (String value : values) {
                if(value == null || value.isEmpty()){
                    convertNullVar(
                            variableEntry.getKey(),
                            lastSurveyUnit,
                            iteration,
                            variablesMap,
                            destination,
                            isCollected);
                    iteration++;
                    continue;
                }
                convertOneVar(variableEntry.getKey(), value, variablesMap, iteration, destination);
                iteration++;
            }
        }
    }

    private static void convertOneVar(
            String variableName,
            String value,
            VariablesMap variablesMap,
            int iteration,
            List<VariableModel> destination
    ) {
        VariableModel variableModel = VariableModel.builder()
                .varId(variableName)
                .value(value)
                .scope(getIdLoop(variablesMap, variableName))
                .iteration(iteration)
                .parentId(GroupUtils.getParentGroupName(variableName, variablesMap))
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
            @Nullable SurveyUnitModel lastSurveyUnitModel,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawResponseModel.payload();
        dataMap = JsonUtils.asMap(dataMap.get("data"));
        Map<String, Object> externalMap = null;
        if(dataMap.containsKey("EXTERNAL")) {
            externalMap = new HashMap<>(JsonUtils.asMap(dataMap.get("EXTERNAL")));
        }

        if(lastSurveyUnitModel != null){
            fillRawDataMapWithAbsentVariables(lastSurveyUnitModel, externalMap, null);
        }

        if (externalMap != null) {
            for (Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()) {
                Object valueObject = externalVariableEntry.getValue();

                if(valueObject == null){
                    convertNullVar(
                            externalVariableEntry.getKey(),
                            lastSurveyUnitModel,
                            1,
                            variablesMap,
                            dstSurveyUnitModel.getExternalVariables(),
                            false);
                    continue;
                }
                if (valueObject instanceof List<?>) {
                    convertListVar(
                            valueObject,
                            lastSurveyUnitModel,
                            externalVariableEntry,
                            variablesMap,
                            dstSurveyUnitModel.getExternalVariables(),
                            false
                    );
                    continue;
                }
                convertOneVar(
                        externalVariableEntry.getKey(),
                        valueObject.toString(),
                        variablesMap,
                        1,
                        dstSurveyUnitModel.getExternalVariables()
                );
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
