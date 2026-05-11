package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.rawdata.ProcessingResultDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.converter.rawdata.RawResponseConverter;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityToolService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.modelefiliere.ModeDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
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
@RequiredArgsConstructor
public class RawResponseService implements RawResponseApiPort {

    private final ControllerUtils controllerUtils;
    private final QuestionnaireMetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final SurveyUnitQualityToolService surveyUnitQualityToolService;
    private final FileUtils fileUtils;
    private final Config config;
    private final RawResponseConverter rawResponseConverter;

    @Qualifier("rawResponseMongoAdapter")
    private final RawResponsePersistencePort rawResponsePersistencePort;

    @Override
    public List<RawResponseModel> getRawResponses(
            String collectionInstrumentId,
            Mode mode,
            List<String> interrogationIdList
    ) {
        return rawResponsePersistencePort.findRawResponses(collectionInstrumentId, mode, interrogationIdList);
    }

    @Override
    public List<RawResponseModel> getRawResponsesByInterrogationID(String interrogationId) {
        return rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId);
    }

    public DataProcessResult processRawResponsesByCollectionInstrumentId(String collectionInstrumentId) throws GenesisException {
        List<String> interrogationIds = rawResponsePersistencePort
                .findUnprocessedInterrogationIdsByCollectionInstrumentId(collectionInstrumentId)
                .stream()
                .toList();

        return processRawResponsesByInterrogationIds(
                collectionInstrumentId,
                interrogationIds,
                new ArrayList<>()
        );
    }

    @Override
    public List<RawResponseModel> getRawResponseByCollectionInstrumentIdAndInterrogationId(
            String collectionInstrumentId,
            String interrogationId
    ) throws NoDataException {
        List<RawResponseModel> rawResponses = rawResponsePersistencePort
                .findRawResponseByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                );

        if (rawResponses.isEmpty()) {
            throw new NoDataException(
                    "No raw responses found for collectionInstrumentId=%s and interrogationId=%s"
                            .formatted(collectionInstrumentId, interrogationId)
            );
        }
        return rawResponses;
    }

    @Override
    public DataProcessResult processRawResponsesByInterrogationIds(
            String collectionInstrumentId,
            List<String> interrogationIdList,
            List<GenesisError> errors
    ) throws GenesisException {

        List<Mode> modes = controllerUtils.getModesList(collectionInstrumentId, null);
        boolean shouldUseQualityTool =
                surveyUnitQualityToolService.resolveWithReviewValue(collectionInstrumentId);

        int batchSize = config.getRawDataProcessingBatchSize();
        int dataCount = 0;
        int formattedDataCount = 0;

        for (Mode mode : modes) {
            VariablesMap variablesMap = getVariablesMap(collectionInstrumentId, mode, errors);
            for (int fromIndex = 0; fromIndex < interrogationIdList.size(); fromIndex += batchSize) {
                int toIndex = Math.min(fromIndex + batchSize, interrogationIdList.size());
                List<String> batch = interrogationIdList.subList(fromIndex, toIndex);

                log.info(
                        "Processing raw data batch [{}-{}] / {} for collectionInstrumentId={} mode={}",
                        fromIndex + 1,
                        toIndex,
                        interrogationIdList.size(),
                        collectionInstrumentId,
                        mode
                );

                ProcessingResultDto result = processRawResponsesForMode(
                        collectionInstrumentId,
                        mode,
                        batch,
                        variablesMap,
                        shouldUseQualityTool
                );

                dataCount += result.dataCount();
                formattedDataCount += result.formattedDataCount();
            }
        }

        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    private ProcessingResultDto processRawResponsesForMode(
            String collectionInstrumentId,
            Mode mode,
            List<String> interrogationIds,
            VariablesMap variablesMap,
            boolean shouldUseQualityTool
    ) {
        List<RawResponseModel> rawResponseModels =
                rawResponsePersistencePort.findRawResponses(collectionInstrumentId, mode, interrogationIds);

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();
        List<SurveyUnitModel> surveyUnitModels =
                rawResponseConverter.convertRawResponseAndCollectEmptyModels(rawResponseModels, variablesMap, emptySurveyUnitModels);

        surveyUnitQualityService.verifySurveyUnits(surveyUnitModels, variablesMap);
        surveyUnitService.saveSurveyUnits(surveyUnitModels);

        updateProcessDates(surveyUnitModels);

        if (!emptySurveyUnitModels.isEmpty()) {
            updateProcessDates(emptySurveyUnitModels);
        }

        if (shouldUseQualityTool) {
            surveyUnitQualityToolService.sendProcessedIdsToQualityTool(surveyUnitModels);
        }

        int formattedDataCount = (int) surveyUnitModels.stream()
                .filter(surveyUnitModel -> surveyUnitModel.getState() == DataState.FORMATTED)
                .count();

        return new ProcessingResultDto(surveyUnitModels.size(), formattedDataCount);
    }

    private VariablesMap getVariablesMap(
            String collectionInstrumentId,
            Mode mode,
            List<GenesisError> errors
    ) throws GenesisException {
        VariablesMap variablesMap = metadataService.loadAndSaveIfNotExists(
                collectionInstrumentId,
                collectionInstrumentId,
                mode,
                fileUtils,
                errors
        ).getVariables();

        if (variablesMap == null) {
            throw new GenesisException(
                    HttpStatus.BAD_REQUEST,
                    "Error during metadata parsing for mode %s :%n%s"
                            .formatted(mode, errors.getLast().getMessage())
            );
        }

        return variablesMap;
    }

    @Override
    public List<String> getUnprocessedCollectionInstrumentIds() {
        return rawResponsePersistencePort.getUnprocessedCollectionIds().stream()
                .filter(this::hasModesWithAllSpecs)
                .toList();
    }

    private boolean hasModesWithAllSpecs(String collectionInstrumentId) {
        Set<ModeDto> modes = new HashSet<>(
                rawResponsePersistencePort.findModesByCollectionInstrument(collectionInstrumentId)
        );

        return hasAtLeastOneValidMode(modes)
                && modes.stream()
                .filter(Objects::nonNull)
                .map(modeDto -> Mode.getEnumFromJsonName(modeDto.toString()))
                .allMatch(mode -> isSpecsPresentForCollectionInstrumentAndMode(collectionInstrumentId, mode));
    }

    private static boolean hasAtLeastOneValidMode(Set<ModeDto> modes) {
        return modes.stream().anyMatch(Objects::nonNull);
    }

    private boolean isSpecsPresentForCollectionInstrumentAndMode(String unprocessedCollectionInstrumentId, Mode mode) {
        List<GenesisError> genesisErrors = new ArrayList<>();
        MetadataModel metadataModel;

        try {
            metadataModel = metadataService.loadAndSaveIfNotExists(
                    unprocessedCollectionInstrumentId,
                    unprocessedCollectionInstrumentId,
                    mode,
                    fileUtils,
                    genesisErrors
            );
        } catch (GenesisException ge) {
            log.warn(
                    "Genesis exception thrown for collection instrument {} and mode {}, excluding from get collection instrument ids...",
                    unprocessedCollectionInstrumentId,
                    mode
            );
            return false;
        }

        return metadataModel != null && genesisErrors.isEmpty();
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> collectionInstrumentIds = surveyUnitModels.stream()
                .map(SurveyUnitModel::getCollectionInstrumentId)
                .collect(Collectors.toSet());

        for (String collectionInstrumentId : collectionInstrumentIds) {
            Set<String> interrogationIds = surveyUnitModels.stream()
                    .filter(su -> su.getCollectionInstrumentId().equals(collectionInstrumentId))
                    .map(SurveyUnitModel::getInterrogationId)
                    .collect(Collectors.toSet());
            rawResponsePersistencePort.updateProcessDates(collectionInstrumentId, interrogationIds);
        }
    }

    @Override
    public boolean existsByInterrogationId(String interrogationId) {
        return rawResponsePersistencePort.existsByInterrogationId(interrogationId);
    }

    @Override
    public Page<RawResponseModel> findRawResponseDataByCampaignIdAndDate(
            String campaignId,
            Instant startDate,
            Instant endDate,
            Pageable pageable
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
            String collectionInstrumentId,
            Pageable pageable
    ) {
        return rawResponsePersistencePort.findByCollectionInstrumentId(collectionInstrumentId, pageable);
    }

}