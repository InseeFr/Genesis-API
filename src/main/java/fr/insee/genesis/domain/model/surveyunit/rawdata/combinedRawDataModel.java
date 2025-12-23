package fr.insee.genesis.domain.model.surveyunit.rawdata;

import java.util.List;

public record combinedRawDataModel(List<RawResponseModel> rawResponseModels,
                                   List<LunaticJsonRawDataModel> lunaticRawDataModels) {
}
