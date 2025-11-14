package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponse;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;

import java.util.List;

public interface RawResponseApiPort {

    List<RawResponse> getRawResponses(String questionnaireModelId, Mode mode, List<String> interrogationIdList);
    DataProcessResult processRawResponses(String questionnaireId, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException;
    List<SurveyUnitModel> convertRawResponse(List<RawResponse> rawResponses, VariablesMap variablesMap);
    void updateProcessDates(List<SurveyUnitModel> surveyUnitModels);
}
