package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticXmlDataModel;

import java.util.List;

public interface LunaticXmlPersistancePort {
    void save(LunaticXmlDataModel lunaticXmlDataModels);
}
