package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
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
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {

    private final ControllerUtils controllerUtils;
    private final MetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final SurveyUnitQualityToolPort surveyUnitQualityToolPort;
    private final DataProcessingContextService dataProcessingContextService;
    private final FileUtils fileUtils;
    private final Config config;

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;
    @Qualifier("dataProcessingContextMongoAdapter")
    private final DataProcessingContextPersistancePort dataProcessingContextPersistancePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonRawDataPersistencePort lunaticJsonRawDataNewPersistencePort,
                                     ControllerUtils controllerUtils,
                                     MetadataService metadataService,
                                     SurveyUnitService surveyUnitService,
                                     SurveyUnitQualityService surveyUnitQualityService,
                                     FileUtils fileUtils,
                                     DataProcessingContextService dataProcessingContextService,
                                     SurveyUnitQualityToolPort surveyUnitQualityToolPort,
                                     Config config,
                                     DataProcessingContextPersistancePort dataProcessingContextPersistancePort
    ) {
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.fileUtils = fileUtils;
        this.lunaticJsonRawDataPersistencePort = lunaticJsonRawDataNewPersistencePort;
        this.dataProcessingContextPersistancePort = dataProcessingContextPersistancePort;
        this.surveyUnitQualityToolPort = surveyUnitQualityToolPort;
        this.dataProcessingContextService = dataProcessingContextService;
        this.config = config;
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
        DataProcessingContextModel dataProcessingContext =
                dataProcessingContextService.getContextByPartitionId(campaignName);
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
            int totalBatchs = Math.ceilDiv(interrogationIdList.size() , config.getRawDataProcessingBatchSize());
            int batchNumber = 1;
            List<String> interrogationIdListForMode = new ArrayList<>(interrogationIdList);
            while(!interrogationIdListForMode.isEmpty()){
                log.info("Processing raw data batch {}/{}", batchNumber, totalBatchs);
                int maxIndex = Math.min(interrogationIdListForMode.size(), config.getRawDataProcessingBatchSize());
                List<String> interrogationIdToProcess = interrogationIdListForMode.subList(0, maxIndex);

                List<LunaticJsonRawDataModel> rawData = getRawData(campaignName,mode, interrogationIdToProcess);

                List<SurveyUnitModel> surveyUnitModels = convertRawData(
                        rawData,
                        variablesMap
                );

                //Save converted data
                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                surveyUnitService.saveSurveyUnits(surveyUnitModels);

                //Update process dates
                updateProcessDates(surveyUnitModels);

                //Increment data count
                dataCount += surveyUnitModels.size();
                formattedDataCount += surveyUnitModels.stream().filter(
                        surveyUnitModel -> surveyUnitModel.getState().equals(DataState.FORMATTED)
                ).toList().size();

                //Send processed ids grouped by questionnaire (if review activated)
                if(dataProcessingContext != null && dataProcessingContext.isWithReview()) {
                    try {
                        ResponseEntity<Object> response =
                                surveyUnitQualityToolPort.sendProcessedIds(getProcessedIdsMap(surveyUnitModels));

                    if (response.getStatusCode().is2xxSuccessful()) {
                        log.info("Successfully sent {} ids to quality tool", getProcessedIdsMap(surveyUnitModels).keySet().size());
                    }else{
                        log.warn("Survey unit quality tool responded non-2xx code {} and body {}",
                                response.getStatusCode(), response.getBody());
                    }
                    }catch (IOException e){
                        log.error("Error during Perret call request building : {}", e.toString());
                    }
                }

                //Remove processed ids from list
                interrogationIdListForMode = interrogationIdListForMode.subList(maxIndex, interrogationIdListForMode.size());

                batchNumber++;
            }
        }
        return new DataProcessResult(dataCount, formattedDataCount);
    }

    private Map<String, Set<String>> getProcessedIdsMap(List<SurveyUnitModel> surveyUnitModels) {
        Map<String, Set<String>> processedInterrogationIdsPerQuestionnaire = new HashMap<>();
        surveyUnitModels.forEach(model ->
                processedInterrogationIdsPerQuestionnaire
                        .computeIfAbsent(model.getQuestionnaireId(), k -> new HashSet<>())
                        .add(model.getInterrogationId())
        );
        return processedInterrogationIdsPerQuestionnaire;
    }

    @Override
    public List<SurveyUnitModel> convertRawData(List<LunaticJsonRawDataModel> rawDataList, VariablesMap variablesMap) {
        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        //For each possible data state (we receive COLLECTED or EDITED)
        for(DataState dataState : List.of(DataState.COLLECTED,DataState.EDITED)){
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                RawDataModelType rawDataModelType =
                        rawData.data().containsKey("data") ?
                                RawDataModelType.FILIERE :
                                RawDataModelType.DEFAULT;

                //Get optional fields
                String contextualId = null;
                Boolean isCapturedIndirectly = null;
                LocalDateTime validationDate = null;
                try{
                    contextualId = rawData.data().get("contextualId") == null ? null : rawData.data().get("contextualId").toString();
                    isCapturedIndirectly = rawData.data().get("isCapturedIndirectly") == null ? null :
                            Boolean.parseBoolean(rawData.data().get("isCapturedIndirectly").toString());
                    validationDate = rawData.data().get("validationDate") == null ? null :
                            LocalDateTime.parse(rawData.data().get("validationDate").toString());
                }catch(Exception e){
                    log.warn("Exception during optional fields parsing : %s".formatted(e.toString()));
                }

                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .campaignId(rawData.campaignId())
                        .questionnaireId(rawData.questionnaireId())
                        .mode(rawData.mode())
                        .interrogationId(rawData.interrogationId())
                        .contextualId(contextualId)
                        .validationDate(validationDate)
                        .isCapturedIndirectly(isCapturedIndirectly)
                        .state(dataState)
                        .fileDate(rawData.recordDate())
                        .recordDate(LocalDateTime.now())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                //Data collected variables conversion
                convertRawDataCollectedVariables(rawData, surveyUnitModel, dataState, rawDataModelType, variablesMap);

                //External variables conversion into COLLECTED document
                if(dataState.equals(DataState.COLLECTED)){
                    convertRawDataExternalVariables(rawData, surveyUnitModel, rawDataModelType, variablesMap);
                }

                if(surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty()
                ){
                    if(surveyUnitModel.getState() == DataState.COLLECTED){
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.", rawData.interrogationId());
                    }
                    continue;
                }
                surveyUnitModels.add(surveyUnitModel);
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
                    .questionnaireId(dataModel.questionnaireId())
                    .interrogationId(dataModel.interrogationId())
                    .build()
            );
        }
        return dtos;
    }

    @SuppressWarnings("unchecked")
    private static void convertRawDataExternalVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = srcRawData.data();
        if (rawDataModelType.equals(RawDataModelType.FILIERE)) {
            dataMap = (Map<String, Object>) dataMap.get("data");
        }

        dataMap = (Map<String, Object>)dataMap.get("EXTERNAL");
        Map<String,Object> externalMap = JsonUtils.asMap(dataMap);
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

    @SuppressWarnings("unchecked")
    private void convertRawDataCollectedVariables(
            LunaticJsonRawDataModel srcRawData,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            RawDataModelType rawDataModelType,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = srcRawData.data();
        if (rawDataModelType.equals(RawDataModelType.FILIERE)) {
            dataMap = (Map<String, Object>) dataMap.get("data");
        }

        dataMap = (Map<String, Object>)dataMap.get("COLLECTED");


        Map<String,Object> collectedMap = JsonUtils.asMap(dataMap);
        if (collectedMap == null || collectedMap.isEmpty()){
            if(dataState.equals(DataState.COLLECTED)) {
                log.warn("No collected data for interrogation {}", srcRawData.interrogationId());
            }
            return;
        }
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

    @Override
    public Map<String, List<String>> findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime since) {
        List<GroupedInterrogation> idsByQuestionnaire = lunaticJsonRawDataPersistencePort.findProcessedIdsGroupedByQuestionnaireSince(since);
        List<String> partitionIds = idsByQuestionnaire.stream().map(GroupedInterrogation::partitionOrCampaignId).toList();
        List<DataProcessingContextModel> contexts = dataProcessingContextPersistancePort.findByPartitionIds(partitionIds);
        List<String> partitionIdsWithReview = contexts.stream().filter(DataProcessingContextModel::isWithReview).map(DataProcessingContextModel::getPartitionId).toList();
        return idsByQuestionnaire.stream().filter(groupedInterrogation -> partitionIdsWithReview.contains(groupedInterrogation.partitionOrCampaignId()))
                .collect(Collectors.toMap(
                GroupedInterrogation::questionnaireId,
                GroupedInterrogation::interrogationIds
        ));
    }
}
