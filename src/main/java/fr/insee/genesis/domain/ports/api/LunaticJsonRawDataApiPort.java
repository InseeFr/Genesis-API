package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public interface LunaticJsonRawDataApiPort {

    void save(LunaticJsonRawDataModel rawData) throws GenesisException;
    List<LunaticJsonRawDataModel> getRawDataByQuestionnaireId(String questionnaireId, Mode mode, List<String> interrogationIdList);
    List<SurveyUnitModel> convertRawData(List<LunaticJsonRawDataModel> rawData, VariablesMap variablesMap);

    List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds();
    Set<String> getUnprocessedDataQuestionnaireIds();
    void updateProcessDates(List<SurveyUnitModel> surveyUnitModels);
    Set<String> findDistinctQuestionnaireIds();
    long countRawResponsesByQuestionnaireId(String questionnaireId);
    long countDistinctInterrogationIdsByQuestionnaireId(String questionnaireId);
    Page<LunaticJsonRawDataModel> findRawDataByCampaignIdAndDate(String campaignId, Instant startDt, Instant endDt, Pageable pageable);

    List<LunaticJsonRawDataModel> getRawDataByInterrogationId(String interrogationId);

    DataProcessResult processRawData(String collectionInstrumentId) throws GenesisException;

    Map<String, List<String>> findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime since);

    Page<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(String questionnaireId, Pageable pageable);

    DataProcessResult processRawDataByInterrogationIds(String campaignName, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException;

    boolean existsByInterrogationId(String interrogationId);
}
