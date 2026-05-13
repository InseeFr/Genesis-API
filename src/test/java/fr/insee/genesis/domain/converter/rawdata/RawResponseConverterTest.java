package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.parser.rawdata.RawResponsePayloadParser;
import fr.insee.modelefiliere.RawResponseDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class RawResponseConverterTest {

    @Mock
    private RawResponsePayloadParser rawResponsePayloadParser;

    @Test
    void shouldReturnNoSurveyUnitWhenRawResponseListIsEmpty() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);
        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawResponseAndCollectEmptyModels(
                List.of(),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).isEmpty();
        assertThat(emptySurveyUnitModels).isEmpty();
    }

    @Test
    void shouldCreateCollectedAndEditedSurveyUnitsFromCollectedPayloadStates() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class, RETURNS_DEEP_STUBS);

        when(variablesMap.getVariable("FIRST_NAME").getGroupName()).thenReturn(Constants.ROOT_GROUP_NAME);

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                collectedVariables(
                        Map.of(
                                "COLLECTED", "Alice",
                                "EDITED", "Alicia"
                        )
                ),
                Map.of()
        ));

        List<SurveyUnitModel> result = converter.convertRawResponse(
                List.of(rawResponseModel),
                variablesMap
        );

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(SurveyUnitModel::getState)
                .containsExactly(DataState.COLLECTED, DataState.EDITED);

        assertThat(result.get(0).getCollectedVariables())
                .extracting("varId", "value", "iteration", "scope")
                .containsExactly(tuple("FIRST_NAME", "Alice", 1, Constants.ROOT_GROUP_NAME));

        assertThat(result.get(1).getCollectedVariables())
                .extracting("varId", "value", "iteration", "scope")
                .containsExactly(tuple("FIRST_NAME", "Alicia", 1, Constants.ROOT_GROUP_NAME));
    }

    @Test
    void shouldConvertExternalVariablesOnlyForCollectedSurveyUnit() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class, RETURNS_DEEP_STUBS);

        when(variablesMap.getVariable("COUNTRY").getGroupName()).thenReturn(Constants.ROOT_GROUP_NAME);
        when(variablesMap.getVariable("CHILDREN").getGroupName()).thenReturn("HOUSEHOLD");

        Map<String, Object> externalVariables = new LinkedHashMap<>();
        externalVariables.put("COUNTRY", "FR");
        externalVariables.put("CHILDREN", List.of("Anna", "", "Paul"));

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(),
                externalVariables
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawResponseAndCollectEmptyModels(
                List.of(rawResponseModel),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).hasSize(1);

        SurveyUnitModel collectedSurveyUnit = result.getFirst();

        assertThat(collectedSurveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        assertThat(collectedSurveyUnit.getCollectedVariables()).isEmpty();

        assertThat(collectedSurveyUnit.getExternalVariables())
                .extracting("varId", "value", "iteration", "scope")
                .containsExactly(
                        tuple("COUNTRY", "FR", 1, Constants.ROOT_GROUP_NAME),
                        tuple("CHILDREN", "Anna", 1, "HOUSEHOLD"),
                        tuple("CHILDREN", "Paul", 3, "HOUSEHOLD")
                );

        assertThat(emptySurveyUnitModels)
                .hasSize(1)
                .extracting(SurveyUnitModel::getState)
                .containsExactly(DataState.EDITED);
    }

    @Test
    void shouldCollectEmptySurveyUnitsWhenNoVariableExists() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(),
                Map.of()
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawResponseAndCollectEmptyModels(
                List.of(rawResponseModel),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).isEmpty();

        assertThat(emptySurveyUnitModels)
                .hasSize(2)
                .extracting(SurveyUnitModel::getState)
                .containsExactly(DataState.COLLECTED, DataState.EDITED);
    }

    @Test
    void shouldAssignRootScopeWhenVariableIsMissingFromMetadata() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        when(variablesMap.getVariable("UNKNOWN_VARIABLE")).thenReturn(null);

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(),
                Map.of("UNKNOWN_VARIABLE", "value")
        ));

        List<SurveyUnitModel> result = converter.convertRawResponse(
                List.of(rawResponseModel),
                variablesMap
        );

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().getExternalVariables())
                .singleElement()
                .extracting("scope")
                .isEqualTo(Constants.ROOT_GROUP_NAME);
    }

    @Test
    void shouldSetQuestionnaireStateWhenPayloadContainsValidState() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(),
                Map.of()
        ));

        when(rawResponsePayloadParser.getStringField(rawResponseModel, "questionnaireState"))
                .thenReturn(RawResponseDto.QuestionnaireStateEnum.FINISHED.name());

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        converter.convertRawResponseAndCollectEmptyModels(
                List.of(rawResponseModel),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(emptySurveyUnitModels)
                .extracting(SurveyUnitModel::getQuestionnaireState)
                .containsOnly(RawResponseDto.QuestionnaireStateEnum.FINISHED);
    }

    @Test
    void shouldKeepQuestionnaireStateNullWhenPayloadContainsInvalidState() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(),
                Map.of()
        ));

        when(rawResponsePayloadParser.getStringField(rawResponseModel, "questionnaireState"))
                .thenReturn("INVALID_STATE");

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        converter.convertRawResponseAndCollectEmptyModels(
                List.of(rawResponseModel),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(emptySurveyUnitModels)
                .extracting(SurveyUnitModel::getQuestionnaireState)
                .containsOnlyNulls();
    }

    @Test
    void shouldConvertPairwiseCollectedVariables() {
        RawResponseConverter converter = new RawResponseConverter(rawResponsePayloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class, RETURNS_DEEP_STUBS);

        when(variablesMap.hasVariable(Constants.PAIRWISE_PREFIX + 1)).thenReturn(true);
        when(variablesMap.getVariable(Constants.PAIRWISE_PREFIX + 1).getGroupName()).thenReturn("PAIRWISE_GROUP");

        List<List<String>> pairwiseValues = List.of(
                List.of("1", "", "3"),
                List.of()
        );

        RawResponseModel rawResponseModel = rawResponseModel(payloadWith(
                Map.of(Constants.PAIRWISES, Map.of("COLLECTED", pairwiseValues)),
                Map.of()
        ));

        List<SurveyUnitModel> result = converter.convertRawResponse(
                List.of(rawResponseModel),
                variablesMap
        );

        SurveyUnitModel collectedSurveyUnit = result.getFirst();

        assertThat(collectedSurveyUnit.getCollectedVariables())
                .hasSize(2 * (Constants.MAX_LINKS_ALLOWED - 1));

        assertThat(collectedSurveyUnit.getCollectedVariables())
                .extracting("varId", "value", "iteration", "scope", "parentId")
                .contains(
                        tuple(Constants.PAIRWISE_PREFIX + 1, "1", 1, "PAIRWISE_GROUP", Constants.ROOT_GROUP_NAME),
                        tuple(Constants.PAIRWISE_PREFIX + 2, Constants.SAME_AXIS_VALUE, 1, "PAIRWISE_GROUP", Constants.ROOT_GROUP_NAME),
                        tuple(Constants.PAIRWISE_PREFIX + 3, "3", 1, "PAIRWISE_GROUP", Constants.ROOT_GROUP_NAME),
                        tuple(Constants.PAIRWISE_PREFIX + 1, Constants.NO_PAIRWISE_VALUE, 2, "PAIRWISE_GROUP", Constants.ROOT_GROUP_NAME)
                );
    }

    private RawResponseModel rawResponseModel(Map<String, Object> payload) {
        RawResponseModel rawResponseModel = mock(RawResponseModel.class);

        when(rawResponseModel.collectionInstrumentId()).thenReturn("collection-instrument-id");
        when(rawResponseModel.mode()).thenReturn(Mode.valueOf("WEB"));
        when(rawResponseModel.interrogationId()).thenReturn("interrogation-id");
        when(rawResponseModel.recordDate()).thenReturn(LocalDateTime.parse("2025-01-01T10:00:00"));
        when(rawResponseModel.payload()).thenReturn(payload);

        when(rawResponsePayloadParser.getStringField(rawResponseModel, "majorModelVersion"))
                .thenReturn("1");
        when(rawResponsePayloadParser.getStringField(rawResponseModel, "usualSurveyUnitId"))
                .thenReturn("survey-unit-id");
        when(rawResponsePayloadParser.getStringField(rawResponseModel, "questionnaireState"))
                .thenReturn(null);
        when(rawResponsePayloadParser.getValidationDate(rawResponseModel))
                .thenReturn(LocalDateTime.parse("2025-01-02T10:00:00"));
        when(rawResponsePayloadParser.getIsCapturedIndirectly(rawResponseModel))
                .thenReturn(false);

        return rawResponseModel;
    }

    private static Map<String, Object> payloadWith(
            Map<String, Object> collectedVariables,
            Map<String, Object> externalVariables
    ) {
        return Map.of(
                "data", Map.of(
                        "COLLECTED", collectedVariables,
                        "EXTERNAL", externalVariables
                )
        );
    }

    private static Map<String, Object> collectedVariables(
            Map<String, Object> states
    ) {
        return Map.of("FIRST_NAME", states);
    }
}