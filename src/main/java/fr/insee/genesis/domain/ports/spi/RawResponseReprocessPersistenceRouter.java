package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;

public interface RawResponseReprocessPersistenceRouter {

    RawResponseReprocessPersistencePort resolve(RawDataModelType rawDataModelType);

}
