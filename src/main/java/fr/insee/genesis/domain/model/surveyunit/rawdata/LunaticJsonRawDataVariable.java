package fr.insee.genesis.domain.model.surveyunit.rawdata;


import lombok.Builder;

import java.util.List;

@Builder
public record LunaticJsonRawDataVariable(
    List<String> valuesArray,
    String value
){}
