package fr.insee.genesis.infrastructure.model.document.schedule;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceToCall {
    MAIN("")
    ,LUNATIC_ONLY("/lunatic-only")
    ,GENESIS("/genesis")
    ,FILE_BY_FILE("/file-by-file");

    private final String pathToService;
}
