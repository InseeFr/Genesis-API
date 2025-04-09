package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
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

    private final ControllerUtils controllerUtils;
    private final MetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final FileUtils fileUtils;

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonRawDataPersistencePort lunaticJsonRawDataNewPersistencePort, ControllerUtils controllerUtils, MetadataService metadataService, SurveyUnitService surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, FileUtils fileUtils) {
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
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
    public DataProcessResult processRawData(String campaignName, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException {
        int dataCount=0;
        int formattedDataCount=0;
        List<Mode> modesList = controllerUtils.getModesList(campaignName, null);
        for (Mode mode : modesList) {
            //Load and save metadata into database, throw exception if none
            VariablesMap variablesMap = metadataService.readMetadatas(campaignName, mode.getModeName(), fileUtils,
                    errors);
            if (variablesMap == null) {
                throw new GenesisException(400,
                        "Error during metadata parsing for mode %s :%n%s"
                                .formatted(mode, errors.getLast().getMessage())
                );
            }

            List<LunaticJsonRawDataModel> rawData = getRawData(campaignName,mode, interrogationIdList);
            //Save converted data
            List<SurveyUnitModel> surveyUnitModels = convertRawData(
                    rawData,
                    variablesMap
            );

            surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
            surveyUnitService.saveSurveyUnits(surveyUnitModels);

            //Update process dates
            updateProcessDates(surveyUnitModels);

            //Increment data count
            dataCount += surveyUnitModels.size();
            formattedDataCount += surveyUnitModels.stream().filter(
                    surveyUnitModel -> surveyUnitModel.getState().equals(DataState.FORMATTED)
            ).toList().size();
        }
        return new DataProcessResult(dataCount, formattedDataCount);
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
                convertRawDataCollectedVariables(rawData, surveyUnitModel, dataState, variablesMap);

                //External variables conversion into COLLECTED document
                if(dataState.equals(DataState.COLLECTED)){
                    convertRawDataExternalVariables(rawData, surveyUnitModel, variablesMap);
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

    private static void convertRawDataExternalVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        Map<String,Object> externalMap = JsonUtils.asMap(srcRawData.data().get("EXTERNAL"));
        if (externalMap != null && !externalMap.isEmpty()){
            convertToExternalVar(dstSurveyUnitModel, variablesMap, externalMap);
        }
    }

    private static void convertToExternalVar(SurveyUnitModel dstSurveyUnitModel, VariablesMap variablesMap, Map<String, Object> externalMap) {
        for(Map.Entry<String, Object> externalVariableEntry : externalMap.entrySet()){
            Object valueObject = externalVariableEntry.getValue();
            if (valueObject instanceof List<?>){
                //Array of values
                convertListVar(valueObject, externalVariableEntry, variablesMap, dstSurveyUnitModel.getExternalVariables());
                continue;
            }
            //Value
            if (valueObject != null) {
                convertOneVar(externalVariableEntry, valueObject.toString(), variablesMap, 1, dstSurveyUnitModel.getExternalVariables());
            }
        }
    }

    private static void convertOneVar(Map.Entry<String, Object> externalVariableEntry, String valueObject, VariablesMap variablesMap, int iteration, List<VariableModel> dstSurveyUnitModel) {
        VariableModel externalVariableModel = VariableModel.builder()
                .varId(externalVariableEntry.getKey())
                .value(valueObject)
                .scope(getIdLoop(variablesMap, externalVariableEntry.getKey()))
                .iteration(iteration)
                .parentId(GroupUtils.getParentGroupName(externalVariableEntry.getKey(), variablesMap))
                .build();
        dstSurveyUnitModel.add(externalVariableModel);
    }

    private void convertRawDataCollectedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        Map<String,Object> collectedMap = JsonUtils.asMap(srcRawData.data().get("COLLECTED"));
        if (collectedMap == null || collectedMap.isEmpty()){return;}
        convertToCollectedVar(dstSurveyUnitModel, dataState, variablesMap, collectedMap);

    }

    private static void convertToCollectedVar(SurveyUnitModel dstSurveyUnitModel, DataState dataState, VariablesMap variablesMap, Map<String, Object> collectedMap) {
        for(Map.Entry<String, Object> collectedVariable : collectedMap.entrySet()) {

            //Skip if collected variable does not have state
            if(!JsonUtils.asMap(collectedVariable.getValue()).containsKey(dataState.toString())){
                continue;
            }

            //Value
            Object valuesForState = JsonUtils.asMap(collectedVariable.getValue()).get(dataState.toString());
            if (valuesForState != null) {
                if (valuesForState instanceof List<?>) {
                    convertListVar(valuesForState, collectedVariable, variablesMap, dstSurveyUnitModel.getCollectedVariables());
                    continue;
                }
                convertOneVar(collectedVariable, valuesForState.toString(), variablesMap, 1, dstSurveyUnitModel.getCollectedVariables());
            }
        }
    }

    private static void convertListVar(Object valuesForState, Map.Entry<String, Object> collectedVariable, VariablesMap variablesMap, List<VariableModel> dstSurveyUnitModel) {
        List<String> values = JsonUtils.asStringList(valuesForState);
        if (!values.isEmpty()) {
            int iteration = 1;
            for (String value : values) {
                convertOneVar(collectedVariable, value, variablesMap, iteration, dstSurveyUnitModel);
                iteration++;
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
}
