package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.parser.rawdata.RawResponsePayloadParser;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
@Slf4j
public class RawResponseRawDataConverter extends RawDataConverter {

    private final RawResponsePayloadParser rawResponsePayloadParser;

    public RawResponseRawDataConverter(SurveyUnitApiPort surveyUnitApiPort,
                                       RawResponsePayloadParser rawResponsePayloadParser) {
        super(surveyUnitApiPort);
        this.rawResponsePayloadParser = rawResponsePayloadParser;
    }

    /**
     * Like convertRawResponseAndCollectEmptyModels but ignoring the empty raw responses
     */
    public List<SurveyUnitModel> convertRawResponse(
            String collectionInstrumentId,
            List<RawResponseModel> rawResponseModels,
            VariablesMap variablesMap
    ) {
        return convertRawResponseAndCollectEmptyModels(
                collectionInstrumentId,
                rawResponseModels,
                variablesMap,
                new ArrayList<>()
        );
    }

    /**
     * Converts RawResponseModels into SurveyUnitModels
     * @param collectionInstrumentId Collection instrument id of raw responses to convert
     * @param rawResponseModels raw responses to convert
     * @param variablesMap variables map of the collection instrument
     * @param emptySurveyUnitModels A list of survey units that will be filled with empty raw responses
     * @return a list of SurveyUnitModels converted from raw responses
     */
    public List<SurveyUnitModel> convertRawResponseAndCollectEmptyModels(
            String collectionInstrumentId,
            List<RawResponseModel> rawResponseModels,
            VariablesMap variablesMap,
            List<SurveyUnitModel> emptySurveyUnitModels
    ) {
        List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();

        Map<String, Map<DataState, SurveyUnitModel>> lastSurveyUnitModelsByInterrogationIdAndState = getLastSurveyUnitModels(
                collectionInstrumentId,
                rawResponseModels.stream().map(RawResponseModel::interrogationId).collect(Collectors.toList())
        );

        for (DataState dataState : List.of(DataState.COLLECTED, DataState.EDITED)) {
            for (RawResponseModel rawResponseModel : rawResponseModels) {
                SurveyUnitModel surveyUnitModel = buildSurveyUnitModel(rawResponseModel, dataState);
                SurveyUnitModel lastSurveyUnitModelForDataState = null;
                if(lastSurveyUnitModelsByInterrogationIdAndState.containsKey(rawResponseModel.interrogationId())){
                    lastSurveyUnitModelForDataState = lastSurveyUnitModelsByInterrogationIdAndState
                            .get(rawResponseModel.interrogationId())
                            .get(dataState);
                }

                convertCollectedVariables(
                        rawResponseModel.payload(),
                        rawResponseModel.interrogationId(),
                        lastSurveyUnitModelForDataState,
                        surveyUnitModel,
                        dataState,
                        RawDataModelType.FILIERE,
                        variablesMap
                );

                if (dataState == DataState.COLLECTED) {
                    convertExternalVariables(
                            rawResponseModel.payload(),
                            lastSurveyUnitModelForDataState,
                            surveyUnitModel,
                            RawDataModelType.FILIERE,
                            variablesMap
                    );
                }

                boolean hasNoVariable = surveyUnitModel.getCollectedVariables().isEmpty()
                        && surveyUnitModel.getExternalVariables().isEmpty();

                if (hasNoVariable) {
                    if (surveyUnitModel.getState() == DataState.COLLECTED) {
                        log.warn(
                                "No collected or external variable for interrogation {}, raw data is ignored.",
                                rawResponseModel.interrogationId()
                        );
                    }
                    emptySurveyUnitModels.add(surveyUnitModel);
                    continue;
                }

                surveyUnitModels.add(surveyUnitModel);
            }
        }

        return surveyUnitModels;
    }

    private SurveyUnitModel buildSurveyUnitModel(RawResponseModel rawResponseModel, DataState dataState) {
        String questionnaireStateString =
                rawResponsePayloadParser.getStringField(rawResponseModel, "questionnaireState");

        RawResponseDto.QuestionnaireStateEnum questionnaireStateEnum = null;
        try {
            questionnaireStateEnum = RawResponseDto.QuestionnaireStateEnum.valueOf(questionnaireStateString);
        } catch (IllegalArgumentException _) {
            log.warn("'{}' is not a valid questionnaire state according to filiere model", questionnaireStateString);
        } catch (NullPointerException _) {
            //Nothing to do
        }

        return SurveyUnitModel.builder()
                .collectionInstrumentId(rawResponseModel.collectionInstrumentId())
                .majorModelVersion(rawResponsePayloadParser.getStringField(rawResponseModel, "majorModelVersion"))
                .mode(rawResponseModel.mode())
                .interrogationId(rawResponseModel.interrogationId())
                .usualSurveyUnitId(rawResponsePayloadParser.getStringField(rawResponseModel, "usualSurveyUnitId"))
                .questionnaireState(questionnaireStateEnum)
                .validationDate(rawResponsePayloadParser.getValidationDate(rawResponseModel))
                .isCapturedIndirectly(rawResponsePayloadParser.getIsCapturedIndirectly(rawResponseModel))
                .state(dataState)
                .fileDate(rawResponseModel.recordDate())
                .recordDate(Instant.now())
                .collectedVariables(new ArrayList<>())
                .externalVariables(new ArrayList<>())
                .build();
    }
}
