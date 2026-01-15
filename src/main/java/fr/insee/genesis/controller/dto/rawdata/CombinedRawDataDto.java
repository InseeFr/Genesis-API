package fr.insee.genesis.controller.dto.rawdata;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;

import java.util.List;

public record CombinedRawDataDto(
        List<RawResponseModel> rawResponseModels,
        List<LunaticJsonRawDataModel> lunaticRawDataModels
) {
}

