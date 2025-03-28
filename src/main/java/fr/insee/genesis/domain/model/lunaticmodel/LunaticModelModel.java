package fr.insee.genesis.domain.model.lunaticmodel;

import lombok.Builder;

import java.util.Map;

@Builder
public record LunaticModelModel (
    String questionnaireId,
    Map<String,Object> lunaticModel
){}
