package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.parser.rawdata.LunaticJsonRawDataPayloadParser;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class LunaticJsonRawDataConverter extends RawDataConverter {

    private final LunaticJsonRawDataPayloadParser payloadParser;

    public LunaticJsonRawDataConverter(SurveyUnitApiPort surveyUnitApiPort,
                                       LunaticJsonRawDataPayloadParser lunaticJsonRawDataPayloadParser) {
        super(surveyUnitApiPort);
        this.payloadParser = lunaticJsonRawDataPayloadParser;
    }

    public List<SurveyUnitModel> convertRawData(
            String questionnaireId,
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap
    ) {
        return convertRawDataAndCollectEmptyModels(
                questionnaireId,
                rawDataList,
                variablesMap,
                new ArrayList<>()
        );
    }

    public List<SurveyUnitModel> convertRawDataAndCollectEmptyModels(
            String questionnaireId,
            List<LunaticJsonRawDataModel> rawDataList,
            VariablesMap variablesMap,
            List<SurveyUnitModel> emptySurveyUnitModels
    ) {
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        Map<String, Map<DataState, SurveyUnitModel>> lastSurveyUnitModelsByInterrogationIdAndState =
                getLastSurveyUnitModels(
                        questionnaireId,
                        rawDataList.stream().map(LunaticJsonRawDataModel::interrogationId).collect(Collectors.toList())
                );

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (LunaticJsonRawDataModel rawData : rawDataList) {
                RawDataModelType rawDataModelType = getRawDataModelType(rawData);
                SurveyUnitModel lastSurveyUnitModelForDataState = null;
                if(lastSurveyUnitModelsByInterrogationIdAndState.containsKey(rawData.interrogationId())){
                    lastSurveyUnitModelForDataState = lastSurveyUnitModelsByInterrogationIdAndState
                            .get(rawData.interrogationId())
                            .get(dataState);
                }

                SurveyUnitModel newSurveyUnitModel = SurveyUnitModel.builder()
                        .collectionInstrumentId(rawData.questionnaireId())
                        .mode(rawData.mode())
                        .interrogationId(rawData.interrogationId())
                        .usualSurveyUnitId(rawData.idUE())
                        .validationDate(payloadParser.getValidationDate(rawData))
                        .isCapturedIndirectly(payloadParser.getIsCapturedIndirectly(rawData))
                        .state(dataState)
                        .rawRecordDate(rawData.recordDate())
                        .recordDate(Instant.now())
                        .collectedVariables(new ArrayList<>())
                        .externalVariables(new ArrayList<>())
                        .build();

                convertCollectedVariables(
                        rawData.data(),
                        rawData.interrogationId(),
                        lastSurveyUnitModelForDataState,
                        newSurveyUnitModel,
                        dataState,
                        rawDataModelType,
                        variablesMap
                );

                if (dataState == DataState.COLLECTED) {
                    convertExternalVariables(
                            rawData.data(),
                            lastSurveyUnitModelForDataState,
                            newSurveyUnitModel,
                            rawDataModelType,
                            variablesMap
                    );
                }

                boolean hasNoVariable = newSurveyUnitModel.getCollectedVariables().isEmpty()
                        && newSurveyUnitModel.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (newSurveyUnitModel.getState() == DataState.COLLECTED) {
                        log.warn("No collected or external variable for interrogation {}, raw data is ignored.",
                                rawData.interrogationId());
                    }
                    emptySurveyUnitModels.add(newSurveyUnitModel);
                    continue;
                }

                surveyUnitModels.add(newSurveyUnitModel);
            }
        }

        return surveyUnitModels;
    }

    private static RawDataModelType getRawDataModelType(LunaticJsonRawDataModel rawData) {
        return rawData.data().containsKey("data")
                ? RawDataModelType.FILIERE
                : RawDataModelType.LEGACY;
    }
}
