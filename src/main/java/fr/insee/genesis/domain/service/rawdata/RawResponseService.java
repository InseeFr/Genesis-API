package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.GroupUtils;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.time.Instant;
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
public class RawResponseService implements RawResponseApiPort {

    private final ControllerUtils controllerUtils;
    private final QuestionnaireMetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final SurveyUnitQualityToolPort surveyUnitQualityToolPort;
    private final DataProcessingContextService dataProcessingContextService;
    private final FileUtils fileUtils;
    private final Config config;

    @Qualifier("rawResponseMongoAdapter")
    private final RawResponsePersistencePort rawResponsePersistencePort;

    public RawResponseService(ControllerUtils controllerUtils, QuestionnaireMetadataService metadataService, SurveyUnitService surveyUnitService, SurveyUnitQualityService surveyUnitQualityService, SurveyUnitQualityToolPort surveyUnitQualityToolPort, DataProcessingContextService dataProcessingContextService, FileUtils fileUtils, Config config, RawResponsePersistencePort rawResponsePersistencePort) {
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityService = surveyUnitQualityService;
        this.surveyUnitQualityToolPort = surveyUnitQualityToolPort;
        this.dataProcessingContextService = dataProcessingContextService;
        this.fileUtils = fileUtils;
        this.config = config;
        this.rawResponsePersistencePort = rawResponsePersistencePort;
    }

