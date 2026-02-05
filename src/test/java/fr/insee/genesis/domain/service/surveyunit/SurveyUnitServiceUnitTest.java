package fr.insee.genesis.domain.service.surveyunit;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.ports.spi.SurveyUnitPersistencePort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import integration_tests.stubs.SurveyUnitPersistencePortStub;
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
                new FileUtils(TestConstants.getConfigStub())
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
}
