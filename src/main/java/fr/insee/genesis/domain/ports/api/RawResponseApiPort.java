package fr.insee.genesis.domain.ports.api;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.Instant;
import java.util.List;
import java.util.Set;

public interface RawResponseApiPort {

    List<RawResponseModel> getRawResponses(String collectionInstrumentId, Mode mode, List<String> interrogationIdList);
    List<RawResponseModel> getRawResponsesByInterrogationID(String interrogationId);
    DataProcessResult processRawResponses(String collectionInstrumentId, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException;
    DataProcessResult processRawResponses(String collectionInstrumentId) throws GenesisException;
    List<SurveyUnitModel> convertRawResponse(List<RawResponseModel> rawResponses, VariablesMap variablesMap);
    List<String> getUnprocessedCollectionInstrumentIds();
    void updateProcessDates(List<SurveyUnitModel> surveyUnitModels);
    Page<RawResponseModel> findRawResponseDataByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable);

    long countByCollectionInstrumentId(String collectionInstrumentId);

    Set<String> getDistinctCollectionInstrumentIds();
    Page<RawResponseModel> findRawResponseDataByCollectionInstrumentId(String collectionInstrumentId, Pageable pageable);

}
