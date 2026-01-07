package fr.insee.genesis.infrastructure.document.lunaticmodel;

import lombok.Builder;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Map;

/* It is not possible to respect these rules in a record (compilation error).
I choose to keep the record here (this choice can be challenged)*/
@SuppressWarnings({
        "java:S1123", // deprecated annotation usage
        "java:S6355"  // Add 'since' and/or 'forRemoval' arguments to the @Deprecated annotation
})
@Builder
@Document(collection = "lunaticmodels")
public record LunaticModelDocument (
    @Deprecated (since = "2026-01-01", forRemoval = true)
    @Indexed
    String questionnaireId,
    @Indexed
    String collectionInstrumentId,
    Map<String,Object> lunaticModel,
    LocalDateTime recordDate
){}
