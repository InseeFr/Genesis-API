package fr.insee.genesis.domain.ports.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;

import java.util.List;

public interface LunaticJsonRawDataApiPort {
    void saveData(String campaignName,String idUE, String dataJson, Mode mode) throws JsonProcessingException;
    List<LunaticJsonRawDataUnprocessedDto> getUnprocessedData();
}
