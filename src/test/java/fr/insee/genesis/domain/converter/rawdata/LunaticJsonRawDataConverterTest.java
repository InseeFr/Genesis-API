package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.parser.rawdata.LunaticJsonRawDataPayloadParser;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LunaticJsonRawDataConverterTest {

    private static final LocalDateTime DATE_TIME = LocalDateTime.parse("2025-01-01T10:00:00");
    private static final String QUESTIONNAIRE_ID = "testQuestionnaire";
    private static final String INTERROGATION_ID = "testInterrogation";

    @Mock
    private SurveyUnitService surveyUnitService;

    @Mock
    private LunaticJsonRawDataPayloadParser payloadParser;

    @InjectMocks
    private LunaticJsonRawDataConverter converter;

    @Test
    void shouldReturnNoSurveyUnitWhenRawDataListIsEmpty() {
        VariablesMap variablesMap = mock(VariablesMap.class);
        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                "test",
                List.of(),
                variablesMap,
                emptySurveyUnitModels
        );

        assertThat(result).isEmpty();
        assertThat(emptySurveyUnitModels).isEmpty();
    }

    @Test
    void shouldCreateCollectedAndEditedSurveyUnitsFromLegacyRawData() {
        VariablesMap variablesMap = mock(VariablesMap.class);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(
                        "FIRST_NAME", Map.of("COLLECTED", "Alice", "EDITED", "Alicia")
                )
        ));

        List<SurveyUnitModel> result = converter.convertRawData(
                QUESTIONNAIRE_ID,
                List.of(rawData),
                variablesMap
        );

        assertThat(result).hasSize(2);

        assertThat(result)
                .extracting(SurveyUnitModel::getState)
                .containsExactly(DataState.COLLECTED, DataState.EDITED);

        for(DataState dataState : Set.of(DataState.COLLECTED, DataState.EDITED)){
            Optional<SurveyUnitModel> surveyUnitModelOptional = result.stream().filter(
                    surveyUnitModel -> surveyUnitModel.getState().equals(dataState)
            ).findFirst();

            Assertions.assertThat(surveyUnitModelOptional).isPresent();
            SurveyUnitModel surveyUnitModel = surveyUnitModelOptional.get();
            Assertions.assertThat(surveyUnitModel.getCollectedVariables())
                    .extracting(VariableModel::varId)
                    .containsExactly("FIRST_NAME");
            Assertions.assertThat(surveyUnitModel.getCollectedVariables())
                    .extracting(VariableModel::value)
                    .containsExactly(dataState.equals(DataState.COLLECTED) ? "Alice" : "Alicia");
        }
    }

    @Test
    void shouldConvertExternalVariablesOnlyForCollectedSurveyUnit() {
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
                QUESTIONNAIRE_ID,
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
        VariablesMap variablesMap = mock(VariablesMap.class);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(),
                "EXTERNAL", Map.of()
        ));

        List<SurveyUnitModel> emptySurveyUnitModels = new ArrayList<>();

        List<SurveyUnitModel> result = converter.convertRawDataAndCollectEmptyModels(
                QUESTIONNAIRE_ID,
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
                QUESTIONNAIRE_ID,
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
        VariablesMap variablesMap = mock(VariablesMap.class);

        when(variablesMap.getVariable("UNKNOWN_VARIABLE")).thenReturn(null);

        LunaticJsonRawDataModel rawData = rawData(Map.of(
                "COLLECTED", Map.of(),
                "EXTERNAL", Map.of("UNKNOWN_VARIABLE", "value")
        ));

        List<SurveyUnitModel> result = converter.convertRawData(
                QUESTIONNAIRE_ID,
                List.of(rawData),
                variablesMap
        );

        assertThat(result).hasSize(1);

        assertThat(result.getFirst().getExternalVariables())
                .singleElement()
                .extracting(VariableModel::scope)
                .isEqualTo(Constants.ROOT_GROUP_NAME);
    }

    @Nested
    @DisplayName("Null cases tests")
    class NullVariablesTests {

        //NullVariablesTests constants
        private static final String COLLECTED_VARIABLE_NAME = "VAR1";
        private static final String COLLECTED_VARIABLE_VALUE = "test";
        private static final String EXTERNAL_VARIABLE_NAME = "EXTVAR1";
        private static final String EXTERNAL_VARIABLE_VALUE = "ext1";

        @Test
        @DisplayName("Should not add variable if not already existant and null in raw")
        void shouldNotAddIfNull() {
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit
            SurveyUnitModel surveyUnitModel = getSurveyUnitModel();

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response with new null variables
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, Map.of("COLLECTED", COLLECTED_VARIABLE_VALUE));
            collectedVariablesMap.put("VAR2", null);

            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();
            externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, EXTERNAL_VARIABLE_VALUE);
            externalVariablesMap.put("EXTVAR2", null);

            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertNonNullVariables(surveyUnitModels);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Should convert to non null if existant variable is null or absent")
        void shouldConvertToNonNullValueIfNullOrNotExists(boolean isVariableAlreadyPresent){
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .collectionInstrumentId(QUESTIONNAIRE_ID)
                    .interrogationId(INTERROGATION_ID)
                    .collectedVariables(new ArrayList<>())
                    .externalVariables(new ArrayList<>())
                    .build();

            if(isVariableAlreadyPresent) {
                surveyUnitModel.getCollectedVariables().add(
                        VariableModel.builder()
                                .varId(COLLECTED_VARIABLE_NAME)
                                .value(null)
                                .state(DataState.COLLECTED)
                                .scope(Constants.ROOT_GROUP_NAME)
                                .iteration(1)
                                .build()
                );
                surveyUnitModel.getExternalVariables().add(
                        VariableModel.builder()
                                .varId(EXTERNAL_VARIABLE_NAME)
                                .value(null)
                                .state(DataState.COLLECTED)
                                .scope(Constants.ROOT_GROUP_NAME)
                                .iteration(1)
                                .build()
                );
            }

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, Map.of("COLLECTED", COLLECTED_VARIABLE_VALUE));

            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();
            externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, EXTERNAL_VARIABLE_VALUE);

            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertNonNullVariables(surveyUnitModels);
        }

        @Test
        @DisplayName("Should convert to null values if non null already exists (one value)")
        void shouldConvertNullValueIfNonNullOneValue(){
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit with non-null variables
            SurveyUnitModel surveyUnitModel = getSurveyUnitModel();

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response with null
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();

            collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, null);
            externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, null);


            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertNullVariables(surveyUnitModels);
        }

        @Test
        @DisplayName("Should convert to null values if non null already exists (multiple iterations)")
        void shouldConvertNullValueIfNonNullMultipleValues(){
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit with non-null variables and new iterations
            SurveyUnitModel surveyUnitModel = getSurveyUnitModel();
            surveyUnitModel.getCollectedVariables().add(
                    VariableModel.builder()
                            .varId(COLLECTED_VARIABLE_NAME)
                            .value("VALUE2")
                            .state(DataState.COLLECTED)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(2)
                            .build()
            );
            surveyUnitModel.getExternalVariables().add(
                    VariableModel.builder()
                            .varId(EXTERNAL_VARIABLE_NAME)
                            .value("ext2")
                            .state(DataState.COLLECTED)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(2)
                            .build()
            );

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response with null second iteration
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();

            List<String> variablesStrings = new ArrayList<>();
            variablesStrings.add(COLLECTED_VARIABLE_VALUE);
            variablesStrings.add(null);
            collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, Map.of("COLLECTED", variablesStrings));

            variablesStrings = new ArrayList<>();
            variablesStrings.add(EXTERNAL_VARIABLE_VALUE);
            variablesStrings.add(null);
            externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, variablesStrings);

            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertSecondIterationNull(surveyUnitModels);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Should keep null value if variable null or absent")
        void shouldKeepNull(boolean isNewVariablesPresent){
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit with null
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .collectionInstrumentId(QUESTIONNAIRE_ID)
                    .interrogationId(INTERROGATION_ID)
                    .state(DataState.COLLECTED)
                    .collectedVariables(new ArrayList<>())
                    .externalVariables(new ArrayList<>())
                    .build();
            surveyUnitModel.getCollectedVariables().add(
                    VariableModel.builder()
                            .varId(COLLECTED_VARIABLE_NAME)
                            .value(null)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(1)
                            .build()
            );
            surveyUnitModel.getExternalVariables().add(
                    VariableModel.builder()
                            .varId(EXTERNAL_VARIABLE_NAME)
                            .value(null)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(1)
                            .build()
            );

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();

            if(isNewVariablesPresent) {
                collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, null);
                externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, null);
            }

            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertNullVariables(surveyUnitModels);
        }

        @ParameterizedTest
        @ValueSource(booleans = {false, true})
        @DisplayName("Should keep null value if variable null or absent (multiple iterations)")
        void shouldKeepNullIteration(boolean isNewVariablesPresent){
            //GIVEN
            VariablesMap variablesMap = mock(VariablesMap.class);

            //Already existing survey unit
            SurveyUnitModel surveyUnitModel = getSurveyUnitModel();
            surveyUnitModel.getCollectedVariables().add(
                    VariableModel.builder()
                            .varId(COLLECTED_VARIABLE_NAME)
                            .value(null)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(2)
                            .build()
            );
            surveyUnitModel.getExternalVariables().add(
                    VariableModel.builder()
                            .varId(EXTERNAL_VARIABLE_NAME)
                            .value(null)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(2)
                            .build()
            );

            when(surveyUnitService.findLatestByInterrogationIds(eq(QUESTIONNAIRE_ID), anySet()))
                    .thenReturn(List.of(
                            surveyUnitModel
                    ));

            //Raw response with null or absent second iteration
            Map<String, Object> collectedVariablesMap = new LinkedHashMap<>();
            Map<String, Object> externalVariablesMap = new LinkedHashMap<>();

            List<String> collectedVariableValues = new ArrayList<>();
            collectedVariableValues.add(COLLECTED_VARIABLE_VALUE);

            List<String> externalVariableValues = new ArrayList<>();
            externalVariableValues.add(EXTERNAL_VARIABLE_VALUE);

            if(isNewVariablesPresent) {
                collectedVariableValues.add(null);
                externalVariableValues.add(null);
            }

            collectedVariablesMap.put(COLLECTED_VARIABLE_NAME, Map.of("COLLECTED", collectedVariableValues));
            externalVariablesMap.put(EXTERNAL_VARIABLE_NAME, externalVariableValues);

            LunaticJsonRawDataModel lunaticJsonRawDataModel = rawData(
                    Map.of(
                            "COLLECTED", collectedVariablesMap,
                            "EXTERNAL", externalVariablesMap
                    )
            );

            List<LunaticJsonRawDataModel> lunaticJsonRawDataModels = new ArrayList<>(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<SurveyUnitModel> surveyUnitModels = converter.convertRawData(
                    QUESTIONNAIRE_ID,
                    lunaticJsonRawDataModels,
                    variablesMap
            );

            //THEN
            assertSecondIterationNull(surveyUnitModels);
        }
        
        //NullVariablesTests UTILS
        private SurveyUnitModel getSurveyUnitModel() {
            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .collectionInstrumentId(QUESTIONNAIRE_ID)
                    .interrogationId(INTERROGATION_ID)
                    .state(DataState.COLLECTED)
                    .collectedVariables(new ArrayList<>())
                    .externalVariables(new ArrayList<>())
                    .build();

            surveyUnitModel.getCollectedVariables().add(
                    VariableModel.builder()
                            .varId(COLLECTED_VARIABLE_NAME)
                            .value(COLLECTED_VARIABLE_VALUE)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(1)
                            .build()
            );
            surveyUnitModel.getExternalVariables().add(
                    VariableModel.builder()
                            .varId(EXTERNAL_VARIABLE_NAME)
                            .value(EXTERNAL_VARIABLE_VALUE)
                            .scope(Constants.ROOT_GROUP_NAME)
                            .iteration(1)
                            .build()
            );
            return surveyUnitModel;
        }
        
        private void assertNullVariables(List<SurveyUnitModel> surveyUnitModels) {
            assertThat(surveyUnitModels).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables()).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables().getFirst()
                    .varId()).isEqualTo(COLLECTED_VARIABLE_NAME);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables().getFirst()
                    .value()).isNull();
            assertThat(surveyUnitModels.getFirst().getExternalVariables()).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst()
                    .varId()).isEqualTo(EXTERNAL_VARIABLE_NAME);
            assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst()
                    .value()).isNull();
        }

        private void assertNonNullVariables(List<SurveyUnitModel> surveyUnitModels) {
            assertThat(surveyUnitModels).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables()).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables().getFirst()
                    .varId()).isEqualTo(COLLECTED_VARIABLE_NAME);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables().getFirst()
                    .value()).isEqualTo(COLLECTED_VARIABLE_VALUE);
            assertThat(surveyUnitModels.getFirst().getExternalVariables()).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst()
                    .varId()).isEqualTo(EXTERNAL_VARIABLE_NAME);
            assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst()
                    .value()).isEqualTo(EXTERNAL_VARIABLE_VALUE);
        }

        private void assertSecondIterationNull(List<SurveyUnitModel> surveyUnitModels) {
            assertThat(surveyUnitModels).hasSize(1);
            assertThat(surveyUnitModels.getFirst().getCollectedVariables()).hasSize(2);
            for(VariableModel variableModel : surveyUnitModels.getFirst().getCollectedVariables()){
                assertThat(variableModel.iteration()).isIn(1,2);
                if(variableModel.iteration().equals(2)){
                    assertThat(variableModel.value()).isNull();
                    continue;
                }
                assertThat(variableModel.value()).isEqualTo(COLLECTED_VARIABLE_VALUE);
            }

            assertThat(surveyUnitModels.getFirst().getExternalVariables()).hasSize(2);
            for(VariableModel variableModel : surveyUnitModels.getFirst().getExternalVariables()) {
                assertThat(variableModel.iteration()).isIn(1,2);
                if (variableModel.iteration().equals(2)) {
                    assertThat(variableModel.value()).isNull();
                    continue;
                }
                assertThat(variableModel.value()).isEqualTo(EXTERNAL_VARIABLE_VALUE);
            }
        }
        
    }

    private LunaticJsonRawDataModel rawData(Map<String, Object> data) {
        LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                .questionnaireId(QUESTIONNAIRE_ID)
                .mode(Mode.WEB)
                .interrogationId(INTERROGATION_ID)
                .idUE("survey-unit-id")
                .recordDate(DATE_TIME)
                .data(data)
                .build();

        when(payloadParser.getValidationDate(rawData)).thenReturn(DATE_TIME);
        when(payloadParser.getIsCapturedIndirectly(rawData)).thenReturn(false);

        return rawData;
    }
}