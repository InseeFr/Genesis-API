package fr.insee.genesis.domain.model.surveyunit;

import fr.insee.modelefiliere.RawResponseDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
@DisplayName("SurveyUnitModel Tests")
class SurveyUnitModelTest {

    private static final String INTERROGATION_ID = "interrogation-123";
    private static final String COLLECTION_INSTRUMENT_ID = "instrument-456";
    private static final String CAMPAIGN_ID = "campaign-789";
    private static final String USUAL_SURVEY_UNIT_ID = "usual-unit-001";
    private static final String TECHNICAL_SURVEY_UNIT_ID = "technical-unit-002";
    private static final String MAJOR_MODEL_VERSION = "2";
    private static final String MODIFIED_BY = "user-admin";

    private Mode mockMode;
    private LocalDateTime now;

    @BeforeEach
    void setUp() {
        mockMode = Mode.WEB;
        now = LocalDateTime.of(2025, 6, 15, 10, 30);
    }

    @Nested
    @DisplayName("Constructor tests")
    class ConstructorTests {

        @Test
        @DisplayName("No-args constructor should create an empty model")
        void noArgsConstructor_shouldCreateEmptyModel() {
            SurveyUnitModel model = new SurveyUnitModel();

            assertThat(model).isNotNull();
            assertThat(model.getInterrogationId()).isNull();
            assertThat(model.getMode()).isNull();
            assertThat(model.getCollectionInstrumentId()).isNull();
            assertThat(model.getUsualSurveyUnitId()).isNull();
            assertThat(model.getTechnicalSurveyUnitId()).isNull();
            assertThat(model.getMajorModelVersion()).isNull();
            assertThat(model.getState()).isNull();
            assertThat(model.getIsCapturedIndirectly()).isNull();
            assertThat(model.getQuestionnaireState()).isNull();
            assertThat(model.getValidationDate()).isNull();
            assertThat(model.getRecordDate()).isNull();
            assertThat(model.getFileDate()).isNull();
            assertThat(model.getCollectedVariables()).isNull();
            assertThat(model.getExternalVariables()).isNull();
            assertThat(model.getModifiedBy()).isNull();
        }

        @Test
        @DisplayName("Constructor(interrogationId, mode) should set only those two fields")
        void twoArgsConstructor_shouldSetInterrogationIdAndMode() {
            SurveyUnitModel model = new SurveyUnitModel(INTERROGATION_ID, mockMode);

            assertThat(model.getInterrogationId()).isEqualTo(INTERROGATION_ID);
            assertThat(model.getMode()).isEqualTo(mockMode);

            // All other fields should be null
            assertThat(model.getCollectionInstrumentId()).isNull();
            assertThat(model.getUsualSurveyUnitId()).isNull();
            assertThat(model.getState()).isNull();
        }

        @Test
        @DisplayName("All-args constructor should set all fields")
        void allArgsConstructor_shouldSetAllFields() {
            List<VariableModel> collected = List.of();
            List<VariableModel> external = List.of();

            SurveyUnitModel model = new SurveyUnitModel(
                    COLLECTION_INSTRUMENT_ID,
                    CAMPAIGN_ID,
                    INTERROGATION_ID,
                    USUAL_SURVEY_UNIT_ID,
                    TECHNICAL_SURVEY_UNIT_ID,
                    MAJOR_MODEL_VERSION,
                    DataState.COLLECTED,
                    mockMode,
                    true,
                    RawResponseDto.QuestionnaireStateEnum.FINISHED,
                    now,
                    now,
                    now,
                    collected,
                    external,
                    MODIFIED_BY
            );

            assertThat(model.getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
            assertThat(model.getInterrogationId()).isEqualTo(INTERROGATION_ID);
            assertThat(model.getUsualSurveyUnitId()).isEqualTo(USUAL_SURVEY_UNIT_ID);
            assertThat(model.getTechnicalSurveyUnitId()).isEqualTo(TECHNICAL_SURVEY_UNIT_ID);
            assertThat(model.getMajorModelVersion()).isEqualTo(MAJOR_MODEL_VERSION);
            assertThat(model.getState()).isEqualTo(DataState.COLLECTED);
            assertThat(model.getMode()).isEqualTo(mockMode);
            assertThat(model.getIsCapturedIndirectly()).isTrue();
            assertThat(model.getQuestionnaireState()).isEqualTo(RawResponseDto.QuestionnaireStateEnum.FINISHED);
            assertThat(model.getValidationDate()).isEqualTo(now);
            assertThat(model.getRecordDate()).isEqualTo(now);
            assertThat(model.getFileDate()).isEqualTo(now);
            assertThat(model.getCollectedVariables()).isEqualTo(collected);
            assertThat(model.getExternalVariables()).isEqualTo(external);
            assertThat(model.getModifiedBy()).isEqualTo(MODIFIED_BY);
        }
    }

    // -------------------------------------------------------------------------
    // Builder
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Builder tests")
    class BuilderTests {

