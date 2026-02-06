package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.controller.dto.rawdata.CombinedRawDataDto;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponsePersistencePort;
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
                lunaticJsonRawDataPersistencePort.findRawDataByInterrogationID(interrogationId);

        return new CombinedRawDataDto(
                rawResponseModels,
                lunaticRawDataModels
        );
    }


}
