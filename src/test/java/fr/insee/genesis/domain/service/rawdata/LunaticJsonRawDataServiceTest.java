package fr.insee.genesis.domain.service.rawdata;

import com.fasterxml.jackson.core.JsonParseException;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.stubs.LunaticJsonPersistanceStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class LunaticJsonRawDataServiceTest {
    LunaticJsonPersistanceStub lunaticJsonPersistanceStub = new LunaticJsonPersistanceStub();
    LunaticJsonRawDataService lunaticJsonRawDataService = new LunaticJsonRawDataService(lunaticJsonPersistanceStub);

    @Test
    void saveDataTest_Invalid(){
        lunaticJsonPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";

        Assertions.assertThatThrownBy(() -> {
            lunaticJsonRawDataService.saveData(
                    campaignId,
                    "interrogationId",
                    "idUE",
                    "questionnaireId",
                    Mode.WEB,
                    "{\"testdata\": \"ERROR");
        }).isInstanceOf(JsonParseException.class);
    }
}