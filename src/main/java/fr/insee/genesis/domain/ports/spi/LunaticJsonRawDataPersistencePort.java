package fr.insee.genesis.domain.ports.spi;

import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

public interface LunaticJsonRawDataPersistencePort {

    void save(LunaticJsonRawDataModel rawData);
    List<LunaticJsonRawDataModel> findRawData(String campaignName, Mode mode, List<String> interrogationIdList);
    List<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(String questionnaireId, Mode mode, List<String> interrogationIdList);
    Page<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(String questionnaireId, Pageable pageable);
    List<LunaticJsonRawDataModel> findRawDataByInterrogationID(String interrogationId);
    List<LunaticJsonRawDataModel> getAllUnprocessedData();
    void updateProcessDates(String campaignId, Set<String> interrogationIds);
    Set<String> findDistinctQuestionnaireIds();
    Set<String> findDistinctQuestionnaireIdsByNullProcessDate();
    Set<Mode> findModesByQuestionnaire(String questionnaireId);
    Page<LunaticJsonRawDataModel> findByCampaignIdAndDate(String campaignId, Instant startDt, Instant endDt, Pageable pageable);
    long countRawResponsesByQuestionnaireId(String questionnaireId);
    List<GroupedInterrogation> findProcessedIdsGroupedByQuestionnaireSince(LocalDateTime since);
    List<GroupedInterrogation> findUnprocessedIds();
    Set<String> findUnprocessedInterrogationIdsByCollectionInstrumentId(String collectionInstrumentId);
}
