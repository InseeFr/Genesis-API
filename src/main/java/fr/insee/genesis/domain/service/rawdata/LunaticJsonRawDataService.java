package fr.insee.genesis.domain.service.rawdata;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawData;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataCollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataVariable;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataVariableType;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistancePort;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonDocumentMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@Slf4j
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {
    @Qualifier("lunaticJsonMongoAdapter")
    private final LunaticJsonRawDataPersistancePort lunaticJsonRawDataPersistancePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonRawDataPersistancePort lunaticJsonRawDataPersistancePort) {
        this.lunaticJsonRawDataPersistancePort = lunaticJsonRawDataPersistancePort;
    }

    @Override
    public void saveData(String campaignName, String questionnaireId, String interrogationId,
                         String idUE, String dataJson,
                         Mode mode) throws GenesisException {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try {
            JsonNode rootNode = mapper.readTree(dataJson);
            if (!rootNode.has(LunaticJsonRawDataVariableType.COLLECTED.getJsonNodeName())
                    && !rootNode.has(LunaticJsonRawDataVariableType.EXTERNAL.getJsonNodeName())
            ) {
                throw new GenesisException(400, "Invalid JSON : needs at least COLLECTED or EXTERNAL in root");
            }

            LunaticJsonRawData rawData = new LunaticJsonRawData(
                    getCollectedVariablesFromJson(rootNode),
                    getOtherVariablesFromJson(rootNode, LunaticJsonRawDataVariableType.EXTERNAL)
            );

            LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder()
                    .campaignId(campaignName)
                    .questionnaireId(questionnaireId)
                    .interrogationId(interrogationId)
                    .idUE(idUE)
                    .mode(mode)
                    .data(rawData)
                    .recordDate(LocalDateTime.now())
                    .build();

            lunaticJsonRawDataPersistancePort.save(lunaticJsonRawDataModel);
        } catch (JacksonException e) {
            throw new GenesisException(400, "Invalid JSON synthax : %s".formatted(e.getOriginalMessage()));
        }
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        List<LunaticJsonRawDataUnprocessedDto> dtos = new ArrayList<>();

        for (LunaticJsonRawDataModel dataModel : lunaticJsonRawDataPersistancePort.getAllUnprocessedData()) {
            dtos.add(LunaticJsonRawDataUnprocessedDto.builder()
                    .campaignId(dataModel.campaignId())
                    .interrogationId(dataModel.interrogationId())
                    .build()
            );
        }

        return dtos;
    }

    @Override
    public List<SurveyUnitModel> parseRawData(
            String campaignName,
            Mode mode,
            List<String> interrogationIdList,
            VariablesMap variablesMap
    ) {
        //Get concerned raw data
        List<LunaticJsonRawDataModel> rawDataList = LunaticJsonDocumentMapper.INSTANCE.listDocumentToListModel(
                lunaticJsonRawDataPersistancePort.findRawData(campaignName, mode, interrogationIdList)
        );
        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        //For each possible data state
        for(DataState dataState : getRawDataStates(rawDataList)){
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .campaignId(campaignName)
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

    private static void processRawDataExtractedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        for(Map.Entry<String, LunaticJsonRawDataVariable> externalVariableEntry
                : srcRawData.data().externalVariables().entrySet()){
            //Value
            if(externalVariableEntry.getValue().value() != null){
                VariableModel externalVariableModel = VariableModel.builder()
                        .varId(externalVariableEntry.getKey())
                        .value(externalVariableEntry.getValue().value())
                        .scope(getIdLoop(variablesMap, externalVariableEntry.getKey()))
                        .iteration(1)
                        .parentId(GroupUtils.getParentGroupName(externalVariableEntry.getKey(), variablesMap))
                        .build();

                dstSurveyUnitModel.getExternalVariables().add(externalVariableModel);
                continue;
            }
            //Array of values
            if(externalVariableEntry.getValue().valuesArray() != null){
                int iteration = 1;
                for(String value : externalVariableEntry.getValue().valuesArray()) {
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
        }
    }

    private void processRawDataCollectedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        for(Map.Entry<String, LunaticJsonRawDataCollectedVariable> collectedVariable
                : srcRawData.data().collectedVariables().entrySet()) {

            //Skip if collected variable does not have state
            if(!collectedVariable.getValue().collectedVariableByStateMap().containsKey(dataState)){
                continue;
            }

            //Value
            if (collectedVariable.getValue().collectedVariableByStateMap().get(dataState).value() != null
                    //To avoid case when both values
                    && collectedVariable.getValue().collectedVariableByStateMap().get(dataState).valuesArray() == null
            ) {
                VariableModel collectedVariableModel = VariableModel.builder()
                        .varId(collectedVariable.getKey())
                        .value(collectedVariable.getValue().collectedVariableByStateMap().get(dataState).value())
                        .scope(getIdLoop(variablesMap, collectedVariable.getKey()))
                        .iteration(1)
                        .parentId(GroupUtils.getParentGroupName(collectedVariable.getKey(), variablesMap))
                        .build();
                dstSurveyUnitModel.getCollectedVariables().add(collectedVariableModel);
            }

            //Array of values
            if(collectedVariable.getValue().collectedVariableByStateMap().get(dataState).valuesArray() != null) {
                int iteration = 1;
                for (String value :
                        collectedVariable.getValue().collectedVariableByStateMap().get(dataState).valuesArray()) {
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
        }
    }

    /**
     * Get all possible DataStates in a list of raw data models
     * @param rawDataList a list containing raw data models
     * @return a set containing all present DataStates
     */
    Set<DataState> getRawDataStates(List<LunaticJsonRawDataModel> rawDataList) {
        Set<DataState> dataStates = new HashSet<>();
        for(LunaticJsonRawDataModel lunaticJsonRawDataModel : rawDataList){
            for(Map.Entry<String, LunaticJsonRawDataCollectedVariable> variable : lunaticJsonRawDataModel.data().collectedVariables().entrySet()){
                dataStates.addAll(variable.getValue().collectedVariableByStateMap().keySet());
            }
            if(!lunaticJsonRawDataModel.data().externalVariables().isEmpty()){
                dataStates.add(DataState.COLLECTED);
            }
        }
        return dataStates;
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
            lunaticJsonRawDataPersistancePort.updateProcessDates(campaignId, interrogationIds);
        }
    }

    /**
     * Parse collected variables from raw data JSON
     * @param rootNode root JSON node of input raw data
     * @return a map of collected variables with the name of the variable as key
     * @throws GenesisException if any problem during parsing
     */
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

    /**
     * Parse other than collected variables from raw data JSON
     * These variables are defined by the lack of state (COLLECTED, EDITED...) in their structure
     * @param rootNode root JSON node of input raw data
     * @return a map of variables with the name of the variable as key
     * @throws GenesisException if any problem during parsing
     */
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
    }
}
