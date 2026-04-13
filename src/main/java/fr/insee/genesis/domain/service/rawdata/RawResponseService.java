package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityToolService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.RawResponseConverter;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.InvalidMetadataException;
import fr.insee.genesis.exceptions.UndefinedMetadataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.modelefiliere.ModeDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@Slf4j
public class RawResponseService implements RawResponseApiPort {

    private final ControllerUtils controllerUtils;
    private final QuestionnaireMetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityToolService surveyUnitQualityToolService;
    private final FileUtils fileUtils;
    private final Config config;

    @Qualifier("rawResponseMongoAdapter")
    private final RawResponsePersistencePort rawResponsePersistencePort;

    public RawResponseService(
            ControllerUtils controllerUtils,
            QuestionnaireMetadataService metadataService,
            SurveyUnitService surveyUnitService,
            SurveyUnitQualityToolService surveyUnitQualityToolService,
            FileUtils fileUtils,
            Config config,
            RawResponsePersistencePort rawResponsePersistencePort
    ) {
        this.controllerUtils = controllerUtils;
        this.metadataService = metadataService;
        this.surveyUnitService = surveyUnitService;
        this.surveyUnitQualityToolService = surveyUnitQualityToolService;
        this.fileUtils = fileUtils;
        this.config = config;
        this.rawResponsePersistencePort = rawResponsePersistencePort;
    }

    @Override
    public DataProcessResult processRawResponsesByInterrogationIds(String collectionInstrumentId) {
        List<String> interrogationIds = rawResponsePersistencePort
                .findUnprocessedInterrogationIdsByCollectionInstrumentId(collectionInstrumentId)
                .stream().toList();
        return processRawResponsesByInterrogationIds(collectionInstrumentId, interrogationIds, new ArrayList<>());
    }

    @Override
    public DataProcessResult processRawResponsesByInterrogationIds(
            String collectionInstrumentId,
            List<String> interrogationIdList,
            List<GenesisError> errors
    ) {
        List<Mode> modes = controllerUtils.getModesList(collectionInstrumentId);
        boolean resolvedWithReviewValue = surveyUnitQualityToolService.resolveWithReviewValue(collectionInstrumentId);

        int batchSize = config.getRawDataProcessingBatchSize();
        int totalBatches = Math.ceilDiv(interrogationIdList.size(), batchSize);
        int dataCount = 0;
        int formattedDataCount = 0;

        for (Mode mode : modes) {
            VariablesMap variablesMap = loadAndSaveMetadata(collectionInstrumentId, mode, errors);
            List<String> interrogationIdListForMode = new ArrayList<>(interrogationIdList);
            int batchNumber = 1;

            while (!interrogationIdListForMode.isEmpty()) {
                log.info("Processing raw data batch {}/{}", batchNumber, totalBatches);

                int maxIndex = Math.min(interrogationIdListForMode.size(), batchSize);
                List<String> batch = interrogationIdListForMode.subList(0, maxIndex);

                List<RawResponseModel> rawModels = rawResponsePersistencePort
                        .findRawResponses(collectionInstrumentId, mode, batch);
                rawModels.removeIf(r -> r.processDate() != null);

                List<SurveyUnitModel> surveyUnits = RawResponseConverter.convertRawData(rawModels, variablesMap);

                surveyUnitService.saveSurveyUnits(surveyUnits);
                updateProcessDates(surveyUnits);

                dataCount += surveyUnits.size();
                formattedDataCount += (int) surveyUnits.stream()
                        .filter(su -> su.getState().equals(DataState.FORMATTED))
                        .count();

                if (resolvedWithReviewValue) {
                    surveyUnitQualityToolService.sendProcessedIdsToQualityTool(surveyUnits);
                }

                interrogationIdListForMode = interrogationIdListForMode.subList(maxIndex, interrogationIdListForMode.size());
                batchNumber++;
            }
        }

        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    @Override
    public List<String> getUnprocessedCollectionInstrumentIds() {
        return rawResponsePersistencePort.getUnprocessedCollectionIds().stream()
                .filter(this::hasValidModesWithSpecs)
                .collect(Collectors.toList());
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        surveyUnitModels.stream()
                .map(SurveyUnitModel::getCollectionInstrumentId)
                .distinct()
                .forEach(id -> {
                    Set<String> ids = surveyUnitModels.stream()
                            .filter(su -> su.getCollectionInstrumentId().equals(id))
                            .map(SurveyUnitModel::getInterrogationId)
                            .collect(Collectors.toSet());
                    rawResponsePersistencePort.updateProcessDates(id, ids);
                });
    }

    @Override
    public boolean existsByInterrogationId(String interrogationId) {
        return rawResponsePersistencePort.existsByInterrogationId(interrogationId);
    }

    @Override
    public Page<RawResponseModel> findRawResponseDataByCampaignIdAndDate(
            String campaignId, Instant startDate, Instant endDate, Pageable pageable
    ) {
        return rawResponsePersistencePort.findByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
    }

    @Override
    public long countDistinctInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId) {
        return rawResponsePersistencePort.countByCollectionInstrumentId(collectionInstrumentId);
    }

