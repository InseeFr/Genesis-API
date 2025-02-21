package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;

import java.util.List;
import java.util.Set;

public interface LunaticJsonRawDataPersistancePort {
    void save(LunaticJsonRawDataModel lunaticJsonRawDataModel);
    List<LunaticJsonRawDataModel> getAllUnprocessedData();

    List<LunaticJsonDataDocument> findRawData(String campaignName, Mode mode, List<String> interrogationIdList);

    void updateProcessDates(String campaignId, Set<String> interrogationIds);
}