        @Test
        @DisplayName("Builder should create model with all specified fields")
        void builder_shouldSetAllFields() {
            List<VariableModel> collected = List.of();
            List<VariableModel> external = List.of();

            SurveyUnitModel model = SurveyUnitModel.builder()
                    .collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
                    .interrogationId(INTERROGATION_ID)
                    .usualSurveyUnitId(USUAL_SURVEY_UNIT_ID)
                    .technicalSurveyUnitId(TECHNICAL_SURVEY_UNIT_ID)
                    .majorModelVersion(MAJOR_MODEL_VERSION)
                    .state(DataState.COLLECTED)
                    .mode(mockMode)
                    .isCapturedIndirectly(false)
                    .questionnaireState(RawResponseDto.QuestionnaireStateEnum.FINISHED)
                    .validationDate(now)
                    .recordDate(now)
                    .fileDate(now)
                    .collectedVariables(collected)
                    .externalVariables(external)
                    .modifiedBy(MODIFIED_BY)
                    .build();

            assertThat(model.getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
            assertThat(model.getInterrogationId()).isEqualTo(INTERROGATION_ID);
            assertThat(model.getUsualSurveyUnitId()).isEqualTo(USUAL_SURVEY_UNIT_ID);
            assertThat(model.getTechnicalSurveyUnitId()).isEqualTo(TECHNICAL_SURVEY_UNIT_ID);
            assertThat(model.getMajorModelVersion()).isEqualTo(MAJOR_MODEL_VERSION);
            assertThat(model.getState()).isEqualTo(DataState.COLLECTED);
            assertThat(model.getMode()).isEqualTo(mockMode);
            assertThat(model.getIsCapturedIndirectly()).isFalse();
            assertThat(model.getQuestionnaireState()).isEqualTo(RawResponseDto.QuestionnaireStateEnum.FINISHED);
            assertThat(model.getValidationDate()).isEqualTo(now);
            assertThat(model.getRecordDate()).isEqualTo(now);
            assertThat(model.getFileDate()).isEqualTo(now);
            assertThat(model.getCollectedVariables()).isEqualTo(collected);
            assertThat(model.getExternalVariables()).isEqualTo(external);
            assertThat(model.getModifiedBy()).isEqualTo(MODIFIED_BY);
        }

        @Test
        @DisplayName("Builder with only mandatory fields should leave others null")
        void builder_withPartialFields_shouldLeaveOthersNull() {
            SurveyUnitModel model = SurveyUnitModel.builder()
                    .interrogationId(INTERROGATION_ID)
                    .mode(mockMode)
                    .build();

            assertThat(model.getInterrogationId()).isEqualTo(INTERROGATION_ID);
            assertThat(model.getMode()).isEqualTo(mockMode);
            assertThat(model.getCollectionInstrumentId()).isNull();
            assertThat(model.getState()).isNull();
            assertThat(model.getValidationDate()).isNull();
        }
    }

    // -------------------------------------------------------------------------
    // Setters (Lombok @Data)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Setter tests (via @Data)")
    class SetterTests {

        @Test
        @DisplayName("setInterrogationId should update the field")
        void setInterrogationId_shouldUpdateField() {
            SurveyUnitModel model = new SurveyUnitModel();
            model.setInterrogationId(INTERROGATION_ID);
            assertThat(model.getInterrogationId()).isEqualTo(INTERROGATION_ID);
        }

        @Test
        @DisplayName("setMode should update the field")
        void setMode_shouldUpdateField() {
            SurveyUnitModel model = new SurveyUnitModel();
            model.setMode(mockMode);
            assertThat(model.getMode()).isEqualTo(mockMode);
        }

        @Test
        @DisplayName("setIsCapturedIndirectly should update the boolean field")
        void setIsCapturedIndirectly_shouldUpdateField() {
            SurveyUnitModel model = new SurveyUnitModel();
            model.setIsCapturedIndirectly(true);
            assertThat(model.getIsCapturedIndirectly()).isTrue();
        }

        @Test
        @DisplayName("setCollectedVariables should update the list")
        void setCollectedVariables_shouldUpdateField() {
            SurveyUnitModel model = new SurveyUnitModel();
            List<VariableModel> variables = List.of(getVariableModel());
            model.setCollectedVariables(variables);
            assertThat(model.getCollectedVariables()).hasSize(1);
        }

        @Test
        @DisplayName("setExternalVariables should update the list")
        void setExternalVariables_shouldUpdateField() {
            SurveyUnitModel model = new SurveyUnitModel();
            List<VariableModel> variables = List.of(getVariableModel(), getVariableModel());
            model.setExternalVariables(variables);
            assertThat(model.getExternalVariables()).hasSize(2);
        }
    }

    private static VariableModel getVariableModel() {
        return new VariableModel(
                "testVar",
                "testValue",
                DataState.COLLECTED,
                "RACINE",
                1,
                null
        );
    }

    // -------------------------------------------------------------------------
    // equals()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("equals() tests")
    class EqualsTests {

        @Test
        @DisplayName("Same instance should be equal to itself")
        void equals_sameInstance_shouldBeTrue() {
            SurveyUnitModel model = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            assertThat(model).isEqualTo(model);
        }

