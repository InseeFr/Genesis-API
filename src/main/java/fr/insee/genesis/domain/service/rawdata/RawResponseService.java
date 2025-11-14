package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

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
    public List<RawResponse> getRawResponses(String questionnaireModelId, Mode mode, List<String> interrogationIdList) {
        return List.of();
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
            VariablesMap variablesMap = metadataService.loadAndSaveIfNotExists(collectionInstrumentId, collectionInstrumentId, mode, fileUtils,
                    errors).getVariables();
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
        return null;
    }

    @Override
    public List<SurveyUnitModel> convertRawResponse(List<RawResponse> rawResponses, VariablesMap variablesMap) {
/*        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        //For each possible data state (we receive COLLECTED or EDITED)
        for(DataState dataState : List.of(DataState.COLLECTED,DataState.EDITED)){
            for (RawResponse rawResponse : rawResponses) {
                //Get optional fields
                String contextualId = getContextualId(rawData);
                Boolean isCapturedIndirectly = getIsCapturedIndirectly(rawData);
                LocalDateTime validationDate = getValidationDate(rawData);

                SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                        .campaignId(rawData.campaignId())
                        .questionnaireId(rawData.questionnaireId())
                        .mode(rawData.mode())
                        .interrogationId(rawData.interrogationId())
                        .idUE(rawData.idUE())
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
                if(dataState == DataState.COLLECTED){
                    convertRawDataExternalVariables(rawData, surveyUnitModel, rawDataModelType, variablesMap);
                }

                boolean hasNoVariable = surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty();

                if(hasNoVariable){
                    if(surveyUnitModel.getState() == DataState.COLLECTED){
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.", rawData.interrogationId());
                    }
                    continue;// don't add suModel
                }
                surveyUnitModels.add(surveyUnitModel);
            }
        }

        return surveyUnitModels;*/
        return List.of();
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> collectionInstrumentIds = new HashSet<>();
        for (SurveyUnitModel surveyUnitModel : surveyUnitModels) {
            collectionInstrumentIds.add(surveyUnitModel.getQuestionnaireId());
        }

        for (String collectionInstrumentId : collectionInstrumentIds) {
            Set<String> interrogationIds = new HashSet<>();
            for (SurveyUnitModel surveyUnitModel :
                    surveyUnitModels.stream().filter(
                            surveyUnitModel -> surveyUnitModel.getQuestionnaireId().equals(collectionInstrumentId)
                    ).toList()) {
                interrogationIds.add(surveyUnitModel.getInterrogationId());
            }
            rawResponsePersistencePort.updateProcessDates(collectionInstrumentId, interrogationIds);
        }
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

    private void sendProcessedIdsToQualityTool(List<SurveyUnitModel> surveyUnitModels) {
        try {
            ResponseEntity<Object> response =
                    surveyUnitQualityToolPort.sendProcessedIds(getProcessedIdsMap(surveyUnitModels));

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("Successfully sent {} ids to quality tool", getProcessedIdsMap(surveyUnitModels).size());
            }else{
                log.warn("Survey unit quality tool responded non-2xx code {} and body {}",
                        response.getStatusCode(), response.getBody());
            }
        }catch (IOException e){
            log.error("Error during Perret call request building : {}", e.toString());
        }
    }
}
