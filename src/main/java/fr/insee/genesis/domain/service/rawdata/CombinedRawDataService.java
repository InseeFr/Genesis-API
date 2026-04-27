package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.controller.dto.rawdata.RawDataIdentifiersDto;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import fr.insee.genesis.exceptions.NoDataException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
//TODO create API port interface
public class CombinedRawDataService {

    @Qualifier("lunaticJsonMongoAdapter")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;
    @Qualifier("rawResponseMongoAdapter")
    private final RawResponsePersistencePort rawResponsePersistencePort;

    public CombinedRawDataDto getCombinedRawDataByInterrogationId(String interrogationId) {

        List<RawResponseModel> rawResponseModels =
                rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId);

        List<LunaticJsonRawDataModel> lunaticRawDataModels =
                lunaticJsonRawDataPersistencePort.findRawDataByInterrogationId(interrogationId);

        return new CombinedRawDataDto(
                rawResponseModels,
                lunaticRawDataModels
        );
    }

    public RawDataIdentifiersDto getRawDataIdentifiersByCollectionInstrumentId(
            String collectionInstrumentId
    ) throws NoDataException {

        RawDataIdentifiersDto rawResult =
                rawResponsePersistencePort.findRawResponseIdentifiersByCollectionInstrumentId(
                        collectionInstrumentId
                );

        if (rawResult != null) {
            return rawResult;
        }

        RawDataIdentifiersDto lunaticResult =
                lunaticJsonRawDataPersistencePort.findLunaticJsonRawDataIdentifiersByQuestionnaireId(
                        collectionInstrumentId
                );

        if (lunaticResult != null) {
            return lunaticResult;
        }

        throw new NoDataException(
                "No raw data found for collectionInstrumentId=%s".formatted(collectionInstrumentId)
        );
    }

}
