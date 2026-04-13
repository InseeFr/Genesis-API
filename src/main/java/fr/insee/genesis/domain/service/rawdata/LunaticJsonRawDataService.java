package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityToolService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.LunaticJsonRawDataConverter;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {

    private final ControllerUtils controllerUtils;
    private final QuestionnaireMetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityToolService surveyUnitQualityToolService;
    private final FileUtils fileUtils;
    private final Config config;

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;
    @Qualifier("dataProcessingContextMongoAdapter")
    private final DataProcessingContextPersistancePort dataProcessingContextPersistancePort;

    @Autowired
    public LunaticJsonRawDataService(
            LunaticJsonRawDataPersistencePort lunaticJsonRawDataNewPersistencePort,
            ControllerUtils controllerUtils,
            QuestionnaireMetadataService metadataService,
            SurveyUnitService surveyUnitService,
            FileUtils fileUtils,
            SurveyUnitQualityToolService surveyUnitQualityToolService,
            Config config,
            DataProcessingContextPersistancePort dataProcessingContextPersistancePort
    ) {
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityToolService = surveyUnitQualityToolService;
        this.fileUtils = fileUtils;
        this.lunaticJsonRawDataPersistencePort = lunaticJsonRawDataNewPersistencePort;
        this.dataProcessingContextPersistancePort = dataProcessingContextPersistancePort;
        this.config = config;
    }

    @Override
    public void save(LunaticJsonRawDataModel rawData) {
        lunaticJsonRawDataPersistencePort.save(rawData);
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByQuestionnaireId(
            String questionnaireId, Mode mode, List<String> interrogationIdList
    ) {
        return lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(
                questionnaireId, mode, interrogationIdList);
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByInterrogationId(String interrogationId) {
        return lunaticJsonRawDataPersistencePort.findRawDataByInterrogationID(interrogationId);
    }

    @Override
    public DataProcessResult processRawData(String questionnaireId) throws GenesisException {
        List<String> interrogationIds = lunaticJsonRawDataPersistencePort
                .findUnprocessedInterrogationIdsByCollectionInstrumentId(questionnaireId)
                .stream()
                .toList();

        return processRawDataByInterrogationIds(questionnaireId, interrogationIds, new ArrayList<>());
    }

    @Override
    public DataProcessResult processRawDataByInterrogationIds(
            String questionnaireId,
            List<String> interrogationIdList,
            List<GenesisError> errors
    ) throws GenesisException {

        List<Mode> modes = controllerUtils.getModesList(questionnaireId, null);
        boolean resolveWithReviewValue = surveyUnitQualityToolService.resolveWithReviewValue(questionnaireId);

        int batchSize = config.getRawDataProcessingBatchSize();
        int totalBatches = Math.ceilDiv(interrogationIdList.size(), batchSize);
        int dataCount = 0;
        int formattedDataCount = 0;

        for (Mode mode : modes) {
            VariablesMap variablesMap = getVariablesMap(questionnaireId, mode, errors);
            List<String> interrogationIdListForMode = new ArrayList<>(interrogationIdList);
            int batchNumber = 1;

            while (!interrogationIdListForMode.isEmpty()) {
                log.info("Processing raw data batch {}/{}", batchNumber, totalBatches);

                int maxIndex = Math.min(interrogationIdListForMode.size(), batchSize);
                List<String> batch = interrogationIdListForMode.subList(0, maxIndex);

                List<LunaticJsonRawDataModel> rawData = lunaticJsonRawDataPersistencePort
                        .findRawDataByQuestionnaireId(questionnaireId, mode, batch);

                List<SurveyUnitModel> surveyUnits =
                        LunaticJsonRawDataConverter.convertRawData(rawData, variablesMap);

                surveyUnitService.saveSurveyUnits(surveyUnits);
                updateProcessDates(surveyUnits);

                dataCount += surveyUnits.size();
                formattedDataCount += (int) surveyUnits.stream()
                        .filter(su -> su.getState() == DataState.FORMATTED)
                        .count();

                if (resolveWithReviewValue) {
                    surveyUnitQualityToolService.sendProcessedIdsToQualityTool(surveyUnits);
                }

                interrogationIdListForMode =
                        interrogationIdListForMode.subList(maxIndex, interrogationIdListForMode.size());
                batchNumber++;
            }
        }

        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        List<LunaticJsonRawDataUnprocessedDto> dtos = new ArrayList<>();
        for (GroupedInterrogation groupedInterrogation : lunaticJsonRawDataPersistencePort.findUnprocessedIds()) {
            for (String interrogationId : groupedInterrogation.interrogationIds()) {
                dtos.add(LunaticJsonRawDataUnprocessedDto.builder()
                        .campaignId(groupedInterrogation.partitionOrCampaignId())
                        .questionnaireId(groupedInterrogation.questionnaireId())
                        .interrogationId(interrogationId)
                        .build());
            }
        }
        return dtos;
    }

    @Override
    public Set<String> getUnprocessedDataQuestionnaireIds() {
        return lunaticJsonRawDataPersistencePort
                .findDistinctQuestionnaireIdsByNullProcessDate()
                .stream()
                .filter(this::hasValidModesWithSpecs)
                .collect(Collectors.toSet());
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        surveyUnitModels.stream()
                .map(SurveyUnitModel::getCampaignId)
                .distinct()
                .forEach(campaignId -> {
                    Set<String> ids = surveyUnitModels.stream()
                            .filter(su -> su.getCampaignId().equals(campaignId))
                            .map(SurveyUnitModel::getInterrogationId)
                            .collect(Collectors.toSet());
                    lunaticJsonRawDataPersistencePort.updateProcessDates(campaignId, ids);
                });
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIds();
    }

    @Override
    public long countRawResponsesByQuestionnaireId(String campaignId) {
        return lunaticJsonRawDataPersistencePort.countRawResponsesByQuestionnaireId(campaignId);
    }

    @Override
    public long countDistinctInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return lunaticJsonRawDataPersistencePort.countDistinctInterrogationIdsByQuestionnaireId(questionnaireId);
    }

    @Override
    public Map<String, List<String>> findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime since) {
        List<GroupedInterrogation> idsByQuestionnaire =
                lunaticJsonRawDataPersistencePort.findProcessedIdsGroupedByQuestionnaireSince(since);
        List<String> partitionIds = idsByQuestionnaire.stream()
                .map(GroupedInterrogation::partitionOrCampaignId).toList();
        List<String> partitionIdsWithReview = dataProcessingContextPersistancePort
                .findByPartitionIds(partitionIds).stream()
                .filter(DataProcessingContextModel::isWithReview)
                .map(DataProcessingContextModel::getPartitionId)
                .toList();
        return idsByQuestionnaire.stream()
                .filter(g -> partitionIdsWithReview.contains(g.partitionOrCampaignId()))
                .collect(Collectors.toMap(
                        GroupedInterrogation::questionnaireId,
                        GroupedInterrogation::interrogationIds
                ));
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(
            String questionnaireId, Pageable pageable
    ) {
        return lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(questionnaireId, pageable);
    }

    @Override
    public boolean existsByInterrogationId(String interrogationId) {
        return lunaticJsonRawDataPersistencePort.existsByInterrogationId(interrogationId);
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByCampaignIdAndDate(
            String campaignId, Instant startDt, Instant endDt, Pageable pageable
    ) {
        return lunaticJsonRawDataPersistencePort.findByCampaignIdAndDate(campaignId, startDt, endDt, pageable);
    }

    private VariablesMap getVariablesMap(
            String questionnaireId, Mode mode, List<GenesisError> errors
    ) throws GenesisException {
        VariablesMap variablesMap = metadataService.loadAndSaveIfNotExists(
                questionnaireId, questionnaireId, mode, fileUtils, errors
        ).getVariables();
        if (variablesMap == null) {
            throw new GenesisException(400,
                    "Error during metadata parsing for mode %s :%n%s"
                            .formatted(mode, errors.getLast().getMessage()));
        }
        return variablesMap;
    }

    private boolean hasValidModesWithSpecs(String questionnaireId) {
        Set<Mode> modes = lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(questionnaireId);
        if (modes.isEmpty()) return false;
        return modes.stream().allMatch(mode -> isSpecsPresentForMode(questionnaireId, mode));
    }

    private boolean isSpecsPresentForMode(String questionnaireId, Mode mode) {
        List<GenesisError> errors = new ArrayList<>();
        try {
            MetadataModel metadata = metadataService.loadAndSaveIfNotExists(
                    questionnaireId, questionnaireId, mode, fileUtils, errors
            );
            return metadata != null && errors.isEmpty();
        } catch (GenesisException e) {
            log.warn("Genesis exception for questionnaire {} and mode {}, excluding.", questionnaireId, mode);
            return false;
        }
    }
}