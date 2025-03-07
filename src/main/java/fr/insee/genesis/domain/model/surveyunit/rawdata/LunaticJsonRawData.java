package fr.insee.genesis.domain.model.surveyunit.rawdata;

import lombok.Builder;

import java.util.Map;

@Builder
public record LunaticJsonRawData (
    Map<String, LunaticJsonRawDataCollectedVariable> collectedVariables,
    Map<String, LunaticJsonRawDataVariable> externalVariables
){}