    @Override
    public List<RawResponse> getRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList) {
        return rawResponsePersistencePort.findRawResponses(collectionInstrumentId,mode,interrogationIdList);
    }

    @Override
    public DataProcessResult processRawResponses(String collectionInstrumentId, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException {
        int dataCount=0;
        int formattedDataCount=0;
        DataProcessingContextModel dataProcessingContext =
                dataProcessingContextService.getContextByCollectionInstrumentId(collectionInstrumentId);
        List<Mode> modesList = controllerUtils.getModesList(collectionInstrumentId, null);
        for (Mode mode : modesList) {
            //Load and save metadata into database, throw exception if none
            VariablesMap variablesMap = getVariablesMap(collectionInstrumentId,mode,errors);
            int totalBatchs = Math.ceilDiv(interrogationIdList.size() , config.getRawDataProcessingBatchSize());
            int batchNumber = 1;
            List<String> interrogationIdListForMode = new ArrayList<>(interrogationIdList);
            while(!interrogationIdListForMode.isEmpty()){
                log.info("Processing raw data batch {}/{}", batchNumber, totalBatchs);
                int maxIndex = Math.min(interrogationIdListForMode.size(), config.getRawDataProcessingBatchSize());
                List<String> interrogationIdToProcess = interrogationIdListForMode.subList(0, maxIndex);

                List<RawResponse> rawResponses = getRawResponses(collectionInstrumentId, mode, interrogationIdToProcess);

                List<SurveyUnitModel> surveyUnitModels = convertRawResponse(
                        rawResponses,
                        variablesMap
                );

                //Save converted data
                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                surveyUnitService.saveSurveyUnits(surveyUnitModels);

                //Update process dates
                updateProcessDates(surveyUnitModels);

                //Increment data count
                dataCount += surveyUnitModels.size();
                formattedDataCount += surveyUnitModels.stream()
                        .filter(surveyUnitModel -> surveyUnitModel.getState().equals(DataState.FORMATTED))
                        .toList()
                        .size();

                //Send processed ids grouped by questionnaire (if review activated)
                if(dataProcessingContext != null && dataProcessingContext.isWithReview()) {
                    sendProcessedIdsToQualityTool(surveyUnitModels);
                }

                //Remove processed ids from list
                interrogationIdListForMode = interrogationIdListForMode.subList(maxIndex, interrogationIdListForMode.size());

                batchNumber++;
            }
        }
        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    @Override
    public DataProcessResult processRawResponses(String collectionInstrumentId) throws GenesisException {
        int dataCount=0;
        int formattedDataCount=0;
        DataProcessingContextModel dataProcessingContext =
                dataProcessingContextService.getContextByCollectionInstrumentId(collectionInstrumentId);
        List<GenesisError> errors = new ArrayList<>();

        List<Mode> modesList = controllerUtils.getModesList(collectionInstrumentId, null);
        for (Mode mode : modesList) {
            //Load and save metadata into database, throw exception if none
            VariablesMap variablesMap = getVariablesMap(collectionInstrumentId,mode,errors);
            Set<String> interrogationIds =
                    rawResponsePersistencePort.findUnprocessedInterrogationIdsByCollectionInstrumentId(collectionInstrumentId);

            int totalBatchs = Math.ceilDiv(interrogationIds.size() , config.getRawDataProcessingBatchSize());
            int batchNumber = 1;
            List<String> interrogationIdListForMode = new ArrayList<>(interrogationIds);
            while(!interrogationIdListForMode.isEmpty()){
                log.info("Processing raw data batch {}/{}", batchNumber, totalBatchs);
                int maxIndex = Math.min(interrogationIdListForMode.size(), config.getRawDataProcessingBatchSize());

                List<SurveyUnitModel> surveyUnitModels = getConvertedSurveyUnits(
                        collectionInstrumentId,
                        mode,
                        interrogationIdListForMode,
                        maxIndex,
                        variablesMap);

                //Save converted data
                surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
                surveyUnitService.saveSurveyUnits(surveyUnitModels);

                //Update process dates
                updateProcessDates(surveyUnitModels);

                //Increment data count
                dataCount += surveyUnitModels.size();
                formattedDataCount += surveyUnitModels.stream()
                        .filter(surveyUnitModel -> surveyUnitModel.getState().equals(DataState.FORMATTED))
                        .toList()
                        .size();

                //Send processed ids grouped by questionnaire (if review activated)
                if(dataProcessingContext != null && dataProcessingContext.isWithReview()) {
                    sendProcessedIdsToQualityTool(surveyUnitModels);
                }

                //Remove processed ids from list
                interrogationIdListForMode = interrogationIdListForMode.subList(maxIndex, interrogationIdListForMode.size());
                batchNumber++;
            }
        }
        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    private List<SurveyUnitModel> getConvertedSurveyUnits(String collectionInstrumentId, Mode mode, List<String> interrogationIdListForMode, int maxIndex, VariablesMap variablesMap) {
        List<String> interrogationIdToProcess = interrogationIdListForMode.subList(0, maxIndex);
        List<RawResponse> rawResponses = getRawResponses(collectionInstrumentId, mode, interrogationIdToProcess);
        return convertRawResponse(
                rawResponses,
                variablesMap
        );
    }

    private VariablesMap getVariablesMap(String collectionInstrumentId, Mode mode, List<GenesisError> errors) throws GenesisException {
        VariablesMap variablesMap = metadataService.loadAndSaveIfNotExists(collectionInstrumentId, collectionInstrumentId, mode, fileUtils,
                errors).getVariables();
        if (variablesMap == null) {
            throw new GenesisException(400,
                    "Error during metadata parsing for mode %s :%n%s"
                            .formatted(mode, errors.getLast().getMessage())
            );
        }
        return variablesMap;
    }

    @Override
    public List<SurveyUnitModel> convertRawResponse(List<RawResponse> rawResponses, VariablesMap variablesMap) {
        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        //For each possible data state (we receive COLLECTED or EDITED)
        for(DataState dataState : List.of(DataState.COLLECTED,DataState.EDITED)){
            for (RawResponse rawResponse : rawResponses) {
                //Get optional fields
                Boolean isCapturedIndirectly = getIsCapturedIndirectly(rawResponse);
                LocalDateTime validationDate = getValidationDate(rawResponse);
                String usualSurveyUnitId = getStringFieldInPayload(rawResponse,"usualSurveyUnitId");
                String majorModelVersion = getStringFieldInPayload(rawResponse, "majorModelVersion");

                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .collectionInstrumentId(rawResponse.collectionInstrumentId())
                        .majorModelVersion(majorModelVersion)
                        .mode(rawResponse.mode())
                        .interrogationId(rawResponse.interrogationId())
                        .usualSurveyUnitId(usualSurveyUnitId)
                        .validationDate(validationDate)
                        .isCapturedIndirectly(isCapturedIndirectly)
                        .state(dataState)
                        .fileDate(rawResponse.recordDate())
                        .recordDate(LocalDateTime.now())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                //Data collected variables conversion
                convertRawDataCollectedVariables(rawResponse, surveyUnitModel, dataState, variablesMap);

                //External variables conversion into COLLECTED document
                if(dataState == DataState.COLLECTED){
                    convertRawDataExternalVariables(rawResponse, surveyUnitModel, variablesMap);
                }

                boolean hasNoVariable = surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty();

                if(hasNoVariable){
                    if(surveyUnitModel.getState() == DataState.COLLECTED){
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.", rawResponse.interrogationId());
                    }
                    continue;// don't add suModel
                }
                surveyUnitModels.add(surveyUnitModel);
            }
        }
        return surveyUnitModels;
    }

    @Override
    public List<String> getUnprocessedCollectionInstrumentIds() {
        return rawResponsePersistencePort.getUnprocessedCollectionIds();
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> collectionInstrumentIds = new HashSet<>();
        for (SurveyUnitModel surveyUnitModel : surveyUnitModels) {
            collectionInstrumentIds.add(surveyUnitModel.getCollectionInstrumentId());
        }

        for (String collectionInstrumentId : collectionInstrumentIds) {
            Set<String> interrogationIds = surveyUnitModels.stream()
                    .filter(su -> su.getCollectionInstrumentId().equals(collectionInstrumentId))
                    .map(SurveyUnitModel::getInterrogationId)
                    .collect(Collectors.toSet());
            rawResponsePersistencePort.updateProcessDates(collectionInstrumentId, interrogationIds);
        }
    }

    private Map<String, Set<String>> getProcessedIdsMap(List<SurveyUnitModel> surveyUnitModels) {
        Map<String, Set<String>> processedInterrogationIdsPerQuestionnaire = new HashMap<>();
        surveyUnitModels.forEach(model ->
                processedInterrogationIdsPerQuestionnaire
                        .computeIfAbsent(model.getCollectionInstrumentId(), k -> new HashSet<>())
                        .add(model.getInterrogationId())
        );
        return processedInterrogationIdsPerQuestionnaire;
    }

    private void sendProcessedIdsToQualityTool(List<SurveyUnitModel> surveyUnitModels) {
        try {
            Map<String, Set<String>> processedIdsMap = getProcessedIdsMap(surveyUnitModels);
            ResponseEntity<Object> response =
                    surveyUnitQualityToolPort.sendProcessedIds(processedIdsMap);

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent {} ids to quality tool", processedIdsMap.size());
            }else{
                log.warn("Survey unit quality tool responded non-2xx code {} and body {}",
                        response.getStatusCode(), response.getBody());
            }
        }catch (IOException e){
            log.error("Error during Perret call request building : {}", e.toString());
        }
    }

    private static Boolean getIsCapturedIndirectly(RawResponse rawResponse) {
        try{
            return rawResponse.payload().get("isCapturedIndirectly") == null ? null :
                    Boolean.parseBoolean(rawResponse.payload().get("isCapturedIndirectly").toString());
        }catch(Exception e){
            log.warn("Exception when parsing isCapturedIndirectly : {}",e.toString());
            return Boolean.FALSE;
        }
    }

    private static LocalDateTime getValidationDate(RawResponse rawResponse) {
        try{
            return rawResponse.payload().get("validationDate") == null ? null :
                    LocalDateTime.parse(rawResponse.payload().get("validationDate").toString());
        }catch(Exception e){
            log.warn("Exception when parsing validation date : {}",e.toString());
            return null;
        }
    }

    private static String getStringFieldInPayload(RawResponse rawResponse, String field) {
        try{
            return rawResponse.payload().get(field).toString();
        }catch(Exception e){
            log.warn("Exception when parsing {} : {}",field, e.toString());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void convertRawDataCollectedVariables(
            RawResponse rawResponse,
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawResponse.payload();
        dataMap = (Map<String, Object>) dataMap.get("data");

        dataMap = (Map<String, Object>)dataMap.get("COLLECTED");


        Map<String,Object> collectedMap = JsonUtils.asMap(dataMap);
        if (collectedMap == null || collectedMap.isEmpty()){
            if(dataState.equals(DataState.COLLECTED)) {
                log.warn("No collected data for interrogation {}", rawResponse.interrogationId());
            }
            return;
        }
        convertToCollectedVar(dstSurveyUnitModel, dataState, variablesMap, collectedMap);
    }

    private static void convertToCollectedVar(
            SurveyUnitModel dstSurveyUnitModel,
            DataState dataState,
            VariablesMap variablesMap,
            Map<String, Object> collectedMap
    ) {
        final String stateKey = dataState.toString();
        final var dest = dstSurveyUnitModel.getCollectedVariables();

        for (Map.Entry<String, Object> collectedVariable : collectedMap.entrySet()) {
            // Map for this variable (COLLECTED/EDITED -> value)
            Map<String, Object> states = JsonUtils.asMap(collectedVariable.getValue());

            if (states != null && states.containsKey(stateKey)) {
                Object value = states.get(stateKey);

                // liste ?
                if (value instanceof List<?>) {
                    // on garde exactement ta signature existante
                    convertListVar(value, collectedVariable, variablesMap, dest);
                }

                // scalaire non null ?
                if (value != null && !(value instanceof List<?>)) {
                    // idem: on garde convertOneVar(entry, String, ...)
                    convertOneVar(collectedVariable, String.valueOf(value), variablesMap, 1, dest);
                }
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

    private static String getIdLoop(VariablesMap variablesMap, String variableName) {
        if (variablesMap.getVariable(variableName) == null) {
            log.warn("Variable {} not present in metadata, assigning to {}", variableName, Constants.ROOT_GROUP_NAME);
            return Constants.ROOT_GROUP_NAME;
        }
        return variablesMap.getVariable(variableName).getGroupName();
    }

    @SuppressWarnings("unchecked")
    private static void convertRawDataExternalVariables(
            RawResponse rawResponse,
            SurveyUnitModel dstSurveyUnitModel,
            VariablesMap variablesMap
    ) {
        Map<String, Object> dataMap = rawResponse.payload();
        dataMap = (Map<String, Object>) dataMap.get("data");


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

    @Override
    public Page<RawResponse> findRawResponseDataByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable) {
        return rawResponsePersistencePort.findByCampaignIdAndDate(campaignId,startDate, endDate,pageable);
    }
}
