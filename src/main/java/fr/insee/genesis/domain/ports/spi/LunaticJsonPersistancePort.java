package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;

public interface LunaticJsonPersistancePort {
    void save(LunaticJsonDataModel lunaticJsonDataModel);
}
