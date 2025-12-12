package fr.insee.genesis.domain.model.lunaticmodel;

import lombok.Builder;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
public record LunaticModelModel (
    String collectionInstrumentId,
    Map<String,Object> lunaticModel,
    LocalDateTime recordDate
){}
