package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonPersistancePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {
    @Qualifier("lunaticJsonMongoAdapter")
    private final LunaticJsonPersistancePort lunaticJsonPersistancePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonPersistancePort lunaticJsonPersistancePort) {
        this.lunaticJsonPersistancePort = lunaticJsonPersistancePort;
    }

    @Override
    public void saveData(String campaignName, String dataJson, Mode mode){
        LunaticJsonDataModel lunaticJsonDataModel = LunaticJsonDataModel.builder()
                .campaignId(campaignName)
                .mode(mode)
                .dataJson(dataJson)
                .recordDate(LocalDateTime.now())
                .build();

        lunaticJsonPersistancePort.save(lunaticJsonDataModel);
    }
}
