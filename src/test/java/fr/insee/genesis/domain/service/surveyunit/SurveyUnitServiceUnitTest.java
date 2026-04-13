package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.SurveyUnitSimplifiedDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;

class SurveyUnitServiceUnitTest {
    
    //Given
    static SurveyUnitService surveyUnitService;
    static SurveyUnitPersistencePort surveyUnitPersistencePortStub;
    static QuestionnaireMetadataService questionnaireMetadataServiceStub;

    @BeforeEach
    void init(){
        surveyUnitPersistencePortStub = mock(SurveyUnitPersistencePortStub.class);



        questionnaireMetadataServiceStub = mock(QuestionnaireMetadataService.class);

        surveyUnitService = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                questionnaireMetadataServiceStub,
                new FileUtils(new ConfigStub())
        );
    }

    @Test
    void get_latest_should_return_usualSurveyId(){
        //GIVEN
        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(TestConstants.DEFAULT_SURVEY_UNIT_ID);

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
        Assertions.assertThat(surveyUnitModels.getFirst().getUsualSurveyUnitId()).isEqualTo(TestConstants.DEFAULT_SURVEY_UNIT_ID);

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
    void get_simplified_should_return_usualSurveyId(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();
        String usualSurveyUnitId = "test";

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(usualSurveyUnitId);
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now());
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
                null
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getUsualSurveyUnitId()).isNotNull().isEqualTo(usualSurveyUnitId);
    }

    @Test
    @SuppressWarnings("deprecation")
    @SneakyThrows
    void get_simplified_should_return_latest_state_in_collected_variable(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now());
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
                null
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getVariablesUpdate().getFirst().state()).isEqualTo(DataState.EDITED);
    }

    @Test
    @SuppressWarnings("deprecation")
    @SneakyThrows
    void get_simplified_should_return_latest_state_in_external_variable(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));
        surveyUnitDocument.setExternalVariables(new ArrayList<>());
        surveyUnitDocument.getExternalVariables().add(new VariableDocument());
        surveyUnitDocument.getExternalVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getExternalVariables().getFirst().setIteration(1);
        surveyUnitDocument.getExternalVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.FORCED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now());
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
                null
        );

        //THEN
        Assertions.assertThat(surveyUnitSimplifiedDto).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitSimplifiedDto.getExternalVariables().getFirst().state()).isEqualTo(DataState.FORCED);
    }


    @Test
    @SuppressWarnings("deprecation")
    void get_latest_should_return_usualSurveyId_when_idue(){
        //GIVEN
        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setIdUE(TestConstants.DEFAULT_SURVEY_UNIT_ID);

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
        Assertions.assertThat(surveyUnitModels.getFirst().getUsualSurveyUnitId()).isEqualTo(TestConstants.DEFAULT_SURVEY_UNIT_ID);

    }

    @Test
    @SuppressWarnings("deprecation")
    void get_latest_should_return_edited(){
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setIdUE(TestConstants.DEFAULT_SURVEY_UNIT_ID);
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);

        surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setState(DataState.EDITED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now());
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

        //WHEN
        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findLatestByIdAndByCollectionInstrumentId(
                TestConstants.DEFAULT_INTERROGATION_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        Assertions.assertThat(surveyUnitModels).isNotNull().hasSize(2);
    }

    @Test
    void countResponsesByCollectionInstrumentId_test() {
        //GIVEN
        long exampleCount = 200;
        doReturn(exampleCount).when(surveyUnitPersistencePortStub).countByCollectionInstrumentId(any());

        //WHEN + THEN
        Assertions.assertThat(surveyUnitService.countResponsesByCollectionInstrumentId(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID))
                .isEqualTo(exampleCount);
    }

    @Test
    void findByIdsUsualSurveyUnitAndCollectionInstrument_should_return_survey_units_from_persistence_port() {
        //GIVEN
        List<SurveyUnitDocument> surveyUnitDocuments = new ArrayList<>();

        SurveyUnitDocument surveyUnitDocument = new SurveyUnitDocument();
        surveyUnitDocument.setMode(String.valueOf(Mode.WEB));
        surveyUnitDocument.setUsualSurveyUnitId(TestConstants.DEFAULT_SURVEY_UNIT_ID);
        surveyUnitDocument.setState(DataState.COLLECTED.toString());
        surveyUnitDocument.setRecordDate(LocalDateTime.now().minusMinutes(1));
        surveyUnitDocument.setCollectedVariables(new ArrayList<>());
        surveyUnitDocument.getCollectedVariables().add(new VariableDocument());
        surveyUnitDocument.getCollectedVariables().getFirst().setVarId("VAR1");
        surveyUnitDocument.getCollectedVariables().getFirst().setIteration(1);
        surveyUnitDocument.getCollectedVariables().getFirst().setValue("VAR1");
        surveyUnitDocuments.add(surveyUnitDocument);


        doReturn(SurveyUnitDocumentMapper.INSTANCE.listDocumentToListModel(surveyUnitDocuments))
                .when(surveyUnitPersistencePortStub).findByUsualSurveyUnitAndCollectionInstrumentIds(
                TestConstants.DEFAULT_SURVEY_UNIT_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //WHEN
        List<SurveyUnitModel> surveyUnitModels = surveyUnitService.findByIdsUsualSurveyUnitAndCollectionInstrument(
                TestConstants.DEFAULT_SURVEY_UNIT_ID,
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        Assertions.assertThat(surveyUnitModels).isNotNull().hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getUsualSurveyUnitId()).isEqualTo(TestConstants.DEFAULT_SURVEY_UNIT_ID);
    }
}
