package fr.insee.genesis.domain.model.surveyunit.rawdata;


import fr.insee.genesis.domain.model.surveyunit.DataState;
import lombok.Builder;

import java.util.Map;

@Builder
public record LunaticJsonRawDataCollectedVariable(
        Map<DataState, LunaticJsonRawDataVariable> collectedVariableByStateMap
){}
