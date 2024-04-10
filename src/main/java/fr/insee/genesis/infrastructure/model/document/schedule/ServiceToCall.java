package fr.insee.genesis.infrastructure.model.document.schedule;

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

    public static String getPathToService(ServiceToCall serviceToCall){
        return serviceToCall.getPathToService();
    }
}
