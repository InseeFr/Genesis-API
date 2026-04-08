package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.ports.api.ReprocessRawResponseApiPort;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistenceRouter;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.InvalidDateIntervalException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReprocessRawResponseService implements ReprocessRawResponseApiPort {

    private final SurveyUnitPersistencePort surveyUnitPersistence;

    private final RawResponseApiPort rawResponseService;
    private final LunaticJsonRawDataApiPort lunaticJsonRawDataService;
    // Polymorphism for the raw data services would cause too much refactor.

    private final RawResponseReprocessPersistenceRouter rawResponseReprocessPersistenceRouter;
    private RawResponseReprocessPersistencePort rawResponseReprocessPersistencePort;

    private enum InputFilterType {
        COLLECTION_ID,
        COLLECTION_ID_AND_DATE
    }

    @Override
    public DataProcessResult reprocessRawResponses(
            @NonNull RawDataModelType rawDataModelType,
            @NonNull String collectionInstrumentId,
            LocalDateTime sinceDate,
            LocalDateTime endDate) throws GenesisException {

        log.info("Start reprocess {} data for collectionInstrumentId={}, sinceDate={}, endDate={}",
                rawDataModelType, collectionInstrumentId, sinceDate, endDate);

        InputFilterType inputFilterType = validateInputs(sinceDate, endDate);

        rawResponseReprocessPersistencePort = rawResponseReprocessPersistenceRouter.resolve(rawDataModelType);

        Set<String> interrogationIds = switch (inputFilterType) {
            case COLLECTION_ID ->
                    rawResponseReprocessPersistencePort
                            .findProcessedInterrogationIdsByCollectionInstrumentId(
                                    collectionInstrumentId);
            case COLLECTION_ID_AND_DATE ->
                    rawResponseReprocessPersistencePort
                            .findProcessedInterrogationIdsByCollectionInstrumentIdAndRecordDateBetween(
                                    collectionInstrumentId,
                                    sinceDate,
                                    effectiveEndDate(endDate));
        };

        return reprocessInterrogations(rawDataModelType, collectionInstrumentId, interrogationIds);
    }

    private static InputFilterType validateInputs(LocalDateTime sinceDate, LocalDateTime endDate) {
        if (bothDatesAreNull(sinceDate, endDate)) {
            return InputFilterType.COLLECTION_ID;
        }
        if (sinceDate == null) {
            throw new InvalidDateIntervalException("'endDate' cannot be provided without 'sinceDate'.");
        }
        if (endIsBeforeSince(sinceDate, endDate)) {
            throw new InvalidDateIntervalException("'endDate' value cannot be before 'sinceDate'.");
        }
        return InputFilterType.COLLECTION_ID_AND_DATE;
    }

    private static boolean endIsBeforeSince(LocalDateTime sinceDate, LocalDateTime endDate) {
        return endDate != null && endDate.isBefore(sinceDate);
    }

    private static boolean bothDatesAreNull(LocalDateTime sinceDate, LocalDateTime endDate) {
        return sinceDate == null && endDate == null;
    }

    private static LocalDateTime effectiveEndDate(LocalDateTime endDate) {
        if (endDate != null)
            return endDate;
        var now = LocalDateTime.now();
        log.info("Effective end date: {}", now);
        return now;
    }

    private DataProcessResult reprocessInterrogations(
            RawDataModelType rawDataModelType, String collectionInstrumentId, Set<String> interrogationIds)
            throws GenesisException {
        if (interrogationIds.isEmpty()) {
            return new DataProcessResult(0, 0, new ArrayList<>());
        }

        surveyUnitPersistence.deleteByCollectionInstrumentIdAndInterrogationIds(collectionInstrumentId, interrogationIds);
        rawResponseReprocessPersistencePort.resetProcessDates(collectionInstrumentId, interrogationIds);

        return switch (rawDataModelType) {
            case FILIERE -> rawResponseService.processRawResponsesByInterrogationIds(
                    collectionInstrumentId, new ArrayList<>(interrogationIds), new ArrayList<>());
            case LEGACY -> lunaticJsonRawDataService.processRawDataByInterrogationIds(
                    collectionInstrumentId, new ArrayList<>(interrogationIds), new ArrayList<>());
        };
    }

}