        @Test
        @DisplayName("Two models with same interrogationId and mode should be equal")
        void equals_sameInterrogationIdAndMode_shouldBeTrue() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            SurveyUnitModel model2 = new SurveyUnitModel(INTERROGATION_ID, mockMode);

            assertThat(model1).isEqualTo(model2);
        }

        @Test
        @DisplayName("Two models with different interrogationId should not be equal")
        void equals_differentInterrogationId_shouldBeFalse() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            SurveyUnitModel model2 = new SurveyUnitModel("other-id", mockMode);

            assertThat(model1).isNotEqualTo(model2);
        }

        @Test
        @DisplayName("Two models with different mode should not be equal")
        void equals_differentMode_shouldBeFalse() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, Mode.WEB);
            SurveyUnitModel model2 = new SurveyUnitModel(INTERROGATION_ID, Mode.PAPER);

            assertThat(model1).isNotEqualTo(model2);
        }

        @Test
        @DisplayName("Equals should ignore other fields (e.g., usualSurveyUnitId)")
        void equals_differentOtherFields_shouldStillBeEqual() {
            SurveyUnitModel model1 = SurveyUnitModel.builder()
                    .interrogationId(INTERROGATION_ID)
                    .mode(mockMode)
                    .usualSurveyUnitId("UE-A")
                    .build();
            SurveyUnitModel model2 = SurveyUnitModel.builder()
                    .interrogationId(INTERROGATION_ID)
                    .mode(mockMode)
                    .usualSurveyUnitId("UE-B")
                    .build();

            assertThat(model1).isEqualTo(model2);
        }

        @Test
        @DisplayName("Equals with null should return false")
        void equals_null_shouldBeFalse() {
            SurveyUnitModel model = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            assertThat(model).isNotEqualTo(null);
        }

        @Test
        @DisplayName("Equals with different class should return false")
        void equals_differentClass_shouldBeFalse() {
            SurveyUnitModel model = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            assertThat(model).isNotEqualTo("a string");
        }

        @Test
        @DisplayName("Two models with null interrogationId and null mode should be equal")
        void equals_bothNullFields_shouldBeEqual() {
            SurveyUnitModel model1 = new SurveyUnitModel();
            SurveyUnitModel model2 = new SurveyUnitModel();

            assertThat(model1).isEqualTo(model2);
        }
    }

    // -------------------------------------------------------------------------
    // hashCode()
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("hashCode() tests")
    class HashCodeTests {

        @Test
        @DisplayName("Same interrogationId and mode should produce the same hashCode")
        void hashCode_sameFields_shouldBeEqual() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            SurveyUnitModel model2 = new SurveyUnitModel(INTERROGATION_ID, mockMode);

            assertThat(model1).hasSameHashCodeAs(model2);
        }

        @Test
        @DisplayName("Different interrogationId should (likely) produce different hashCode")
        void hashCode_differentInterrogationId_shouldDiffer() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            SurveyUnitModel model2 = new SurveyUnitModel("completely-different-id", mockMode);

            assertThat(model1.hashCode()).isNotEqualTo(model2.hashCode());
        }

        @Test
        @DisplayName("hashCode should be consistent with equals")
        void hashCode_consistentWithEquals() {
            SurveyUnitModel model1 = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            SurveyUnitModel model2 = new SurveyUnitModel(INTERROGATION_ID, mockMode);

            assertThat(model1).isEqualTo(model2).hasSameHashCodeAs(model2);
        }
    }

    // -------------------------------------------------------------------------
    // toString() (generated by @Data)
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("toString() tests")
    class ToStringTests {

        @Test
        @DisplayName("toString should contain interrogationId")
        void toString_shouldContainInterrogationId() {
            SurveyUnitModel model = new SurveyUnitModel(INTERROGATION_ID, mockMode);
            assertThat(model.toString()).contains(INTERROGATION_ID);
        }

        @Test
        @DisplayName("toString should not be null or empty")
        void toString_shouldNotBeNullOrEmpty() {
            SurveyUnitModel model = new SurveyUnitModel();
            assertThat(model.toString()).isNotNull().isNotEmpty();
        }
    }

    // -------------------------------------------------------------------------
    // Deprecation awareness
    // -------------------------------------------------------------------------

    @Nested
    @DisplayName("Deprecated field (campaignId) tests")
    class DeprecatedFieldTests {

        @Test
        @DisplayName("campaignId setter and getter should still function")
        @SuppressWarnings("deprecation")
        void campaignId_shouldBeSettableAndGettable() {
            SurveyUnitModel model = new SurveyUnitModel();
            model.setCampaignId(CAMPAIGN_ID);
            assertThat(model.getCampaignId()).isEqualTo(CAMPAIGN_ID);
        }

        @Test
        @DisplayName("campaignId via builder should still work")
        @SuppressWarnings("deprecation")
        void campaignId_viaBuilder_shouldWork() {
            SurveyUnitModel model = SurveyUnitModel.builder()
                    .campaignId(CAMPAIGN_ID)
                    .build();
            assertThat(model.getCampaignId()).isEqualTo(CAMPAIGN_ID);
        }
    }
}