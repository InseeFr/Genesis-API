package fr.insee.genesis.domain.ports.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.genesis.domain.model.surveyunit.Mode;

public interface LunaticJsonRawDataApiPort {
    void saveData(String campaignName, String interrogationId, String idUE, String questionnaireId, Mode mode, String jsonData) throws JsonProcessingException;
}
