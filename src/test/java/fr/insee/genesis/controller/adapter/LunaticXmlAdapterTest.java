package fr.insee.genesis.controller.adapter;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.sources.xml.LunaticXmlCollectedData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlOtherData;
import fr.insee.genesis.controller.sources.xml.LunaticXmlSurveyUnit;
import fr.insee.genesis.controller.sources.xml.ValueType;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LunaticXmlAdapter tests")
class LunaticXmlAdapterTest {

    private static final String QUESTIONNAIRE_ID = "questionnaire-abc";
    private static final String INTERROGATION_ID = "interrogation-123";
    private static final Mode MODE = Mode.WEB;
    private static final VariablesMap VARIABLES_MAP = new VariablesMap();

    // -------------------------------------------------------------------------
    // convert() — returned list composition
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("convert() — list composition tests")
    class ConvertListCompositionTests {

        @Test
        @DisplayName("Should always include COLLECTED state in the result")
        void convert_shouldAlwaysIncludeCollected() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .contains(DataState.COLLECTED);
        }

        @Test
        @DisplayName("Should return only COLLECTED when no other state has data")
        void convert_noOtherStateData_shouldReturnOnlyCollected() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).hasSize(1);
            assertThat(result.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        }

        @Test
        @DisplayName("Should include EDITED state when it has data")
        void convert_withEditedData_shouldIncludeEdited() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("collected-val")));
            data.setEdited(List.of(getValueType("edited-val")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .contains(DataState.COLLECTED, DataState.EDITED);
        }

        @Test
        @DisplayName("Should include INPUTED state when it has data")
        void convert_withInputedData_shouldIncludeInputed() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val")));
            data.setInputed(List.of(getValueType("inputed-val")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .contains(DataState.INPUTED);
        }

        @Test
        @DisplayName("Should include FORCED state when it has data")
        void convert_withForcedData_shouldIncludeForced() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val")));
            data.setForced(List.of(getValueType("forced-val")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .contains(DataState.FORCED);
        }

        @Test
        @DisplayName("Should include PREVIOUS state when it has data")
        void convert_withPreviousData_shouldIncludePrevious() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val")));
            data.setPrevious(List.of(getValueType("previous-val")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .contains(DataState.PREVIOUS);
        }

        @Test
        @DisplayName("Should include all 5 states when all have data")
        void convert_allStatesHaveData_shouldReturnFiveModels() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("c")));
            data.setEdited(List.of(getValueType("e")));
            data.setInputed(List.of(getValueType("i")));
            data.setForced(List.of(getValueType("f")));
            data.setPrevious(List.of(getValueType("p")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).hasSize(5);
            assertThat(result).extracting(SurveyUnitModel::getState)
                    .containsExactlyInAnyOrder(
                            DataState.COLLECTED, DataState.EDITED,
                            DataState.INPUTED, DataState.FORCED, DataState.PREVIOUS);
        }

        @Test
        @DisplayName("Should not include EDITED when its values are all null")
        void convert_editedAllNullValues_shouldNotIncludeEdited() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val")));
            data.setEdited(List.of(getValueType(null))); // null value — no data

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            List<SurveyUnitModel> result = LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE);

            // THEN
            assertThat(result).extracting(SurveyUnitModel::getState).isNotEmpty()
                    .doesNotContain(DataState.EDITED);
        }
    }

    // -------------------------------------------------------------------------
    // convert() — field mapping
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("convert() — field mapping tests")
    class ConvertFieldMappingTests {

        @Test
        @DisplayName("Should map questionnaireModelId to collectionInstrumentId in uppercase")
        void convert_shouldMapQuestionnaireIdUppercased() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectionInstrumentId()).isEqualTo(QUESTIONNAIRE_ID.toUpperCase());
        }

        @Test
        @DisplayName("Should map interrogation id")
        void convert_shouldMapInterrogationId() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getInterrogationId()).isEqualTo(INTERROGATION_ID);
        }

        @Test
        @DisplayName("Should map mode")
        void convert_shouldMapMode() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getMode()).isEqualTo(MODE);
        }

        @Test
        @DisplayName("Should set a non-null recordDate close to now")
        void convert_shouldSetRecordDateCloseToNow() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));
            LocalDateTime before = LocalDateTime.now();

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            LocalDateTime after = LocalDateTime.now();
            assertThat(collected.getRecordDate())
                    .isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should map fileDate from survey unit")
        void convert_shouldMapFileDate() {
            // GIVEN
            LocalDateTime fileDate = LocalDateTime.of(2024, 1, 15, 10, 0);
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(collectedDataWithValue("VAR1", "val")));
            su.setFileDate(fileDate);

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getFileDate()).isEqualTo(fileDate);
        }
    }

    // -------------------------------------------------------------------------
    // convert() — collected variables
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("convert() — collected variables tests")
    class ConvertCollectedVariablesTests {

        @Test
        @DisplayName("Should populate collectedVariables from COLLECTED data")
        void convert_shouldPopulateCollectedVariables() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(
                    collectedDataWithValue("VAR1", "value1")
            ));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectedVariables()).hasSize(1);
            assertThat(collected.getCollectedVariables().getFirst().varId()).isEqualTo("VAR1");
            assertThat(collected.getCollectedVariables().getFirst().value()).isEqualTo("value1");
        }

        @Test
        @DisplayName("Should set iteration starting at 1 for each value")
        void convert_shouldSetIterationStartingAtOne() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val1"), getValueType("val2")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectedVariables()).hasSize(2);
            assertThat(collected.getCollectedVariables().get(0).iteration()).isEqualTo(1);
            assertThat(collected.getCollectedVariables().get(1).iteration()).isEqualTo(2);
        }

        @Test
        @DisplayName("Should skip collected variables with null value")
        void convert_shouldSkipNullValues() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(List.of(getValueType("val"), getValueType(null)));

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectedVariables()).hasSize(1);
            assertThat(collected.getCollectedVariables().getFirst().value()).isEqualTo("val");
        }

        @Test
        @DisplayName("Should produce empty collectedVariables when collected list is null for a variable")
        void convert_nullCollectedList_shouldProduceEmptyVariables() {
            // GIVEN
            LunaticXmlCollectedData data = new LunaticXmlCollectedData();
            data.setVariableName("VAR1");
            data.setCollected(null); // null list — skipped

            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(data));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectedVariables()).isEmpty();
        }

        @Test
        @DisplayName("Should handle multiple variables in the collected data")
        void convert_multipleVariables_shouldMapAll() {
            // GIVEN
            LunaticXmlSurveyUnit su = buildSurveyUnit(List.of(
                    collectedDataWithValue("VAR1", "v1"),
                    collectedDataWithValue("VAR2", "v2")
            ));

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getCollectedVariables()).hasSize(2);
            assertThat(collected.getCollectedVariables())
                    .extracting(VariableModel::varId)
                    .containsExactlyInAnyOrder("VAR1", "VAR2");
        }
    }

    // -------------------------------------------------------------------------
    // convert() — external variables
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("convert() — external variables tests")
    class ConvertExternalVariablesTests {

        @Test
        @DisplayName("Should populate externalVariables on the COLLECTED model")
        void convert_shouldPopulateExternalVariables() {
            // GIVEN
            LunaticXmlOtherData external = new LunaticXmlOtherData();
            external.setVariableName("EXT1");
            external.setValues(List.of(getValueType("ext-value")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(
                    List.of(collectedDataWithValue("VAR1", "v")),
                    List.of(external)
            );

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getExternalVariables()).hasSize(1);
            assertThat(collected.getExternalVariables().getFirst().varId()).isEqualTo("EXT1");
            assertThat(collected.getExternalVariables().getFirst().value()).isEqualTo("ext-value");
        }

        @Test
        @DisplayName("Should skip external variables with null value")
        void convert_externalNullValue_shouldSkip() {
            // GIVEN
            LunaticXmlOtherData external = new LunaticXmlOtherData();
            external.setVariableName("EXT1");
            external.setValues(List.of(getValueType(null)));

            LunaticXmlSurveyUnit su = buildSurveyUnit(
                    List.of(collectedDataWithValue("VAR1", "v")),
                    List.of(external)
            );

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getExternalVariables()).isEmpty();
        }

        @Test
        @DisplayName("Should skip external variable when values list is null")
        void convert_externalNullValuesList_shouldSkip() {
            // GIVEN
            LunaticXmlOtherData external = new LunaticXmlOtherData();
            external.setVariableName("EXT1");
            external.setValues(null);

            LunaticXmlSurveyUnit su = buildSurveyUnit(
                    List.of(collectedDataWithValue("VAR1", "v")),
                    List.of(external)
            );

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getExternalVariables()).isEmpty();
        }

        @Test
        @DisplayName("Should set iteration starting at 1 for external variables")
        void convert_externalVariables_shouldSetIteration() {
            // GIVEN
            LunaticXmlOtherData external = new LunaticXmlOtherData();
            external.setVariableName("EXT1");
            external.setValues(List.of(getValueType("v1"), getValueType("v2")));

            LunaticXmlSurveyUnit su = buildSurveyUnit(
                    List.of(collectedDataWithValue("VAR1", "v")),
                    List.of(external)
            );

            // WHEN
            SurveyUnitModel collected = getCollected(LunaticXmlAdapter.convert(su, VARIABLES_MAP, MODE));

            // THEN
            assertThat(collected.getExternalVariables()).hasSize(2);
            assertThat(collected.getExternalVariables().get(0).iteration()).isEqualTo(1);
            assertThat(collected.getExternalVariables().get(1).iteration()).isEqualTo(2);
        }
    }

    // -------------------------------------------------------------------------
    // Helpers
    // -------------------------------------------------------------------------

    private LunaticXmlSurveyUnit buildSurveyUnit(List<LunaticXmlCollectedData> collectedData) {
        return buildSurveyUnit(collectedData, List.of());
    }

    private LunaticXmlSurveyUnit buildSurveyUnit(
            List<LunaticXmlCollectedData> collectedData,
            List<LunaticXmlOtherData> externalData) {

        LunaticXmlSurveyUnit su = new LunaticXmlSurveyUnit();
        su.setId(INTERROGATION_ID);
        su.setQuestionnaireModelId(QUESTIONNAIRE_ID);

        fr.insee.genesis.controller.sources.xml.LunaticXmlData data =
                new fr.insee.genesis.controller.sources.xml.LunaticXmlData();
        data.setCollected(collectedData);
        data.setExternal(externalData);
        su.setData(data);

        return su;
    }

    private LunaticXmlCollectedData collectedDataWithValue(String varName, String value) {
        LunaticXmlCollectedData data = new LunaticXmlCollectedData();
        data.setVariableName(varName);
        data.setCollected(List.of(getValueType(value)));
        return data;
    }

    private ValueType getValueType(String value) {
        ValueType vt = new ValueType(
                value,
                "STRING"
        );
        vt.setValue(value);
        return vt;
    }

    private SurveyUnitModel getCollected(List<SurveyUnitModel> models) {
        return models.stream()
                .filter(m -> m.getState() == DataState.COLLECTED)
                .findFirst()
                .orElseThrow(() -> new AssertionError("No COLLECTED model found"));
    }
}