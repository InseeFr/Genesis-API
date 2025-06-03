package fr.insee.genesis.domain.model.context.schedule;

import fr.insee.genesis.Constants;
import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public enum ServiceToCall {
    MAIN(Constants.KRAFTWERK_MAIN_ENDPOINT)
    ,LUNATIC_ONLY("/lunatic-only")
    ,GENESIS("/genesis")
    ,FILE_BY_FILE("/file-by-file");

    private final String pathToService;
}