    @Override
    public long countByCollectionInstrumentId(String collectionInstrumentId) {
        return rawResponsePersistencePort.countByCollectionInstrumentId(collectionInstrumentId);
    }

    @Override
    public Set<String> getDistinctCollectionInstrumentIds() {
        return new HashSet<>(rawResponsePersistencePort.findDistinctCollectionInstrumentIds());
    }

    @Override
    public Page<RawResponseModel> findRawResponseDataByCollectionInstrumentId(
            String collectionInstrumentId, Pageable pageable
    ) {
        return rawResponsePersistencePort.findByCollectionInstrumentId(collectionInstrumentId, pageable);
    }


    /** Load and save metadata into database, throw exception if none. */
    private VariablesMap loadAndSaveMetadata(
            String collectionInstrumentId, Mode mode, List<GenesisError> errors
    ) {
        VariablesMap variablesMap;
        try {
            variablesMap = metadataService.loadAndSaveIfNotExists(
                    collectionInstrumentId, collectionInstrumentId, mode, fileUtils, errors
            ).getVariables();
        } catch (GenesisException e) {
            throw new UndefinedMetadataException(
                    "Cannot load metadata for collection instrument %s and mode %s."
                            .formatted(collectionInstrumentId, mode), e);
        }
        if (variablesMap == null) {
            throw new InvalidMetadataException(
                    "Error during metadata parsing for mode %s :%n%s"
                            .formatted(mode, errors.getLast().getMessage()));
        }
        return variablesMap;
    }

    private boolean hasValidModesWithSpecs(String collectionInstrumentId) {
        Set<ModeDto> modes = new HashSet<>(
                rawResponsePersistencePort.findModesByCollectionInstrument(collectionInstrumentId)
        );
        if (modes.isEmpty()) return false;
        if (modes.contains(null) && modes.size() == 1) return false;

        return modes.stream()
                .filter(Objects::nonNull)
                .map(m -> Mode.getEnumFromJsonName(m.toString()))
                .allMatch(mode -> isSpecsPresentForMode(collectionInstrumentId, mode));
    }

    private boolean isSpecsPresentForMode(String collectionInstrumentId, Mode mode) {
        List<GenesisError> errors = new ArrayList<>();
        try {
            MetadataModel metadata = metadataService.loadAndSaveIfNotExists(
                    collectionInstrumentId, collectionInstrumentId, mode, fileUtils, errors
            );
            return metadata != null && errors.isEmpty();
        } catch (GenesisException e) {
            log.warn("Genesis exception for collection instrument {} and mode {}, excluding.",
                    collectionInstrumentId, mode);
            return false;
        }
    }
}