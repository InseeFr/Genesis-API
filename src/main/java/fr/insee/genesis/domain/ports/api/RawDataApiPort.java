package fr.insee.genesis.domain.ports.api;

import fr.insee.genesis.controller.sources.xml.LunaticXmlCampaign;
import fr.insee.genesis.domain.model.surveyunit.Mode;

public interface RawDataApiPort {
    void saveData(LunaticXmlCampaign data, Mode mode);
}