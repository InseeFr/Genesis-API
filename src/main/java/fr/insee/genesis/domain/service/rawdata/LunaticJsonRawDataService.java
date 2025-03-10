package fr.insee.genesis.domain.service.rawdata;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
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
    public void saveData(String campaignName, String dataJson, Mode mode) throws JsonParseException {
        if(!isJsonValid(dataJson)){
            throw new JsonParseException("Invalid JSON synthax");
        }
        LunaticJsonDataModel lunaticJsonDataModel = LunaticJsonDataModel.builder()
                .campaignId(campaignName)
                .mode(mode)
                .dataJson(dataJson)
                .recordDate(LocalDateTime.now())
                .build();

        lunaticJsonPersistancePort.save(lunaticJsonDataModel);
    }


    private boolean isJsonValid(String json) {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try {
            mapper.readTree(json);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }
}
