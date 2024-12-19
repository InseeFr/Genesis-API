package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;

import java.util.List;

public interface LunaticJsonPersistancePort {
    void save(LunaticJsonDataModel lunaticJsonDataModel);
    List<LunaticJsonDataModel> getAllUnprocessedData();
}
