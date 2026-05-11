package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.dto.rawdata.ProcessingResultDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.converter.rawdata.LunaticJsonRawDataConverter;
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
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityToolService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.DataAccessException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;


@Service
@Slf4j
@RequiredArgsConstructor
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {

    private final ControllerUtils controllerUtils;
    private final QuestionnaireMetadataService metadataService;
    private final SurveyUnitService surveyUnitService;
    private final SurveyUnitQualityService surveyUnitQualityService;
    private final SurveyUnitQualityToolService surveyUnitQualityToolService;
    private final FileUtils fileUtils;
    private final Config config;
    private final LunaticJsonRawDataConverter lunaticJsonRawDataConverter;

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;

    @Qualifier("dataProcessingContextMongoAdapter")
    private final DataProcessingContextPersistancePort dataProcessingContextPersistancePort;

    @Override
    public void save(LunaticJsonRawDataModel rawData) throws GenesisException {
        try {
            lunaticJsonRawDataPersistencePort.save(rawData);
        } catch (DataAccessException e) {
            throw new GenesisException(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error");
        } catch (IllegalArgumentException e) {
            throw new GenesisException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByQuestionnaireId(
            String questionnaireId,
            Mode mode,
            List<String> interrogationIdList
    ) {
        return lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(
                questionnaireId,
                mode,
                interrogationIdList
        );
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByInterrogationId(String interrogationId) {
        return lunaticJsonRawDataPersistencePort.findRawDataByInterrogationId(interrogationId);
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
        boolean shouldUseQualityTool =
                surveyUnitQualityToolService.resolveWithReviewValue(questionnaireId);

        int batchSize = config.getRawDataProcessingBatchSize();
        int dataCount = 0;
        int formattedDataCount = 0;

        for (Mode mode : modes) {
            VariablesMap variablesMap = getVariablesMap(questionnaireId, mode, errors);

            for (int fromIndex = 0; fromIndex < interrogationIdList.size(); fromIndex += batchSize) {
                int toIndex = Math.min(fromIndex + batchSize, interrogationIdList.size());
                List<String> batch = interrogationIdList.subList(fromIndex, toIndex);

                log.info(
                        "Processing raw data batch [{}-{}] / {} for questionnaireId={} mode={}",
                        fromIndex + 1,
                        toIndex,
                        interrogationIdList.size(),
                        questionnaireId,
                        mode
                );

                ProcessingResultDto processingResultDto = processRawDataForMode(
                        questionnaireId,
                        mode,
                        batch,
                        variablesMap,
                        shouldUseQualityTool
                );

                dataCount += processingResultDto.dataCount();
                formattedDataCount += processingResultDto.formattedDataCount();
            }
        }

        return new DataProcessResult(dataCount, formattedDataCount, errors);
    }

    private ProcessingResultDto processRawDataForMode(
            String questionnaireId,
            Mode mode,
            List<String> interrogationIds,
            VariablesMap variablesMap,
            boolean shouldUseQualityTool
    ) {
        List<LunaticJsonRawDataModel> rawData =
                lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(
                        questionnaireId,
                        mode,
                        interrogationIds
                );

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();
        List<SurveyUnitModel> surveyUnitModels =
                lunaticJsonRawDataConverter.convertRawDataAndCollectEmptyModels(
                        rawData,
                        variablesMap,
                        emptySurveyUnitModels
                );

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
            String questionnaireId,
            Mode mode,
            List<GenesisError> errors
    ) throws GenesisException {
        VariablesMap variablesMap = metadataService.loadAndSaveIfNotExists(
                questionnaireId,
                questionnaireId,
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
    public List<SurveyUnitModel> convertRawData(
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap
    ) {
        return lunaticJsonRawDataConverter.convertRawData(rawDataList, variablesMap);
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> collectionInstrumentIds = surveyUnitModels.stream()
                .map(SurveyUnitModel::getCollectionInstrumentId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        for (String collectionInstrumentId : collectionInstrumentIds) {
            Set<String> interrogationIds = surveyUnitModels.stream()
                    .filter(su -> collectionInstrumentId.equals(su.getCollectionInstrumentId()))
                    .map(SurveyUnitModel::getInterrogationId)
                    .collect(Collectors.toSet());
            lunaticJsonRawDataPersistencePort.updateProcessDates(collectionInstrumentId, interrogationIds);
        }
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        List<LunaticJsonRawDataUnprocessedDto> dtos = new ArrayList<>();

        for (GroupedInterrogation groupedInterrogation : lunaticJsonRawDataPersistencePort.findUnprocessedIds()) {
            for (String interrogationId : groupedInterrogation.interrogationIds()) {
                dtos.add(LunaticJsonRawDataUnprocessedDto.builder()
                        .questionnaireId(groupedInterrogation.questionnaireId())
                        .interrogationId(interrogationId)
                        .build());
            }
        }

        return dtos;
    }

    @Override
    public Set<String> getUnprocessedDataQuestionnaireIds() {
        Set<String> unprocessedQuestionnaireIds =
                lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIdsByNullProcessDate();
        Set<String> unprocessedQuestionnaireIdsWithSpecs = new HashSet<>();

        for (String unprocessedQuestionnaireId : unprocessedQuestionnaireIds) {
            Set<Mode> modes = lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(unprocessedQuestionnaireId);
            if (modes.isEmpty()) {
                continue;
            }

            boolean areAllSpecsOK = true;
            for (Mode mode : modes) {
                if (!isSpecsPresentForQuestionnaireAndMode(unprocessedQuestionnaireId, mode)) {
                    areAllSpecsOK = false;
                }
            }

            if (areAllSpecsOK) {
                unprocessedQuestionnaireIdsWithSpecs.add(unprocessedQuestionnaireId);
            }
        }

        return unprocessedQuestionnaireIdsWithSpecs;
    }

    private boolean isSpecsPresentForQuestionnaireAndMode(String questionnaireId, Mode mode) {
        List<GenesisError> genesisErrors = new ArrayList<>();
        MetadataModel metadataModel;

        try {
            metadataModel = metadataService.loadAndSaveIfNotExists(
                    questionnaireId,
                    questionnaireId,
                    mode,
                    fileUtils,
                    genesisErrors
            );
        } catch (GenesisException ge) {
            log.warn(
                    "Genesis exception thrown for questionnaire {} and mode {}, excluding from get questionnaire ids...",
                    questionnaireId,
                    mode
            );
            return false;
        }

        return metadataModel != null && genesisErrors.isEmpty();
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIds();
    }

    @Override
    public long countRawResponsesByQuestionnaireId(String questionnaireId) {
        return lunaticJsonRawDataPersistencePort.countRawResponsesByQuestionnaireId(questionnaireId);
    }

    @Override
    public long countDistinctInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return lunaticJsonRawDataPersistencePort.countDistinctInterrogationIdsByQuestionnaireId(questionnaireId);
    }

    @Override
    public Map<String, List<String>> findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime since) {
        List<GroupedInterrogation> idsByQuestionnaire =
                lunaticJsonRawDataPersistencePort.findProcessedIdsGroupedByQuestionnaireSince(since);

        List<String> collectionInstrumentIds = idsByQuestionnaire.stream()
                .map(GroupedInterrogation::questionnaireId)
                .toList();

        List<DataProcessingContextModel> contexts =
                dataProcessingContextPersistancePort.findByCollectionInstrumentIds(collectionInstrumentIds);

        List<String> collectionInstrumentIdsWithReview = contexts.stream()
                .filter(DataProcessingContextModel::isWithReview)
                .map(DataProcessingContextModel::getCollectionInstrumentId)
                .toList();

        return idsByQuestionnaire.stream()
                .filter(groupedInterrogation ->
                        collectionInstrumentIdsWithReview.contains(groupedInterrogation.questionnaireId()))
                .collect(Collectors.toMap(
                        GroupedInterrogation::questionnaireId,
                        GroupedInterrogation::interrogationIds
                ));
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(String questionnaireId, Pageable pageable) {
        return lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(questionnaireId, pageable);
    }

    @Override
    public boolean existsByInterrogationId(String interrogationId) {
        return lunaticJsonRawDataPersistencePort.existsByInterrogationId(interrogationId);
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByCampaignIdAndDate(
            String campaignId,
            Instant startDt,
            Instant endDt,
            Pageable pageable
    ) {
        return lunaticJsonRawDataPersistencePort.findByCampaignIdAndDate(campaignId, startDt, endDt, pageable);
    }

    public static String getValueString(Object value) {
        if (value instanceof Double || value instanceof Float) {
            BigDecimal bd = new BigDecimal(value.toString());
            return bd.stripTrailingZeros().toPlainString();
        }
        if (value instanceof Number) {
            return value.toString();
        }
        return String.valueOf(value);
    }
}