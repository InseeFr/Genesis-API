package fr.insee.genesis.domain.converter.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawDataModelType;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;

@ExtendWith(MockitoExtension.class)
class RawDataConverterTest {

    @Mock
    private SurveyUnitService surveyUnitService;

    private VariablesMap variablesMap;

    @InjectMocks
    private final RawDataConverter rawDataConverterTestImpl = new RawDataConverter(surveyUnitService) {
        @Override
        protected Map<String, Map<DataState, SurveyUnitModel>> getLastSurveyUnitModels(String questionnaireOrCollectionInstrumentId, List<String> interrogationIds) {
            return super.getLastSurveyUnitModels(questionnaireOrCollectionInstrumentId, interrogationIds);
        }
    };

    @BeforeEach
    void setUp(){
        variablesMap = new VariablesMap();
    }

    @Nested
    @DisplayName("getLastSurveyUnitModels tests")
    class getLastSurveyUnitModelsTests{
        @Test
        @DisplayName("Should return map of map with interrogation ids and states as keys")
        void getLastSurveyUnitModelsTest() {
            //GIVEN
            String questionnaireId = "questionnaire";
            Set<String> interrogationIds = Set.of("Interrogation1", "Interrogation2");

            List<SurveyUnitModel> surveyUnitModels = new ArrayList<>();
            for(String interrogationId : interrogationIds){
                surveyUnitModels.add(
                        SurveyUnitModel.builder()
                                .collectionInstrumentId(questionnaireId)
                                .state(DataState.COLLECTED)
                                .interrogationId(interrogationId)
                                .build()
                );
            }

            Mockito.when(surveyUnitService.findLatestByInterrogationIds(questionnaireId, interrogationIds)
            ).thenReturn(surveyUnitModels);

            //WHEN

            Map<String, Map<DataState, SurveyUnitModel>> resultMap = rawDataConverterTestImpl.getLastSurveyUnitModels(
                questionnaireId,
                interrogationIds.stream().toList()
            );


            //THEN
            //Service call with good arguments
            @SuppressWarnings("unchecked")
            ArgumentCaptor<Set<String>> setArgumentCaptor = ArgumentCaptor.forClass(Set.class);
            Mockito.verify(surveyUnitService, Mockito.times(1)).findLatestByInterrogationIds(
                    eq(questionnaireId),
                    setArgumentCaptor.capture()
            );
            assertThat(setArgumentCaptor.getValue()).containsExactlyInAnyOrder(
                    "Interrogation1","Interrogation2"
            );

            //Check resulted map key and values content
            assertThat(resultMap).containsOnlyKeys(interrogationIds);
            for(String interrogationId : interrogationIds){
                Map<DataState, SurveyUnitModel> surveyUnitModelsOfInterrogationId = resultMap.get(interrogationId);
                assertThat(surveyUnitModelsOfInterrogationId).containsOnlyKeys(DataState.COLLECTED);
                assertThat(
                        surveyUnitModelsOfInterrogationId.get(DataState.COLLECTED).getInterrogationId()
                ).isEqualTo(interrogationId);
            }
        }
    }

    @Nested
    @DisplayName("getValueString() util")
    class GetValueStringTests {

