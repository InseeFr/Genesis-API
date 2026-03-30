package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.exceptions.GenesisException;

import java.time.LocalDateTime;

public interface ReprocessRawResponseApiPort {

    /**
     * Reprocesses raw data of the collection that correspond to the given identifier.
     * An optional date interval can be given to reprocess a subset of the collection.
     * @param rawDataModelType {@link RawDataModelType}
     * @param collectionInstrumentId Collection instrument identifier.
     * @param sinceDate Start of the date interval.
     * @param endDate End of the date interval.
     * @return Data processing result record.
     * @see DataProcessResult
     */
    DataProcessResult reprocessRawResponses(
            RawDataModelType rawDataModelType,
            String collectionInstrumentId, LocalDateTime sinceDate, LocalDateTime endDate)
            throws GenesisException;

}
