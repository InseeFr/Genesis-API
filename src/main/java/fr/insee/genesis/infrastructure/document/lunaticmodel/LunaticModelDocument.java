package fr.insee.genesis.infrastructure.document.lunaticmodel;

import lombok.Builder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

@Builder
@Document(collection = "lunaticmodels")
public record LunaticModelDocument (
    @Indexed
    String questionnaireId,
    Map<String,Object> lunaticModel,
    LocalDateTime recordDate
){}
