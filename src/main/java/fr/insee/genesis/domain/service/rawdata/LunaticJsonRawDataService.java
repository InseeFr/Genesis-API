package fr.insee.genesis.domain.service.rawdata;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonPersistancePort;
import fr.insee.genesis.domain.utils.LoopIdentifier;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonDataDocument;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class LunaticJsonRawDataService implements LunaticJsonRawDataApiPort {
    @Qualifier("lunaticJsonMongoAdapter")
    private final LunaticJsonPersistancePort lunaticJsonPersistancePort;

    @Autowired
    public LunaticJsonRawDataService(LunaticJsonPersistancePort lunaticJsonPersistancePort) {
        this.lunaticJsonPersistancePort = lunaticJsonPersistancePort;
    }

    @Override
    public void saveData(String campaignName, String idUE, String dataJson, Mode mode) throws JsonParseException {
        if(!isJsonValid(dataJson)){
            throw new JsonParseException("Invalid JSON synthax");
        }
        LunaticJsonDataModel lunaticJsonDataModel = LunaticJsonDataModel.builder()
                .campaignId(campaignName)
                .idUE(idUE)
                .mode(mode)
                .dataJson(dataJson)
                .recordDate(LocalDateTime.now())
                .build();

        lunaticJsonPersistancePort.save(lunaticJsonDataModel);
    }

    @Override
    public List<LunaticJsonRawDataUnprocessedDto> getUnprocessedDataIds() {
        List<LunaticJsonRawDataUnprocessedDto> dtos = new ArrayList<>();

        for(LunaticJsonDataModel dataModel : lunaticJsonPersistancePort.getAllUnprocessedData()){
            dtos.add(LunaticJsonRawDataUnprocessedDto.builder()
                    .campaignId(dataModel.campaignId())
                    .idUE(dataModel.idUE())
                    .build()
            );
        }

        return dtos;
    }

    @Override
    public List<SurveyUnitModel> parseRawData(
            String campaignName,
            Mode mode,
            List<String> idUEList,
            VariablesMap variablesMap
    ) {
        //Get concerned raw data
        List<LunaticJsonDataDocument> rawDataList = lunaticJsonPersistancePort.findRawData(campaignName, mode, idUEList);

        //Convert to genesis model
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
        for(LunaticJsonDataDocument rawData : rawDataList){
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                            .idCampaign(campaignName)
                            .idQuest(campaignName)
                            .mode(rawData.getMode())
                            .idUE(rawData.getIdUE())
                            .state(DataState.COLLECTED)
                            .fileDate(rawData.getRecordDate())
                            .recordDate(LocalDateTime.now())
                            .collectedVariables(new ArrayList<>())
                    .build();

            //Data variables convertion
            for(String variableName : rawData.getData().keySet()){
                CollectedVariable collectedVariable = CollectedVariable.collectedVariableBuilder()
                        .idVar(variableName)
                        .values(new ArrayList<>())
                        .idLoop(variablesMap.getVariable(variableName).getGroupName())
                        .idParent(LoopIdentifier.getRelatedVariableName(variableName, variablesMap))
                        .build();

                collectedVariable.getValues().add(rawData.getData().get(variableName).toString());

                surveyUnitModel.getCollectedVariables().add(collectedVariable);
            }
            surveyUnitModels.add(surveyUnitModel);
        }

        return surveyUnitModels;
    }

    @Override
    public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {
        Set<String> campaignIds = new HashSet<>();
        for(SurveyUnitModel surveyUnitModel : surveyUnitModels){
            campaignIds.add(surveyUnitModel.getIdCampaign());
        }

        for(String campaignId : campaignIds){
            Set<String> idUEs = new HashSet<>();
            for(SurveyUnitModel surveyUnitModel :
                    surveyUnitModels.stream().filter(
                            surveyUnitModel -> surveyUnitModel.getIdCampaign().equals(campaignId)
                            ).toList()){
                idUEs.add(surveyUnitModel.getIdUE());
            }
            lunaticJsonPersistancePort.updateProcessDates(campaignId, idUEs);
        }
    }


    private boolean isJsonValid(String json) {
        ObjectMapper mapper = new ObjectMapper()
                .enable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS);
        try {
            mapper.readTree(json);
        } catch (JacksonException e) {
            return false;
        }
        return true;
    }
}
