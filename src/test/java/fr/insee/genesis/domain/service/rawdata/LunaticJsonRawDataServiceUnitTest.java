package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.configuration.Config;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.GroupedInterrogation;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.spi.DataProcessingContextPersistancePort;
import fr.insee.genesis.domain.ports.spi.LunaticJsonRawDataPersistencePort;
import fr.insee.genesis.domain.ports.spi.SurveyUnitQualityToolPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class LunaticJsonRawDataServiceTest {

    @Mock
    private LunaticJsonRawDataPersistencePort lunaticJsonRawDataPersistencePort;
    @Mock
    private ControllerUtils controllerUtils;
    @Mock
    private QuestionnaireMetadataService metadataService;
    @Mock
    private SurveyUnitService surveyUnitService;
    @Mock
    private SurveyUnitQualityService surveyUnitQualityService;
    @Mock
    private SurveyUnitQualityToolPort surveyUnitQualityToolPort;
    @Mock
    private DataProcessingContextService dataProcessingContextService;
    @Mock
    private DataProcessingContextPersistancePort dataProcessingContextPersistancePort;
    @Mock
    private FileUtils fileUtils;
    @Mock
    private Config config;

    @InjectMocks
    private LunaticJsonRawDataService service;

    private static final String CAMPAIGN_ID = "campaign-1";
    private static final String QUESTIONNAIRE_ID = "questionnaire-1";
    private static final String INTERROGATION_ID = "interrogation-1";

    @Nested
    @DisplayName("save()")
    class SaveTests {
        @Test
        @DisplayName("Calls persistence port")
        void save_delegatesToPort() {
            //GIVEN
            LunaticJsonRawDataModel model = buildRawDataWithCollected(Map.of());

            //WHEN
            service.save(model);

            //THEN
            verify(lunaticJsonRawDataPersistencePort).save(model);
        }
    }

    @Nested
    @DisplayName("getRawDataByQuestionnaireId()")
    class GetRawDataByQuestionnaireIdTests {

        @Test
        @DisplayName("Calls persistence port and returns result")
        void returns_result_from_port() {
            //GIVEN
            LunaticJsonRawDataModel model = buildRawDataWithCollected(Map.of());
            when(lunaticJsonRawDataPersistencePort
                    .findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, Mode.WEB, List.of(INTERROGATION_ID)))
                    .thenReturn(List.of(model));

            //WHEN
            List<LunaticJsonRawDataModel> result =
                    service.getRawDataByQuestionnaireId(QUESTIONNAIRE_ID, Mode.WEB, List.of(INTERROGATION_ID));

            //THEN
            assertThat(result).containsExactly(model);
        }
    }

    @Nested
    @DisplayName("getRawDataByInterrogationId()")
    class GetRawDataByInterrogationIdTests {

        @Test
        @DisplayName("Calls persistence port")
        void delegates_to_port() {
            //GIVEN
            LunaticJsonRawDataModel lunaticJsonRawDataModel = buildRawDataWithCollected(Map.of());
            when(lunaticJsonRawDataPersistencePort.findRawDataByInterrogationId(INTERROGATION_ID))
                    .thenReturn(List.of(lunaticJsonRawDataModel));

            //WHEN
            List<LunaticJsonRawDataModel> result = service.getRawDataByInterrogationId(INTERROGATION_ID);

            //THEN
            assertThat(result).containsExactly(lunaticJsonRawDataModel);
        }
    }

    @Nested
    @DisplayName("processRawData(questionnaireId)")
    class ProcessRawDataNewTests {

        @BeforeEach
        void commonSetup() throws GenesisException {
            when(config.getRawDataProcessingBatchSize()).thenReturn(100);
            when(controllerUtils.getModesList(QUESTIONNAIRE_ID, null)).thenReturn(List.of(Mode.WEB));
            when(dataProcessingContextService.getContextByCollectionInstrumentId(QUESTIONNAIRE_ID)).thenReturn(null);
        }

        @Test
        @DisplayName("Returns zero counts when no unprocessed ids")
        void noUnprocessedIds_returnsZeroCounts() throws GenesisException {
            //GIVEN
            MetadataModel metadataModel = new MetadataModel();
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenReturn(metadataModel);
            when(lunaticJsonRawDataPersistencePort
                    .findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of());

            //WHEN
            DataProcessResult result = service.processRawData(QUESTIONNAIRE_ID);

            //THEN
            assertThat(result.dataCount()).isZero();
            assertThat(result.formattedDataCount()).isZero();
            verify(surveyUnitService, never()).saveSurveyUnits(any());
        }

        @Test
        @DisplayName("Processes one batch and saves survey units")
        void processOneBatch_savesSurveyUnits() throws GenesisException {
            //GIVEN
            MetadataModel metadataModel = new MetadataModel();
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenReturn(metadataModel);
            when(lunaticJsonRawDataPersistencePort
                    .findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of(INTERROGATION_ID));
            when(lunaticJsonRawDataPersistencePort
                    .findRawDataByQuestionnaireId(eq(QUESTIONNAIRE_ID), eq(Mode.WEB), anyList()))
                    .thenReturn(List.of());

            DataProcessResult result = service.processRawData(QUESTIONNAIRE_ID);

            assertThat(result.dataCount()).isZero(); // empty raw data → no models
            verify(surveyUnitService, atLeastOnce()).saveSurveyUnits(anyList());
        }

        @Test
        @DisplayName("Throws GenesisException when metadata cannot be loaded")
        void missingMetadata_throwsGenesisException() throws GenesisException {
            // GIVEN
            // Null variable map + genesis error in list
            MetadataModel metadataModel = mock(MetadataModel.class);
            when(metadataModel.getVariables()).thenReturn(null);
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenAnswer(inv -> {
                        List<Object> errors = inv.getArgument(4);
                        errors.add(new GenesisError("test"));
                        return metadataModel;
                    });

            //WHEN + THEN
            assertThatThrownBy(() -> service.processRawData(QUESTIONNAIRE_ID))
                    .isInstanceOf(GenesisException.class);
        }

        @Test
        @DisplayName("Sends processed ids to quality tool when review is activated")
        void withReview_sendsProcessedIds() throws GenesisException, IOException {
            // GIVEN
            // Context with review
            DataProcessingContextModel context = mock(DataProcessingContextModel.class);
            when(context.isWithReview()).thenReturn(true);
            when(dataProcessingContextService.getContextByCollectionInstrumentId(QUESTIONNAIRE_ID))
                    .thenReturn(context);

            MetadataModel metadataModel = new MetadataModel();
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenReturn(metadataModel);

            when(lunaticJsonRawDataPersistencePort
                    .findUnprocessedInterrogationIdsByCollectionInstrumentId(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of(INTERROGATION_ID));

            // Build raw data with actual COLLECTED variable so it produces a SurveyUnitModel
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "value1");
            varInner.put("EDITED", null);
            Map<String, Object> collected = new HashMap<>();
            collected.put("VAR1", varInner);
            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(collected);

            when(lunaticJsonRawDataPersistencePort
                    .findRawDataByQuestionnaireId(eq(QUESTIONNAIRE_ID), eq(Mode.WEB), anyList()))
                    .thenReturn(List.of(rawData));

            //Mock response from quality tool
            ResponseEntity<Object> fakeResponse = ResponseEntity.ok().build();
            when(surveyUnitQualityToolPort.sendProcessedIds(anyMap())).thenReturn(fakeResponse);

            //WHEN
            service.processRawData(QUESTIONNAIRE_ID);

            //THEN
            verify(surveyUnitQualityToolPort, times(1)).sendProcessedIds(anyMap());
        }
    }

    @Nested
    @DisplayName("convertRawData()")
    class ConvertRawDataTests {

        @Test
        @DisplayName("Empty raw data list returns empty list")
        void emptyRawDataList_returnsEmpty() {
            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(), new VariablesMap());

            //THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Raw data with no COLLECTED/EXTERNAL variables is ignored")
        void noVariables_rawDataIgnored() {
            //GIVEN
            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(Map.of());

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            assertThat(result).isEmpty();
            verify(lunaticJsonRawDataPersistencePort, atLeastOnce()).updateProcessDates(eq(CAMPAIGN_ID), anySet());
        }

        @Test
        @DisplayName("Raw data with COLLECTED variable produces COLLECTED and EDITED models")
        void withCollectedVariable_producesBothDataStates() {
            //GIVEN
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "val");
            varInner.put("EDITED", null);
            Map<String, Object> collected = new HashMap<>();
            collected.put("VAR1", varInner);

            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(collected);

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            // Expect 1 COLLECTED (VAR1 has a value) and 0 EDITED (EDITED value is null → empty)
            long collectedCount = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED).count();
            assertThat(collectedCount).isEqualTo(1);
            long editedCount = result.stream()
                    .filter(m -> m.getState() == DataState.EDITED).count();
            assertThat(editedCount).isZero();
        }

        @Test
        @DisplayName("EDITED value is extracted when present")
        void withEditedVariable_producesEditedModel() {
            //GIVEN
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "original");
            varInner.put("EDITED", "edited");
            Map<String, Object> collected = new HashMap<>();
            collected.put("VAR1", varInner);

            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(collected);

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            long editedCount = result.stream()
                    .filter(m -> m.getState() == DataState.EDITED).count();
            assertThat(editedCount).isEqualTo(1);
        }

        @Test
        @DisplayName("FILIERE model type is detected when 'data' key is present")
        void filiereModelType_detected() {
            //GIVEN
            // Wrap data in "data" key to simulate FILIERE type
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "val");
            varInner.put("EDITED", null);
            Map<String, Object> inner = new HashMap<>();
            inner.put("VAR1", varInner);
            Map<String, Object> collected = new HashMap<>();
            collected.put("COLLECTED", inner);
            Map<String, Object> outerData = new HashMap<>();
            outerData.put("data", collected); // triggers FILIERE detection

            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(outerData);

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            // Should not throw — FILIERE path is followed TODO More asserts
            assertThat(result).isNotNull();
        }

        @Test
        @DisplayName("validationDate and isCapturedIndirectly are mapped correctly")
        void optionalFields_mappedCorrectly() {
            //GIVEN
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "val");
            varInner.put("EDITED", null);
            Map<String, Object> collected = new HashMap<>();
            collected.put("VAR1", varInner);

            Map<String, Object> data = new HashMap<>();
            data.put("COLLECTED", collected);
            data.put("validationDate", "2024-01-15T10:00:00");
            data.put("isCapturedIndirectly", "true");

            LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                    .campaignId(CAMPAIGN_ID)
                    .questionnaireId(QUESTIONNAIRE_ID)
                    .interrogationId(INTERROGATION_ID)
                    .idUE("testIdUE")
                    .mode(Mode.WEB)
                    .data(data)
                    .recordDate(LocalDateTime.now())
                    .build();

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            assertThat(result).isNotEmpty();
            SurveyUnitModel model = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .findFirst().orElseThrow();
            assertThat(model.getValidationDate()).isEqualTo(LocalDateTime.parse("2024-01-15T10:00:00"));
            assertThat(model.getIsCapturedIndirectly()).isTrue();
        }

        @Test
        @DisplayName("Malformed validationDate falls back to null without throwing")
        void malformedValidationDate_fallsBackToNull() {
            //GIVEN
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", "val");
            varInner.put("EDITED", null);
            Map<String, Object> collected = new HashMap<>();
            collected.put("VAR1", varInner);

            Map<String, Object> data = new HashMap<>();
            data.put("COLLECTED", collected);
            data.put("validationDate", "not-a-date");

            LunaticJsonRawDataModel rawData = LunaticJsonRawDataModel.builder()
                    .campaignId(CAMPAIGN_ID)
                    .questionnaireId(QUESTIONNAIRE_ID)
                    .interrogationId(INTERROGATION_ID)
                    .idUE("idUE-1")
                    .mode(Mode.WEB)
                    .data(data)
                    .recordDate(LocalDateTime.now())
                    .build();

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            assertThat(result).isNotEmpty();
            result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .forEach(m -> assertThat(m.getValidationDate()).isNull());
        }

        @Test
        @DisplayName("Array values are converted to multiple VariableModels")
        void arrayValues_convertedToMultipleIterations() {
            //GIVEN
            Map<String, Object> varInner = new HashMap<>();
            varInner.put("COLLECTED", List.of("a", "b", "c"));
            varInner.put("EDITED", null);
            Map<String, Object> collected = new HashMap<>();
            collected.put("ARRAY_VAR", varInner);

            LunaticJsonRawDataModel rawData = buildRawDataWithCollected(collected);

            //WHEN
            List<SurveyUnitModel> result = service.convertRawData(List.of(rawData), new VariablesMap());

            //THEN
            SurveyUnitModel collectedModel = result.stream()
                    .filter(m -> m.getState() == DataState.COLLECTED)
                    .findFirst().orElseThrow();
            assertThat(collectedModel.getCollectedVariables()).hasSize(3);
        }
    }

    @Nested
    @DisplayName("getUnprocessedDataIds()")
    class GetUnprocessedDataIdsTests {

        @Test
        @DisplayName("Returns DTOs for each interrogation id")
        void returnsDtos() {
            //GIVEN
            GroupedInterrogation grouped = new GroupedInterrogation(
                    QUESTIONNAIRE_ID, null, List.of("id1", "id2"));
            when(lunaticJsonRawDataPersistencePort.findUnprocessedIds()).thenReturn(List.of(grouped));

            //WHEN
            List<LunaticJsonRawDataUnprocessedDto> result = service.getUnprocessedDataIds();

            //THEN
            assertThat(result).hasSize(2);
            assertThat(result.get(0).interrogationId()).isEqualTo("id1");
            assertThat(result.get(1).interrogationId()).isEqualTo("id2");
        }

        @Test
        @DisplayName("Returns empty list when no unprocessed ids")
        void noUnprocessed_returnsEmpty() {
            //GIVEN
            when(lunaticJsonRawDataPersistencePort.findUnprocessedIds()).thenReturn(List.of());

            //WHEN + THEN
            assertThat(service.getUnprocessedDataIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("getUnprocessedDataQuestionnaireIds()")
    class GetUnprocessedDataQuestionnaireIdsTests {

        @Test
        @DisplayName("Excludes questionnaire with no modes")
        void noModes_excluded() {
            //GIVEN
            when(lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIdsByNullProcessDate())
                    .thenReturn(Set.of(QUESTIONNAIRE_ID));
            when(lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of());

            //WHEN + THEN
            assertThat(service.getUnprocessedDataQuestionnaireIds()).isEmpty();
        }

        @Test
        @DisplayName("Includes questionnaire when specs are present for all modes")
        void specsPresent_included() throws GenesisException {
            //GIVEN
            when(lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIdsByNullProcessDate())
                    .thenReturn(Set.of(QUESTIONNAIRE_ID));
            when(lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of(Mode.WEB));

            MetadataModel metadataModel = new MetadataModel();
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenReturn(metadataModel);

            //WHEN
            Set<String> result = service.getUnprocessedDataQuestionnaireIds();

            //THEN
            assertThat(result).containsExactly(QUESTIONNAIRE_ID);
        }

        @Test
        @DisplayName("Excludes questionnaire when GenesisException is thrown during spec load")
        void genesisException_excluded() throws GenesisException {
            //GIVEN
            when(lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIdsByNullProcessDate())
                    .thenReturn(Set.of(QUESTIONNAIRE_ID));
            when(lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of(Mode.WEB));
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenThrow(new GenesisException(500, "error"));

            //WHEN + THEN
            assertThat(service.getUnprocessedDataQuestionnaireIds()).isEmpty();
        }

        @Test
        @DisplayName("Excludes questionnaire when metadata model is null")
        void nullMetadataModel_excluded() throws GenesisException {
            //GIVEN
            when(lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIdsByNullProcessDate())
                    .thenReturn(Set.of(QUESTIONNAIRE_ID));
            when(lunaticJsonRawDataPersistencePort.findModesByQuestionnaire(QUESTIONNAIRE_ID))
                    .thenReturn(Set.of(Mode.WEB));
            when(metadataService.loadAndSaveIfNotExists(anyString(), anyString(), any(), any(), any()))
                    .thenReturn(null);

            //WHEN + THEN
            assertThat(service.getUnprocessedDataQuestionnaireIds()).isEmpty();
        }
    }

    @Nested
    @DisplayName("updateProcessDates()")
    class UpdateProcessDatesTests {

        @Test
        @DisplayName("Groups interrogation ids by campaign and calls port once per campaign")
        void groupsByCampaignId() {
            //GIVEN
            SurveyUnitModel m1 = SurveyUnitModel.builder()
                    .campaignId(CAMPAIGN_ID).interrogationId("id1").build();
            SurveyUnitModel m2 = SurveyUnitModel.builder()
                    .campaignId(CAMPAIGN_ID).interrogationId("id2").build();
            SurveyUnitModel m3 = SurveyUnitModel.builder()
                    .campaignId("campaign-2").interrogationId("id3").build();

            //WHEN
            service.updateProcessDates(List.of(m1, m2, m3));

            //THEN
            verify(lunaticJsonRawDataPersistencePort).updateProcessDates(eq(CAMPAIGN_ID), argThat(s -> s.containsAll(Set.of("id1", "id2"))));
            verify(lunaticJsonRawDataPersistencePort).updateProcessDates(eq("campaign-2"), argThat(s -> s.contains("id3")));
        }

        @Test
        @DisplayName("Does nothing for empty list")
        void emptyList_noCalls() {
            //WHEN
            service.updateProcessDates(List.of());

            //THEN
            verifyNoInteractions(lunaticJsonRawDataPersistencePort);
        }
    }

    @Nested
    @DisplayName("findDistinctQuestionnaireIds()")
    class FindDistinctQuestionnaireIdsTests {

        @Test
        @DisplayName("Call persistence port")
        void delegates_to_port() {
            //WHEN
            when(lunaticJsonRawDataPersistencePort.findDistinctQuestionnaireIds())
                    .thenReturn(Set.of("q1", "q2"));

            //THEN
            assertThat(service.findDistinctQuestionnaireIds()).containsExactlyInAnyOrder("q1", "q2");
        }
    }

    @Nested
    @DisplayName("countRawResponsesByQuestionnaireId()")
    class CountRawResponsesTests {

        @Test
        @DisplayName("Returns count from persistence port")
        void returnsCount() {
            //WHEN
            when(lunaticJsonRawDataPersistencePort.countRawResponsesByQuestionnaireId(QUESTIONNAIRE_ID))
                    .thenReturn(42L);

            //THEN
            assertThat(service.countRawResponsesByQuestionnaireId(QUESTIONNAIRE_ID)).isEqualTo(42L);
        }
    }

    @Nested
    @DisplayName("findProcessedIdsgroupedByQuestionnaireSince()")
    class FindProcessedIdsSinceTests {

        @Test
        @DisplayName("Filters out questionnaires without review context")
        void noReview_filtered() {
            //GIVEN
            LocalDateTime since = LocalDateTime.now().minusDays(1);
            GroupedInterrogation group = new GroupedInterrogation(CAMPAIGN_ID, QUESTIONNAIRE_ID, List.of("id1"));
            when(lunaticJsonRawDataPersistencePort.findProcessedIdsGroupedByQuestionnaireSince(since))
                    .thenReturn(List.of(group));

            DataProcessingContextModel dataProcessingContextModel = mock(DataProcessingContextModel.class);
            when(dataProcessingContextModel.isWithReview()).thenReturn(false);
            when(dataProcessingContextModel.getPartitionId()).thenReturn(CAMPAIGN_ID);
            when(dataProcessingContextPersistancePort.findByPartitionIds(List.of(CAMPAIGN_ID)))
                    .thenReturn(List.of(dataProcessingContextModel));

            //WHEN
            Map<String, List<String>> result = service.findProcessedIdsgroupedByQuestionnaireSince(since);

            //THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Includes questionnaires with review context")
        void withReview_included() {
            //GIVEN
            LocalDateTime since = LocalDateTime.now().minusDays(1);
            GroupedInterrogation groupedInterrogation = new GroupedInterrogation(QUESTIONNAIRE_ID, CAMPAIGN_ID, List.of("id1"));
            when(lunaticJsonRawDataPersistencePort.findProcessedIdsGroupedByQuestionnaireSince(since))
                    .thenReturn(List.of(groupedInterrogation));

            DataProcessingContextModel dataProcessingContextModel = DataProcessingContextModel.builder()
                    .partitionId(CAMPAIGN_ID)
                    .collectionInstrumentId(QUESTIONNAIRE_ID)
                    .withReview(true)
                    .build();
            when(dataProcessingContextPersistancePort.findByPartitionIds(List.of(CAMPAIGN_ID)))
                    .thenReturn(List.of(dataProcessingContextModel));

            //WHEN
            Map<String, List<String>> result = service.findProcessedIdsgroupedByQuestionnaireSince(since);

            //THEN
            assertThat(result).containsKey(QUESTIONNAIRE_ID);
            assertThat(result.get(QUESTIONNAIRE_ID)).containsExactly("id1");
        }
    }

    @Nested
    @DisplayName("findRawDataByQuestionnaireId(questionnaireId, pageable)")
    class FindRawDataPageableTests {

        @Test
        @DisplayName("Returns page from persistence port")
        void returnsPage() {
            //GIVEN
            Pageable pageable = PageRequest.of(0, 10);
            LunaticJsonRawDataModel model = buildRawDataWithCollected(Map.of());
            Page<LunaticJsonRawDataModel> page = new PageImpl<>(List.of(model));
            when(lunaticJsonRawDataPersistencePort.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, pageable))
                    .thenReturn(page);

            //WHEN
            Page<LunaticJsonRawDataModel> result =
                    service.findRawDataByQuestionnaireId(QUESTIONNAIRE_ID, pageable);

            //THEN
            assertThat(result.getContent()).containsExactly(model);
        }
    }

    @Nested
    @DisplayName("getValueString() util")
    class GetValueStringTests {

        @Test
        @DisplayName("Double value strips trailing zeros")
        void doubleStripsTrailingZeros() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString(1.50)).isEqualTo("1.5");
        }

        @Test
        @DisplayName("Float value strips trailing zeros")
        void floatStripsTrailingZeros() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString(1.500f)).isEqualTo("1.5");
        }

        @Test
        @DisplayName("Integer value returns plain string")
        void integerReturnsPlainString() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString(42)).isEqualTo("42");
        }

        @Test
        @DisplayName("String value returns same string")
        void stringReturnsItself() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString("hello")).isEqualTo("hello");
        }

        @Test
        @DisplayName("Null returns 'null' string")
        void nullReturnsNullString() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString(null)).isEqualTo("null");
        }

        @Test
        @DisplayName("BigDecimal integer-like double has no decimal point")
        void bigDecimalIntegerDouble() {
            //WHEN + THEN
            assertThat(LunaticJsonRawDataService.getValueString(3.0)).isEqualTo("3");
        }
    }

    //UTILS
    /** Builds a minimal raw data document with COLLECTED data. */
    private LunaticJsonRawDataModel buildRawDataWithCollected(Map<String, Object> collectedData) {
        Map<String, Object> data = new HashMap<>();
        data.put("COLLECTED", collectedData);

        return LunaticJsonRawDataModel.builder()
                .campaignId(CAMPAIGN_ID)
                .questionnaireId(QUESTIONNAIRE_ID)
                .interrogationId(INTERROGATION_ID)
                .idUE("idUE-1")
                .mode(Mode.WEB)
                .data(data)
                .recordDate(LocalDateTime.now())
                .build();
    }
}