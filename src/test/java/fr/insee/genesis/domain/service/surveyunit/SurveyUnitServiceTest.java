package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.SurveyUnitInputDto;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
import fr.insee.genesis.controller.dto.VariableInputDto;
import fr.insee.genesis.controller.dto.VariableStateInputDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.InterrogationInfo;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.exceptions.NoDataException;
import fr.insee.genesis.exceptions.QuestionnaireNotFoundException;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

class SurveyUnitServiceTest {

    static SurveyUnitService surveyUnitService;
    static SurveyUnitPersistencePort surveyUnitPersistencePortStub;
    static QuestionnaireMetadataService questionnaireMetadataServiceStub;

    @BeforeEach
    void init() {
        surveyUnitPersistencePortStub = mock(SurveyUnitPersistencePort.class);
        questionnaireMetadataServiceStub = mock(QuestionnaireMetadataService.class);
        surveyUnitService = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                questionnaireMetadataServiceStub,
                new FileUtils(TestConstants.getConfigStub())
        );
    }

    @Test
    void get_latest_should_return_usualSurveyId() {
        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(TestConstants.DEFAULT_SURVEY_UNIT_ID);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());
        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        assertThat(surveyUnitModels).isNotNull().hasSize(1);
        assertThat(surveyUnitModels.getFirst().getUsualSurveyUnitId()).isEqualTo(TestConstants.DEFAULT_SURVEY_UNIT_ID);
    }

    @Test
    void get_latest_should_return_state_in_collected_variables(){
        //GIVEN
        String variableId = "testVar";
        DataState state = DataState.COLLECTED;

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setState(String.valueOf(state));
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(TestConstants.DEFAULT_SURVEY_UNIT_ID);

        VariableDocument variableDocument = new VariableDocument();
        variableDocument.setVarId(variableId);
        variableDocument.setIteration(1);
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(variableDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());
        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        //WHEN
        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        Assertions.assertThat(surveyUnitModels).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().getFirst().state())
                .isEqualTo(state);
    }

    @Test
    void get_latest_should_return_state_in_external_variables(){
        //GIVEN
        String variableId = "testVar";
        DataState state = DataState.COLLECTED;

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setState(String.valueOf(state));
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(TestConstants.DEFAULT_SURVEY_UNIT_ID);

        VariableDocument variableDocument = new VariableDocument();
        variableDocument.setVarId(variableId);
        variableDocument.setIteration(1);
        surveyUnitDocument.setExternalVariables(new ArrayList<>());
        surveyUnitDocument.getExternalVariables().add(variableDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());
        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        //WHEN
        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        Assertions.assertThat(surveyUnitModels).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().state())
                .isEqualTo(state);
    }

    @Test
    @SneakyThrows
    void get_simplified_should_return_usualSurveyId(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();
        String usualSurveyUnitId = "test";

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(Instant.now().minusSeconds(60));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(Instant.now());
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1EDITED");
        surveyUnitDocuments.add(surveyUnitDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        //WHEN
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto = surveyUnitService.findSimplified(
                "test",
                "testInterrogation",
                Mode.WEB,
                Instant.now()
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getUsualSurveyUnitId()).isNotNull().isEqualTo(usualSurveyUnitId);
    }

    @Test
    @SneakyThrows
    void get_simplified_should_return_latest_state_in_collected_variable(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(Instant.now().minusSeconds(60));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(Instant.now());
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1EDITED");
        surveyUnitDocuments.add(surveyUnitDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        //WHEN
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto = surveyUnitService.findSimplified(
                "test",
                "testInterrogation",
                Mode.WEB,
                Instant.now()
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate().getFirst().state()).isEqualTo(DataState.EDITED);
    }

    @Test
    @SneakyThrows
    void get_simplified_should_return_latest_state_in_external_variable(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(Instant.now().minusSeconds(60));
        surveyUnitDocument.setExternalVariables(new ArrayList<>());
        surveyUnitDocument.getExternalVariables().add(new VariableDocument());
        surveyUnitDocument.getExternalVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getExternalVariables().getFirst().setIteration(1);
        surveyUnitDocument.getExternalVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.FORCED.toString());
        surveyUnitDocument.setRecordDate(Instant.now());
        surveyUnitDocument.setExternalVariables(new ArrayList<>());
        surveyUnitDocument.getExternalVariables().add(new VariableDocument());
        surveyUnitDocument.getExternalVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getExternalVariables().getFirst().setIteration(1);
        surveyUnitDocument.getExternalVariables().getFirst().setValue("VAR1FORCED");
        surveyUnitDocuments.add(surveyUnitDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        //WHEN
        SurveyUnitSimplifiedDto surveyUnitSimplifiedDto = surveyUnitService.findSimplified(
                "test",
                "testInterrogation",
                Mode.WEB,
                Instant.now()
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables().getFirst().state()).isEqualTo(DataState.FORCED);
    }

    @Test
    void get_simplified_should_throw_genesis_exception_if_not_found(){
        try {
            surveyUnitService.findSimplified(
                    "test",
                    "testInterrogation",
                    Mode.WEB,
                    Instant.now()
            );
            Assertions.fail();
        }catch (NoDataException e){
            Assertions.assertThat(e.getMessage()).contains("No response found for interrogation ");
        }
    }


    @Test
    @SuppressWarnings("deprecation")
    void get_latest_should_return_usualSurveyId_when_idue() {
        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setIdUE(TestConstants.DEFAULT_SURVEY_UNIT_ID);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());
        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(Collections.singletonList(surveyUnitDocument)))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        assertThat(surveyUnitModels).isNotNull().hasSize(1);
        assertThat(surveyUnitModels.getFirst().getUsualSurveyUnitId()).isEqualTo(TestConstants.DEFAULT_SURVEY_UNIT_ID);
    }

    @Test
    @SuppressWarnings("deprecation")
    void get_latest_should_return_edited() {
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setIdUE(TestConstants.DEFAULT_SURVEY_UNIT_ID);
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1).toInstant(ZoneOffset.UTC));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(Instant.now());
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR2");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR2");
        surveyUnitDocuments.add(surveyUnitDocument);

        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());
        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findByIds(any(), any());

        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        assertThat(surveyUnitModels).isNotNull().hasSize(2);
    }

    @Test
    void countResponsesByCollectionInstrumentId_test() {
        long exampleCount = 200;
        doReturn(exampleCount).when(surveyUnitPersistencePortStub).countByCollectionInstrumentId(any());

        assertThat(surveyUnitService.countResponsesByCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .isEqualTo(exampleCount);
    }

    @Test
    @DisplayName("saveSurveyUnits should call persistance port")
    void saveSurveyUnits_shouldDelegateToPersistencePort() {
        // GIVEN
        List<SurveyUnitModel> models = List.of(mock(SurveyUnitModel.class));

        // WHEN
        surveyUnitService.saveSurveyUnits(models);

        // THEN
        verify(surveyUnitPersistencePortStub).saveAll(models);
    }

    @Test
    @DisplayName("findByInterrogationId should call persistance port")
    void findByInterrogationId_shouldDelegateToPersistencePort() {
        // GIVEN
        List<SurveyUnitModel> expected = List.of(mock(SurveyUnitModel.class));
        doReturn(expected).when(surveyUnitPersistencePortStub).findByInterrogationId(any());

        // WHEN
        List<SurveyUnitModel> result = surveyUnitService.findByInterrogationId(TestConstants.DEFAULT_INTERROGATION_ID);

        // THEN
        assertThat(result).isEqualTo(expected);
    }

    @Nested
    @DisplayName("findDistinctInterrogationIds tests")
    class FindDistinctInterrogationIdsTests {

        @Test
        @DisplayName("findDistinctInterrogationIdsByQuestionnaireId should return distinct ids")
        void findDistinctInterrogationIdsByQuestionnaireId_shouldReturnDistinctIds() {
            // GIVEN
            SurveyUnitDocument doc1 = buildDoc("INTERRO1", Mode.WEB);
            SurveyUnitDocument doc2 = buildDoc("INTERRO1", Mode.WEB); // doublon
            SurveyUnitDocument doc3 = buildDoc("INTERRO2", Mode.WEB);
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(doc1, doc2, doc3)))
                    .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());

            // WHEN
            List<InterrogationId> result = surveyUnitService.findDistinctInterrogationIdsByQuestionnaireId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
            );

            // THEN
            assertThat(result).hasSize(2)
                    .extracting(InterrogationId::getInterrogationId)
                    .containsExactlyInAnyOrder("INTERRO1", "INTERRO2");
        }

        @Test
        @DisplayName("searchInterrogations should return distinct ids")
        void searchInterrogations_shouldReturnDistinctIds() {
            // GIVEN
            Instant since = Instant.now().minusSeconds(86400); //Minus one day
            InterrogationInfo info1 = new InterrogationInfo("INTERRO1",Instant.parse("2025-04-20T10:15:30Z"));
            InterrogationInfo info2 = new InterrogationInfo("INTERRO1", Instant.parse("2025-04-20T10:15:30Z")); // doublon
            doReturn(List.of(info1, info2)).when(surveyUnitPersistencePortStub).searchInterrogations(any(), any(), any());

            // WHEN
            List<InterrogationInfo> result = surveyUnitService.searchInterrogations(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, since, null
            );

            // THEN
            assertThat(result).hasSize(1)
                    .extracting(InterrogationInfo::interrogationId)
                    .containsExactly("INTERRO1");
        }

    }

    @Nested
    @DisplayName("findModes tests")
    class FindModesTests {

        @Test
        @DisplayName("findModesByCollectionInstrumentId should return distinct modes")
        void findModesByCollectionInstrumentId_shouldReturnDistinctModes() {
            // GIVEN
            SurveyUnitDocument webDoc = buildDoc("INTERRO1", Mode.WEB);
            SurveyUnitDocument f2fDoc = buildDoc("INTERRO2", Mode.F2F);
            SurveyUnitDocument webDoc2 = buildDoc("INTERRO3", Mode.WEB);
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(webDoc, f2fDoc, webDoc2)))
                    .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());

            // WHEN
            List<Mode> result = surveyUnitService.findModesByCollectionInstrumentId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
            );

            // THEN
            assertThat(result).hasSize(2).containsExactlyInAnyOrder(Mode.WEB, Mode.F2F);
        }

        @Test
        @DisplayName("findModesByCollectionInstrumentId should throw QuestionnaireNotFoundException if not found")
        void findModesByCollectionInstrumentId_shouldThrow_whenEmpty() {
            // GIVEN
            doReturn(Collections.emptyList())
                    .when(surveyUnitPersistencePortStub).findInterrogationIdsByCollectionInstrumentId(any());

            // WHEN + THEN
            assertThatThrownBy(() -> surveyUnitService.findModesByCollectionInstrumentId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
            )).isInstanceOf(QuestionnaireNotFoundException.class);
        }

        @Test
        @DisplayName("findModesByQuestionnaireIdV2 should return distinct modes")
        void findModesByQuestionnaireIdV2_shouldReturnDistinctModes() {
            // GIVEN
            SurveyUnitDocument webDoc = buildDoc("INTERRO1", Mode.WEB);
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(webDoc)))
                    .when(surveyUnitPersistencePortStub).findModesByQuestionnaireIdV2(any());

            // WHEN
            List<Mode> result = surveyUnitService.findModesByQuestionnaireIdV2(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
            );

            // THEN
            assertThat(result).containsExactly(Mode.WEB);
        }
    }

    @Test
    @DisplayName("deleteByCollectionInstrumentId should return deleted documents count")
    void deleteByCollectionInstrumentId_shouldReturnDeleteCount() {
        // GIVEN
        doReturn(42L).when(surveyUnitPersistencePortStub).deleteByCollectionInstrumentId(any());

        // WHEN
        Long result = surveyUnitService.deleteByCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);

        // THEN
        assertThat(result).isEqualTo(42L);
    }

    @Test
    @DisplayName("countResponses should call persistance port")
    void countResponses_shouldDelegateToPersistencePort() {
        // GIVEN
        doReturn(500L).when(surveyUnitPersistencePortStub).count();

        // WHEN + THEN
        assertThat(surveyUnitService.countResponses()).isEqualTo(500L);
    }

    @Test
    @DisplayName("countResponsesByQuestionnaireId should call persistance port")
    void countResponsesByQuestionnaireId_shouldDelegateToPersistencePort() {
        // GIVEN
        doReturn(77L).when(surveyUnitPersistencePortStub).countByQuestionnaireId(any());

        // WHEN + THEN
        assertThat(surveyUnitService.countResponsesByQuestionnaireId(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
        ).isEqualTo(77L);
    }

    @Nested
    @DisplayName("findQuestionnaireIdByInterrogationId tests")
    class FindQuestionnaireIdByInterrogationIdTests {

        @Test
        @DisplayName("Should return questionnaireId if unique")
        void findQuestionnaireIdByInterrogationId_shouldReturnQuestionnaireId() throws GenesisException {
            // GIVEN
            SurveyUnitDocument doc = buildDoc(TestConstants.DEFAULT_INTERROGATION_ID, Mode.WEB);
            doc.setQuestionnaireId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(doc)))
                    .when(surveyUnitPersistencePortStub).findByInterrogationId(any());

            // WHEN
            String result = surveyUnitService.findQuestionnaireIdByInterrogationId(
                    TestConstants.DEFAULT_INTERROGATION_ID
            );

            // THEN
            assertThat(result).isEqualTo(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        }

        @Test
        @DisplayName("Should throw GenesisException 404 if no response found")
        void findQuestionnaireIdByInterrogationId_shouldThrow404_whenEmpty() {
            // GIVEN
            doReturn(Collections.emptyList())
                    .when(surveyUnitPersistencePortStub).findByInterrogationId(any());

            // WHEN + THEN
            assertThatThrownBy(() -> surveyUnitService.findQuestionnaireIdByInterrogationId(
                    TestConstants.DEFAULT_INTERROGATION_ID
            ))
                    .isInstanceOf(GenesisException.class)
                    .hasMessageContaining(TestConstants.DEFAULT_INTERROGATION_ID);
        }

        @Test
        @DisplayName("Should throw GenesisException 207 if multiple questionnaires found")
        void findQuestionnaireIdByInterrogationId_shouldThrow207_whenMultipleQuestionnaires() {
            // GIVEN
            SurveyUnitDocument doc1 = buildDoc(TestConstants.DEFAULT_INTERROGATION_ID, Mode.WEB);
            doc1.setQuestionnaireId("QUEST1");
            SurveyUnitDocument doc2 = buildDoc(TestConstants.DEFAULT_INTERROGATION_ID, Mode.WEB);
            doc2.setQuestionnaireId("QUEST2");
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(doc1, doc2)))
                    .when(surveyUnitPersistencePortStub).findByInterrogationId(any());

            // WHEN + THEN
            assertThatThrownBy(() -> surveyUnitService.findQuestionnaireIdByInterrogationId(
                    TestConstants.DEFAULT_INTERROGATION_ID
            )).isInstanceOf(GenesisException.class);
        }
    }

    @Nested
    @DisplayName("findDistinctPageableInterrogationIdsByQuestionnaireId tests")
    class FindDistinctPageableInterrogationIdsTests {

        @Test
        @DisplayName("Should return empty list if out of bounds page")
        void findDistinctPageable_shouldReturnEmpty_whenPageOutOfBounds() {
            // GIVEN
            doReturn(10L).when(surveyUnitPersistencePortStub).countByCollectionInstrumentId(any());

            // WHEN - page 5 avec blockSize 3 => skip=15 > totalSize=10
            List<InterrogationId> result = surveyUnitService.findDistinctPageableInterrogationIdsByQuestionnaireId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, 0, 3, 5
            );

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should return empty list if negative page")
        void findDistinctPageable_shouldReturnEmpty_whenNegativePage() {
            // WHEN
            List<InterrogationId> result = surveyUnitService.findDistinctPageableInterrogationIdsByQuestionnaireId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, 10, 3, -1
            );

            // THEN
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("Should use provided totalSize")
        void findDistinctPageable_shouldUseTotalSizeParam_whenProvided() {
            // GIVEN
            SurveyUnitDocument doc = buildDoc("INTERRO1", Mode.WEB);
            doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(List.of(doc)))
                    .when(surveyUnitPersistencePortStub).findPageableInterrogationIdsByQuestionnaireId(any(), any(), any());

            // WHEN
            List<InterrogationId> result = surveyUnitService.findDistinctPageableInterrogationIdsByQuestionnaireId(
                    TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID, 100, 10, 0
            );

            // THEN
            verify(surveyUnitPersistencePortStub, never()).countByCollectionInstrumentId(any());
            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("parseEditedVariables tests")
    class ParseEditedVariablesTests {

        @Test
        @DisplayName("Should throw GenesisException if COLLECTED state")
        void parseEditedVariables_shouldThrow_whenCollectedStateReceived() {
            // GIVEN
            VariableStateInputDto variableStateInputDto = VariableStateInputDto.builder()
                    .state(DataState.COLLECTED)
                    .value("someValue")
                    .build();

            VariableInputDto variableInputDto = VariableInputDto.builder()
                    .variableName("VAR1")
                    .variableStateInputDto(variableStateInputDto)
                    .iteration(1)
                    .build();

            SurveyUnitInputDto inputDto = SurveyUnitInputDto.builder()
                    .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                    .questionnaireId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                    .mode(Mode.WEB)
                    .collectedVariables(List.of(variableInputDto))
                    .build();

            // WHEN + THEN
            assertThatThrownBy(() -> surveyUnitService.parseEditedVariables(
                    inputDto, "user1", new VariablesMap()
            )).isInstanceOf(GenesisException.class)
                    .hasMessageContaining("COLLECTED");
        }

        @Test
        @DisplayName("Should create one SurveyUnitModel for each state")
        void parseEditedVariables_shouldCreateOneModelPerDistinctState() throws GenesisException {
            // GIVEN
            VariablesMap variablesMap = buildVariablesMapWithVar();
            SurveyUnitInputDto inputDto = buildInputDtoWithStates(
                    List.of(DataState.EDITED, DataState.FORCED)
            );

            // WHEN
            List<SurveyUnitModel> result = surveyUnitService.parseEditedVariables(inputDto, "user1", variablesMap);

            // THEN
            assertThat(result).hasSize(2);
            assertThat(result)
                    .extracting(SurveyUnitModel::getState)
                    .containsExactlyInAnyOrder(DataState.EDITED, DataState.FORCED);
        }

        @Test
        @DisplayName("modifiedBy should be filled")
        void parseEditedVariables_shouldSetModifiedBy() throws GenesisException {
            // GIVEN
            VariablesMap variablesMap = buildVariablesMapWithVar();
            SurveyUnitInputDto inputDto = buildInputDtoWithStates(
                    List.of(DataState.EDITED)
            );

            // WHEN
            List<SurveyUnitModel> result = surveyUnitService.parseEditedVariables(inputDto, "userX", variablesMap);

            // THEN
            assertThat(result.getFirst().getModifiedBy()).isEqualTo("userX");
        }

        @Test
        @DisplayName("Should handle null value")
        void parseEditedVariables_shouldHandleNullValue() throws GenesisException {
            // GIVEN
            VariablesMap variablesMap = buildVariablesMapWithVar();
            VariableStateInputDto variableStateInputDto = VariableStateInputDto.builder()
                    .state(DataState.EDITED)
                    .build();

            VariableInputDto variableInputDto = VariableInputDto.builder()
                    .variableName("VAR1")
                    .variableStateInputDto(variableStateInputDto)
                    .iteration(1)
                    .build();

            SurveyUnitInputDto surveyUnitInputDto = SurveyUnitInputDto.builder()
                    .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                    .questionnaireId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                    .mode(Mode.WEB)
                    .collectedVariables(List.of(variableInputDto))
                    .build();

            // WHEN
            List<SurveyUnitModel> result = surveyUnitService.parseEditedVariables(surveyUnitInputDto, "user1", variablesMap);

            // THEN
            assertThat(result).hasSize(1);
            VariableModel variable = result.getFirst().getCollectedVariables().getFirst();
            assertThat(variable.value()).isNull();
        }

        // parseEditedVariable UTILS
        private SurveyUnitInputDto buildInputDtoWithStates(List<DataState> states) {
            List<VariableInputDto> variables = new ArrayList<>();
            for (DataState state : states) {
                VariableStateInputDto variableStateInputDto = VariableStateInputDto.builder()
                        .state(state)
                        .value("value_" + state)
                        .build();

                VariableInputDto variableInputDto = VariableInputDto.builder()
                        .variableName("VAR1")
                        .variableStateInputDto(variableStateInputDto)
                        .iteration(1)
                        .build();
                variables.add(variableInputDto);
            }

            return SurveyUnitInputDto.builder()
                    .interrogationId(TestConstants.DEFAULT_INTERROGATION_ID)
                    .questionnaireId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID)
                    .mode(Mode.WEB)
                    .collectedVariables(variables)
                    .build();
        }

        private VariablesMap buildVariablesMapWithVar() {
            MetadataModel metadataModel = new MetadataModel();
            Variable variable = new Variable(
                    "VAR1",
                    metadataModel.getRootGroup(),
                    VariableType.STRING,
                    "1"
            );
            metadataModel.getVariables().putVariable(variable);
            return metadataModel.getVariables();
        }
    }

    //UTILS

    private SurveyUnitDocument buildDoc(String interrogationId, Mode mode) {
        SurveyUnitDocument doc = new SurveyUnitDocument();
        doc.setInterrogationId(interrogationId);
        doc.setMode(String.valueOf(mode));
        doc.setQuestionnaireId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID);
        doc.setCollectedVariables(new ArrayList<>());
        doc.setExternalVariables(new ArrayList<>());
        doc.setRecordDate(Instant.now());
        return doc;
    }
}
