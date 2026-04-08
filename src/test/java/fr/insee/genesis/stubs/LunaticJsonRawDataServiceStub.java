package fr.insee.genesis.stubs;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class LunaticJsonRawDataServiceStub implements LunaticJsonRawDataApiPort {
    @Override
    public void save(LunaticJsonRawDataModel rawData) {
        // stub, this method unused in tests yet.
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByQuestionnaireId(String questionnaireId, Mode mode, List<String> interrogationIdList) {
        return List.of();
    }

    @Override
    public List<SurveyUnitModel> convertRawData(List<LunaticJsonRawDataModel> rawData, VariablesMap variablesMap) {
        return List.of();
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        return List.of();
    }

    @Override
    public Set<String> getUnprocessedDataQuestionnaireIds() {
        return Set.of();
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        // stub, this method unused in tests yet.
    }

    @Override
    public Set<String> findDistinctQuestionnaireIds() {
        return Set.of();
    }

    @Override
    public long countRawResponsesByQuestionnaireId(String campaignId) {
        return 0;
    }

    @Override
    public long countDistinctInterrogationIdsByQuestionnaireId(String questionnaireId) {
        return 0;
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByCampaignIdAndDate(String campaignId, Instant startDt, Instant endDt, Pageable pageable) {
        return null;
    }

    @Override
    public List<LunaticJsonRawDataModel> getRawDataByInterrogationId(String interrogationId) {
        return List.of();
    }

    @Override
    public DataProcessResult processRawData(String campaignName, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException {
        return null;
    }

    @Override
    public DataProcessResult processRawData(String collectionInstrumentId) throws GenesisException {
        return null;
    }

    @Override
    public Map<String, List<String>> findProcessedIdsgroupedByQuestionnaireSince(LocalDateTime since) {
        return Map.of();
    }

    @Override
    public Page<LunaticJsonRawDataModel> findRawDataByQuestionnaireId(String questionnaireId, Pageable pageable) {
        return null;
    }

    @Override
    public boolean existsByInterrogationId(String interrogationId) {
        return false;
    }
}
