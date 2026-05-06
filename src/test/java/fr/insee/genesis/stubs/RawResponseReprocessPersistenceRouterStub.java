package fr.insee.genesis.stubs;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistencePort;
import fr.insee.genesis.domain.ports.spi.RawResponseReprocessPersistenceRouter;

public class RawResponseReprocessPersistenceRouterStub implements RawResponseReprocessPersistenceRouter {

    @Override
    public RawResponseReprocessPersistencePort resolve(RawDataModelType rawDataModelType) {
        return new RawResponseReprocessPersistenceStub();
    }

}
