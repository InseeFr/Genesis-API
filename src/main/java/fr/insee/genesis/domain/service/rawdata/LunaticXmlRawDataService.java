package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticXmlDataModel;
import fr.insee.genesis.domain.ports.api.RawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticXmlPersistancePort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class LunaticXmlRawDataService implements RawDataApiPort {
    @Qualifier("lunaticXmlMongoAdapter")
    private final LunaticXmlPersistancePort lunaticXmlPersistancePort;

    @Autowired
    public LunaticXmlRawDataService(LunaticXmlPersistancePort lunaticXmlPersistancePort) {
        this.lunaticXmlPersistancePort = lunaticXmlPersistancePort;
    }

    @Override
    public void saveData(LunaticXmlCampaign campaign, Mode mode) {
        LunaticXmlDataModel lunaticXmlDataModel = LunaticXmlDataModel.builder()
                .mode(mode)
                .data(campaign)
                .recordDate(LocalDateTime.now())
                .build();

        lunaticXmlPersistancePort.save(lunaticXmlDataModel);
    }
}
