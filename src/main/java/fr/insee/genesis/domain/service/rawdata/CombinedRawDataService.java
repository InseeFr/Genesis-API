package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.CombinedRawData;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Slf4j
public class CombinedRawDataService {

    @Qualifier("lunaticJsonMongoAdapterNew")
    private final LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;
    @Qualifier("rawResponseMongoAdapter")
    private final RawResponsePersistencePort rawResponsePersistencePort;

    public CombinedRawDataService(LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort, RawResponsePersistencePort rawResponsePersistencePort) {
        this.lunaticJsonRawDataPersistencePort = lunaticJsonRawDataPersistencePort;
        this.rawResponsePersistencePort = rawResponsePersistencePort;
    }

    public CombinedRawData getCombinedRawDataByInterrogationId(String interrogationId) {
        List<RawResponse> rawResponses = rawResponsePersistencePort.findRawResponsesByInterrogationID(interrogationId);
        List<LunaticJsonRawDataModel> lunaticRawData = lunaticJsonRawDataPersistencePort.findRawDataByInterrogationID(interrogationId);

        return new CombinedRawData(rawResponses, lunaticRawData);
    }

}
