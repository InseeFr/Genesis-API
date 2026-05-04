package fr.insee.genesis.controller.adapter;

import fr.insee.genesis.controller.sources.json.LunaticJsonSurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("LunaticJsonAdapter tests")
class LunaticJsonAdapterTest {

    private final LunaticJsonAdapter adapter = new LunaticJsonAdapter();

    @Nested
    @DisplayName("convert() tests")
    class ConvertTests {

        @Test
        @DisplayName("Should map questionnaireId to collectionInstrumentId")
        void convert_shouldMapQuestionnaireIdToCollectionInstrumentId() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("questionnaire-123", "interrogation-456");

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            assertThat(result.getCollectionInstrumentId()).isEqualTo("questionnaire-123");
        }

        @Test
        @DisplayName("Should map interrogationId")
        void convert_shouldMapInterrogationId() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("questionnaire-123", "interrogation-456");

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            assertThat(result.getInterrogationId()).isEqualTo("interrogation-456");
        }

        @Test
        @DisplayName("Should set state to COLLECTED")
        void convert_shouldSetStateToCollected() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("q1", "i1");

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            assertThat(result.getState()).isEqualTo(DataState.COLLECTED);
        }

        @Test
        @DisplayName("Should set mode to WEB")
        void convert_shouldSetModeToWeb() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("q1", "i1");

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            assertThat(result.getMode()).isEqualTo(Mode.WEB);
        }

        @Test
        @DisplayName("Should set a non-null recordDate close to now")
        void convert_shouldSetRecordDateCloseToNow() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("q1", "i1");
            Instant before = Instant.now();

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            Instant after = Instant.now();
            assertThat(result.getRecordDate())
                    .isNotNull()
                    .isAfterOrEqualTo(before)
                    .isBeforeOrEqualTo(after);
        }

        @Test
        @DisplayName("Should return a non-null model")
        void convert_shouldReturnNonNullModel() {
            // GIVEN
            LunaticJsonSurveyUnit su = buildSurveyUnit("q1", "i1");

            // WHEN
            SurveyUnitModel result = adapter.convert(su);

            // THEN
            assertThat(result).isNotNull();
        }
    }

    //UTILS
    private LunaticJsonSurveyUnit buildSurveyUnit(String questionnaireId, String interrogationId) {
        LunaticJsonSurveyUnit su = new LunaticJsonSurveyUnit();
        su.setQuestionnaireId(questionnaireId);
        su.setInterrogationId(interrogationId);
        return su;
    }
}