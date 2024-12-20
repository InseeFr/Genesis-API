package fr.insee.genesis.domain.ports.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;

import java.util.List;

public interface LunaticJsonRawDataApiPort {
    void saveData(String campaignName,String idUE, String dataJson, Mode mode) throws JsonProcessingException;
    List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds();

    List<SurveyUnitModel> parseRawData(
            String campaignName,
            Mode mode,
            List<String> idUEList,
            VariablesMap variablesMap
    );

    void updateProcessDates(List<SurveyUnitModel> surveyUnitModels);
}
