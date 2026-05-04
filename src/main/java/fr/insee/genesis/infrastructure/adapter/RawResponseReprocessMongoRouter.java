package fr.insee.genesis.infrastructure.adapter;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistenceRouter;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class RawResponseReprocessMongoRouter implements RawResponseReprocessPersistenceRouter {

    private final RawResponseReprocessMongoAdapter rawResponseReprocessMongoAdapter;
    private final LunaticJsonReprocessMongoAdapter lunaticJsonReprocessMongoAdapter;

    @Override
    public RawResponseReprocessPersistencePort resolve(RawDataModelType rawDataModelType) {
        return switch (rawDataModelType) {
            case FILIERE -> rawResponseReprocessMongoAdapter;
            case LEGACY ->  lunaticJsonReprocessMongoAdapter;
        };
    }

}
