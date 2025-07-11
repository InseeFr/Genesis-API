package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface LunaticJsonRawDataPersistencePort {

    void save(LunaticJsonRawDataModel rawData);
    List<LunaticJsonRawDataModel> findRawData(String campaignName, Mode mode, List<String> interrogationIdList);
    List<LunaticJsonRawDataModel> getAllUnprocessedData();
    void updateProcessDates(String campaignId, Set<String> interrogationIds);
    Set<String> findDistinctQuestionnaireIds();
    Page<LunaticJsonRawDataModel> findByCampaignIdAndDate(String campaignId, Instant startDt, Instant endDt, Pageable pageable);
    long countResponsesByQuestionnaireId(String questionnaireId);
}
