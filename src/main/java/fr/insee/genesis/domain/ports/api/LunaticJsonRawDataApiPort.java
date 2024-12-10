package fr.insee.genesis.domain.ports.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.genesis.domain.model.surveyunit.Mode;

public interface LunaticJsonRawDataApiPort {
    void saveData(String campaignName, String dataJson, Mode mode) throws JsonProcessingException;
}
