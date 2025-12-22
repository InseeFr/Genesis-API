package fr.insee.genesis.domain.model.surveyunit.rawdata;

import java.util.List;

public record CombinedRawData(List<RawResponse> rawResponses,
                             List<LunaticJsonRawDataModel> lunaticRawData) {
}
