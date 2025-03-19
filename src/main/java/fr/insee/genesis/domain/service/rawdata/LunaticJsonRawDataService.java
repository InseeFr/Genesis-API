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

}
