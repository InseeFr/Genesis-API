package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;

import java.util.List;
import java.util.Set;

public interface LunaticJsonPersistancePort {
    void save(LunaticJsonDataModel lunaticJsonDataModel);
    List<LunaticJsonDataModel> getAllUnprocessedData();

    List<LunaticJsonDataDocument> findRawData(String campaignName, Mode mode, List<String> idUEList);

    void updateProcessDates(String campaignId, Set<String> idUEs);
}
