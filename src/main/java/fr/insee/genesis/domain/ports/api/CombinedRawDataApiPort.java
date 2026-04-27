package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.controller.dto.rawdata.RawDataIdentifiersDto;
import fr.insee.genesis.exceptions.NoDataException;

public interface CombinedRawDataApiPort {

    CombinedRawDataDto getCombinedRawDataByInterrogationId(String interrogationId);
    RawDataIdentifiersDto getRawDataIdentifiersByCollectionInstrumentId(
            String collectionInstrumentId
    ) throws NoDataException;
}