        @Test
        @DisplayName("Double value strips trailing zeros")
        void doubleStripsTrailingZeros() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString(1.50)).isEqualTo("1.5");
        }

        @Test
        @DisplayName("Float value strips trailing zeros")
        void floatStripsTrailingZeros() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString(1.500f)).isEqualTo("1.5");
        }

        @Test
        @DisplayName("Integer value returns plain string")
        void integerReturnsPlainString() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString(42)).isEqualTo("42");
        }

        @Test
        @DisplayName("String value returns same string")
        void stringReturnsItself() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Null returns 'null' string")
        void nullReturnsNullString() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("BigDecimal integer-like double has no decimal point")
        void bigDecimalIntegerDouble() {
            //WHEN + THEN
            assertThat(RawDataConverter.getValueString(3.0)).isEqualTo("3");
        }
    }

    @DisplayName("convertCollectedVariables tests")
    @Nested
    class convertCollectedVariablesTests{
        @Test
        void convertCollectedVariables_shouldDoNothing_whenNoCollectedKeyAndNoLastSurveyUnit() {
            Map<String, Object> payload = new HashMap<>();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .collectedVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertCollectedVariables(
                    payload,
                    "INT1",
                    null, // lastSurveyUnitModel
                    dst,
                    DataState.COLLECTED,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getCollectedVariables()).isEmpty();
        }

        @Test
        void convertCollectedVariables_shouldDoNothing_whenNoCollectedKeyAndLastSurveyUnitStateDoesNotMatch() {
            Map<String, Object> payload = new HashMap<>(); // pas de clé "COLLECTED"

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .state(DataState.FORCED) // différent de DataState.COLLECTED
                    .collectedVariables(List.of(
                            VariableModel.builder().varId("VAR1").value("val").iteration(1).build()
                    ))
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .collectedVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertCollectedVariables(
                    payload,
                    "INT1",
                    lastSurveyUnit,
                    dst,
                    DataState.COLLECTED,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getCollectedVariables()).isEmpty();
        }

        @Test
        void convertCollectedVariables_shouldAddNullVariable_whenNoCollectedKeyAndLastSurveyUnitHasSingleVariable() {
            Map<String, Object> payload = new HashMap<>(); // pas de clé "COLLECTED"

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .state(DataState.COLLECTED) // même état que dataState attendu
                    .collectedVariables(List.of(
                            VariableModel.builder().varId("VAR1").value("oldVal").iteration(1).build()
                    ))
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .collectedVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertCollectedVariables(
                    payload,
                    "INT1",
                    lastSurveyUnit,
                    dst,
                    DataState.COLLECTED,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getCollectedVariables())
                    .hasSize(1)
                    .first()
                    .satisfies(v -> {
                        assertThat(v.varId()).isEqualTo("VAR1");
                        assertThat(v.value()).isNull();
                        assertThat(v.iteration()).isEqualTo(1);
                    });
        }

        @Test
        void convertCollectedVariables_shouldAddNullVariablesForEachIteration_whenNoCollectedKeyAndLastSurveyUnitHasMultipleIterations() {
            Map<String, Object> payload = new HashMap<>(); // pas de clé "COLLECTED"

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .state(DataState.COLLECTED)
                    .collectedVariables(List.of(
                            VariableModel.builder().varId("VAR1").value("old1").iteration(1).build(),
                            VariableModel.builder().varId("VAR1").value("old2").iteration(2).build(),
                            VariableModel.builder().varId("VAR1").value("old3").iteration(3).build()
                    ))
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .collectedVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertCollectedVariables(
                    payload,
                    "INT1",
                    lastSurveyUnit,
                    dst,
                    DataState.COLLECTED,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getCollectedVariables())
                    .hasSize(3)
                    .allSatisfy(v -> {
                        assertThat(v.varId()).isEqualTo("VAR1");
                        assertThat(v.value()).isNull();
                    })
                    .extracting(VariableModel::iteration)
                    .containsExactlyInAnyOrder(1, 2, 3);
        }
    }

    @DisplayName("convertExternalVariables tests")
    @Nested
    class convertExternalVariablesTests {
        @Test
        void convertExternalVariables_shouldDoNothing_whenNoExternalKeyAndNoLastSurveyUnit() {
            Map<String, Object> payload = new HashMap<>(); // pas de clé "EXTERNAL"

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertExternalVariables(
                    payload,
                    null,
                    dst,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getExternalVariables()).isEmpty();
        }

        @Test
        void convertExternalVariables_shouldDoNothing_whenNoExternalKeyAndLastSurveyUnitHasNoExternalVariables() {
            Map<String, Object> payload = new HashMap<>();

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(new ArrayList<>())
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertExternalVariables(
                    payload,
                    lastSurveyUnit,
                    dst,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getExternalVariables()).isEmpty();
        }

        @Test
        void convertExternalVariables_shouldAddNullVariable_whenNoExternalKeyAndLastSurveyUnitHasSingleVariable() {
            Map<String, Object> payload = new HashMap<>();

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(List.of(
                            VariableModel.builder().varId("EXTVAR1").value("oldVal").iteration(1).build()
                    ))
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertExternalVariables(
                    payload,
                    lastSurveyUnit,
                    dst,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getExternalVariables())
                    .hasSize(1)
                    .first()
                    .satisfies(v -> {
                        assertThat(v.varId()).isEqualTo("EXTVAR1");
                        assertThat(v.value()).isNull();
                        assertThat(v.iteration()).isEqualTo(1);
                    });
        }

        @Test
        void convertExternalVariables_shouldAddNullVariablesForEachIteration_whenNoExternalKeyAndLastSurveyUnitHasMultipleIterations() {
            Map<String, Object> payload = new HashMap<>();

            SurveyUnitModel lastSurveyUnit = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(List.of(
                            VariableModel.builder().varId("EXTVAR1").value("old1").iteration(1).build(),
                            VariableModel.builder().varId("EXTVAR1").value("old2").iteration(2).build()
                    ))
                    .build();

            SurveyUnitModel dst = SurveyUnitModel.builder()
                    .interrogationId("INT1")
                    .externalVariables(new ArrayList<>())
                    .build();

            rawDataConverterTestImpl.convertExternalVariables(
                    payload,
                    lastSurveyUnit,
                    dst,
                    RawDataModelType.FILIERE,
                    variablesMap
            );

            assertThat(dst.getExternalVariables())
                    .hasSize(2)
                    .allSatisfy(v -> {
                        assertThat(v.varId()).isEqualTo("EXTVAR1");
                        assertThat(v.value()).isNull();
                    })
                    .extracting(VariableModel::iteration)
                    .containsExactlyInAnyOrder(1, 2);
        }
    }
}