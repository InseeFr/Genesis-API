package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;

import java.util.List;

public interface LunaticJsonRawDataApiPort {

    void save(LunaticJsonRawDataModel rawData);
    List<LunaticJsonRawDataModel> getRawData(String campaignName, Mode mode, List<String> interrogationIdList);
    List<SurveyUnitModel> convertRawData(List<LunaticJsonRawDataModel> rawData, VariablesMap variablesMap);
    List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds();
    List<SurveyUnitModel> parseRawData(
            String campaignName,
            Mode mode,
            List<String> interrogationIdList,
            VariablesMap variablesMap
    );
    void updateProcessDates(List<SurveyUnitModel> surveyUnitModels);
}
