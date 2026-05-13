package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.parser.rawdata.LunaticJsonRawDataPayloadParser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LunaticJsonRawDataConverterTest {

    public static final LocalDateTime DATE_TIME = LocalDateTime.parse("2025-01-01T10:00:00");
    @Mock
    private LunaticJsonRawDataPayloadParser payloadParser;

    @Test
    void shouldReturnNoSurveyUnitWhenRawDataListIsEmpty() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);
        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                List.of(),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).isEmpty();
        assertThat(emptySurveyUnitModels).isEmpty();
    }

    @Test
    void shouldCreateCollectedAndEditedSurveyUnitsFromLegacyRawData() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(
                        "FIRST_NAME", Map.of("COLLECTED", "Alice", "EDITED", "Alicia")
                )
        ));

        try (MockedStatic<RawResponseConverter> rawResponseConverter = mockStatic(RawResponseConverter.class)) {
            rawResponseConverter
                    .when(() -> RawResponseConverter.processCollectedVariable(any(), any(), any(), any(), any()))
                    .thenAnswer(invocation -> {
                        String state = invocation.getArgument(1);
                        List<VariableModel> destination = invocation.getArgument(4);

                        destination.add(VariableModel.builder()
                                .varId("FIRST_NAME")
                                .value(state)
                                .build());

                        return null;
                    });

            List<SurveyUnitModel> result = converter.convertRawData(
                    List.of(rawData),
                    variablesMap
            );

            assertThat(result).hasSize(2);

            assertThat(result)
                    .extracting(SurveyUnitModel::getState)
                    .containsExactly(DataState.COLLECTED, DataState.EDITED);

            assertThat(result.get(0).getCollectedVariables())
                    .extracting(VariableModel::value)
                    .containsExactly("COLLECTED");

            assertThat(result.get(1).getCollectedVariables())
                    .extracting( VariableModel::value)
                    .containsExactly("EDITED");
        }
    }

    @Test
    void shouldConvertExternalVariablesOnlyForCollectedSurveyUnit() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class, RETURNS_DEEP_STUBS);

        when(variablesMap.getVariable("COUNTRY").getGroupName()).thenReturn("ROOT");
        when(variablesMap.getVariable("CHILDREN").getGroupName()).thenReturn("HOUSEHOLD");

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(),
                "EXTERNAL", Map.of(
                        "COUNTRY", "FR",
                        "CHILDREN", List.of("Anna", "", "Paul")
                )
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                List.of(rawData),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).hasSize(1);

        SurveyUnitModel collectedSurveyUnit = result.getFirst();

        assertThat(collectedSurveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        assertThat(collectedSurveyUnit.getCollectedVariables()).isEmpty();

        assertThat(collectedSurveyUnit.getExternalVariables())
                .extracting(VariableModel::varId, VariableModel::value, VariableModel::iteration)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple("CHILDREN", "Anna", 1),
                        org.assertj.core.groups.Tuple.tuple("CHILDREN", "Paul", 3),
                        org.assertj.core.groups.Tuple.tuple("COUNTRY", "FR", 1)
                );

        assertThat(emptySurveyUnitModels)
                .hasSize(1)
                .first()
                .extracting(SurveyUnitModel::getState)
                .isEqualTo(DataState.EDITED);
    }

    @Test
    void shouldCollectEmptySurveyUnitsWhenNoCollectedNorExternalVariablesExist() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(),
                "EXTERNAL", Map.of()
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                List.of(rawData),
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
    void shouldReadFiliereRawDataFromNestedDataProperty() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class, RETURNS_DEEP_STUBS);

        when(variablesMap.getVariable("COUNTRY").getGroupName()).thenReturn("ROOT");

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "data", Map.of(
                        "COLLECTED", Map.of(),
                        "EXTERNAL", Map.of("COUNTRY", "FR")
                )
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                List.of(rawData),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().getExternalVariables())
                .extracting(VariableModel::varId, VariableModel::value)
                .containsExactly(org.assertj.core.groups.Tuple.tuple("COUNTRY", "FR"));
    }

    @Test
    void shouldAssignRootScopeWhenExternalVariableIsMissingFromMetadata() {
        LunaticJsonRawDataConverter converter = new LunaticJsonRawDataConverter(payloadParser);
        VariablesMap variablesMap = mock(VariablesMap.class);

        when(variablesMap.getVariable("UNKNOWN_VARIABLE")).thenReturn(null);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(),
                "EXTERNAL", Map.of("UNKNOWN_VARIABLE", "value")
        ));

        List<SurveyUnitModel> result = converter.convertRawData(
                List.of(rawData),
                variablesMap
        );

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().getExternalVariables())
                .singleElement()
                .extracting(VariableModel::scope)
                .isEqualTo(Constants.ROOT_GROUP_NAME);
    }

    private LunaticJsonRawDataModel rawData(Map<String, Object> data) {
        LunaticJsonRawDataModel rawData = mock(LunaticJsonRawDataModel.class);

        when(rawData.questionnaireId()).thenReturn("questionnaire-id");
        when(rawData.mode()).thenReturn(Mode.valueOf("WEB"));
        when(rawData.interrogationId()).thenReturn("interrogation-id");
        when(rawData.idUE()).thenReturn("survey-unit-id");
        when(rawData.recordDate()).thenReturn(DATE_TIME);
        when(rawData.data()).thenReturn(data);

        when(payloadParser.getValidationDate(rawData)).thenReturn(DATE_TIME);
        when(payloadParser.getIsCapturedIndirectly(rawData)).thenReturn(false);

        return rawData;
    }
}