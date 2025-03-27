package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonRawDataPersistencePort lunaticJsonRawDataNewPersistencePort) {
        this.lunaticJsonRawDataPersistencePort = lunaticJsonRawDataNewPersistencePort;
    }

    @Override
    public void save(LunaticJsonRawDataModel rawData) {
        lunaticJsonRawDataPersistencePort.save(rawData);
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawData(String campaignName, Mode mode, List<String> interrogationIdList) {
        return lunaticJsonRawDataPersistencePort.findRawData(campaignName, mode, interrogationIdList);
    }

    @Override
    public List<SurveyUnitModel> convertRawData(List<LunaticJsonRawDataModel> rawDataList, VariablesMap variablesMap) {
        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        //For each possible data state (we receive COLLECTED or EDITED)
        for(DataState dataState : List.of(DataState.COLLECTED,DataState.EDITED)){
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .campaignId(rawData.campaignId())
                        .questionnaireId(rawData.questionnaireId())
                        .mode(rawData.mode())
                        .interrogationId(rawData.interrogationId())
                        .state(dataState)
                        .fileDate(rawData.recordDate())
                        .recordDate(LocalDateTime.now())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                //Data collected variables conversion
                processRawDataCollectedVariables(rawData, surveyUnitModel, dataState, variablesMap);

                //External variables conversion into COLLECTED document
                if(dataState.equals(DataState.COLLECTED)){
                    processRawDataExtractedVariables(rawData, surveyUnitModel, variablesMap);
                }

                if(!surveyUnitModel.getCollectedVariables().isEmpty()
                        || !surveyUnitModel.getExternalVariables().isEmpty()
                ){
                    surveyUnitModels.add(surveyUnitModel);
                }
            }
        }

        return surveyUnitModels;
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        List<LunaticJsonRawDataUnprocessedDto> dtos = new ArrayList<>();

        for (LunaticJsonRawDataModel dataModel : lunaticJsonRawDataPersistencePort.getAllUnprocessedData()) {
            dtos.add(LunaticJsonRawDataUnprocessedDto.builder()
                    .campaignId(dataModel.campaignId())
                    .interrogationId(dataModel.interrogationId())
                    .build()
            );
        }
        return dtos;
    }

    private static void processRawDataExtractedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        Map<String,Object> externalMap = JsonUtils.asMap(srcRawData.data().get("EXTERNAL"));
        if (!externalMap.isEmpty()){
            for(Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()){
                Object valueObject = externalVariableEntry.getValue();
                if (valueObject instanceof List<?>){
                    //Array of values
                    List<String> values = JsonUtils.asStringList(valueObject);
                    if(!values.isEmpty()){
                        int iteration = 1;
                        for(String value : values) {
                            VariableModel externalVariableModel = VariableModel.builder()
                                    .varId(externalVariableEntry.getKey())
                                    .value(value)
                                    .scope(getIdLoop(variablesMap, externalVariableEntry.getKey()))
                                    .iteration(iteration)
                                    .parentId(GroupUtils.getParentGroupName(externalVariableEntry.getKey(),
                                            variablesMap))
                                    .build();

                            dstSurveyUnitModel.getExternalVariables().add(externalVariableModel);
                            iteration++;
                        }
                    }
                    continue;
                }
                //Value
                if (valueObject != null) {
                    VariableModel externalVariableModel = VariableModel.builder()
                            .varId(externalVariableEntry.getKey())
                            .value(valueObject.toString())
                            .scope(getIdLoop(variablesMap, externalVariableEntry.getKey()))
                            .iteration(1)
                            .parentId(GroupUtils.getParentGroupName(externalVariableEntry.getKey(), variablesMap))
                            .build();
                    dstSurveyUnitModel.getExternalVariables().add(externalVariableModel);
                }
            }
        }
    }

    private void processRawDataCollectedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        Map<String,Object> collectedMap = JsonUtils.asMap(srcRawData.data().get("COLLECTED"));
        if (!collectedMap.isEmpty()){
            for(Map.Entry<String, Object> collectedVariable : collectedMap.entrySet()) {

                //Skip if collected variable does not have state
                if(!JsonUtils.asMap(collectedVariable.getValue()).containsKey(dataState.toString())){
                    continue;
                }

                //Value
                Object valuesForState = JsonUtils.asMap(collectedVariable.getValue()).get(dataState.toString());
                if (valuesForState != null) {
                    if (valuesForState instanceof List<?>) {
                        List<String> values = JsonUtils.asStringList(valuesForState);
                        if (!values.isEmpty()) {
                            int iteration = 1;
                            for (String value : values) {
                                VariableModel collectedVariableModel = VariableModel.builder()
                                        .varId(collectedVariable.getKey())
                                        .value(value)
                                        .scope(getIdLoop(variablesMap, collectedVariable.getKey()))
                                        .iteration(iteration)
                                        .parentId(GroupUtils.getParentGroupName(collectedVariable.getKey(), variablesMap))
                                        .build();
                                dstSurveyUnitModel.getCollectedVariables().add(collectedVariableModel);
                                iteration++;
                            }
                        }
                        continue;
                    }
                    VariableModel collectedVariableModel = VariableModel.builder()
                            .varId(collectedVariable.getKey())
                            .value(valuesForState.toString())
                            .scope(getIdLoop(variablesMap, collectedVariable.getKey()))
                            .iteration(1)
                            .parentId(GroupUtils.getParentGroupName(collectedVariable.getKey(), variablesMap))
                            .build();
                    dstSurveyUnitModel.getCollectedVariables().add(collectedVariableModel);
                }
            }
        }
    }


    private static String getIdLoop(VariablesMap variablesMap, String variableName) {
        if (variablesMap.getVariable(variableName) == null) {
            log.warn("Variable {} not present in metadatas, assigning to {}", variableName, Constants.ROOT_GROUP_NAME);
            return Constants.ROOT_GROUP_NAME;
        }
        return variablesMap.getVariable(variableName).getGroupName();
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> campaignIds = new HashSet<>();
        for (SurveyUnitModel surveyUnitModel : surveyUnitModels) {
            campaignIds.add(surveyUnitModel.getCampaignId());
        }

        for (String campaignId : campaignIds) {
            Set<String> interrogationIds = new HashSet<>();
            for (SurveyUnitModel surveyUnitModel :
                    surveyUnitModels.stream().filter(
                            surveyUnitModel -> surveyUnitModel.getCampaignId().equals(campaignId)
                    ).toList()) {
                interrogationIds.add(surveyUnitModel.getInterrogationId());
            }
            lunaticJsonRawDataPersistencePort.updateProcessDates(campaignId, interrogationIds);
        }
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIds();
    }

    @Override
    public long countResponsesByQuestionnaireId(String campaignId) {
        return lunaticJsonRawDataPersistencePort.countResponsesByQuestionnaireId(campaignId);
    }

    /*
    *//**
     * Parse collected variables from raw data JSON
     * @param rootNode root JSON node of input raw data
     * @return a map of collected variables with the name of the variable as key
     * @throws GenesisException if any problem during parsing
     *//*
    private Map<String, LunaticJsonRawDataCollectedVariable> getCollectedVariablesFromJson(JsonNode rootNode) throws GenesisException {
        Map<String, LunaticJsonRawDataCollectedVariable> lunaticJsonRawDataCollectedVariables = new HashMap<>();

        if(!rootNode.has(LunaticJsonRawDataVariableType.COLLECTED.getJsonNodeName())){
            return lunaticJsonRawDataCollectedVariables;
        }

        Iterator<Map.Entry<String, JsonNode>> variables =
                rootNode.get(LunaticJsonRawDataVariableType.COLLECTED.getJsonNodeName()).fields();
        while (variables.hasNext()) {
            Map.Entry<String, JsonNode> variableNode = variables.next();
            LunaticJsonRawDataCollectedVariable lunaticJsonRawDataCollectedVariable =
                    LunaticJsonRawDataCollectedVariable.builder()
                            .collectedVariableByStateMap(new EnumMap<>(DataState.class))
                            .build();
            Iterator<Map.Entry<String, JsonNode>> states =
                    variableNode.getValue().fields();

            if(!states.hasNext()){
                throw new GenesisException(400, "Invalid JSON structure: Variable %s does not have any state (%s)"
                        .formatted(variableNode.getKey(), Arrays.stream(DataState.values()).toList()));
            }

            while (states.hasNext()){
                Map.Entry<String, JsonNode> stateNode = states.next();

                DataState dataState;
                //Check if data state (ex: COLLECTED) is in enum
                try{
                    dataState = DataState.valueOf(stateNode.getKey());
                }catch (IllegalArgumentException e){
                    throw new GenesisException(400, "Invalid JSON : Data state %s contained in variable %s is not supported"
                            .formatted(stateNode.getKey(), variableNode.getKey()));
                }

                //Parse values
                LunaticJsonRawDataVariable lunaticJsonRawDataVariable;
                if (stateNode.getValue().isArray()) {
                    //If is array of values
                    lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                            .valuesArray(new ArrayList<>())
                            .build();
                    for (JsonNode valueNode : stateNode.getValue()) {
                        lunaticJsonRawDataVariable.valuesArray().add(valueNode.asText());
                    }
                } else {
                    //If only 1 value
                    lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                            .value(stateNode.getValue().asText())
                            .build();
                }
                lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().put(dataState, lunaticJsonRawDataVariable);
            }
            lunaticJsonRawDataCollectedVariables.put(variableNode.getKey(), lunaticJsonRawDataCollectedVariable);
        }
        return lunaticJsonRawDataCollectedVariables;
    }

    *//**
     * Parse other than collected variables from raw data JSON
     * These variables are defined by the lack of state (COLLECTED, EDITED...) in their structure
     * @param rootNode root JSON node of input raw data
     * @return a map of variables with the name of the variable as key
     * @throws GenesisException if any problem during parsing
     *//*
    private Map<String, LunaticJsonRawDataVariable> getOtherVariablesFromJson(
            JsonNode rootNode,
            //Don't mind the warning, we expect only EXTERNAL for now but this method is open for another types
            LunaticJsonRawDataVariableType variableType
    ) {

        if (!rootNode.has(variableType.getJsonNodeName())) {
            return new HashMap<>();
        }

        //Parse from json
        Map<String, LunaticJsonRawDataVariable> lunaticJsonRawDataVariables = new HashMap<>();
        Iterator<Map.Entry<String, JsonNode>> variables = rootNode.get(variableType.getJsonNodeName()).fields();
        while (variables.hasNext()) {
            Map.Entry<String, JsonNode> variableNode = variables.next();
            LunaticJsonRawDataVariable lunaticJsonRawDataVariable;
            if (variableNode.getValue().isArray()) {
                //If is array of values
                lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                        .valuesArray(new ArrayList<>())
                        .build();
                for (JsonNode valueNode : variableNode.getValue()) {
                    lunaticJsonRawDataVariable.valuesArray().add(valueNode.asText());
                }
                lunaticJsonRawDataVariables.put(variableNode.getKey(), lunaticJsonRawDataVariable);
            } else {
                //If only 1 value
                lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                        .value(variableNode.getValue().asText())
                        .build();
            }
            lunaticJsonRawDataVariables.put(variableNode.getKey(), lunaticJsonRawDataVariable);
        }
        return lunaticJsonRawDataVariables;
    }*/
}
