package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapperImpl;
import fr.insee.genesis.infrastructure.document.surveyunit.VariableDocument;
import fr.insee.genesis.infrastructure.document.surveyunit.SurveyUnitDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class SurveyUnitDocumentMapperImplTest {

    //Given
    static SurveyUnitDocumentMapper surveyUnitDocumentMapperImplStatic;
    static SurveyUnitDocument surveyUnitDocumentStatic;

    static SurveyUnitModel surveyUnitStatic;

    // TODO : make different tests for document to model transformation, for old documents and new ones

    @BeforeAll
    static void init(){
        surveyUnitDocumentMapperImplStatic = new SurveyUnitDocumentMapperImpl();

        surveyUnitDocumentStatic = new SurveyUnitDocument();
        surveyUnitDocumentStatic.setCampaignId("TESTCAMPAIGNID");
        surveyUnitDocumentStatic.setMode("WEB");
        surveyUnitDocumentStatic.setInterrogationId("TESTINTERROGATIONID");
        surveyUnitDocumentStatic.setQuestionnaireId("TESTQUESTIONNAIREID");
        surveyUnitDocumentStatic.setState("COLLECTED");
        surveyUnitDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        List<VariableDocument> documentExternalVariableList = new ArrayList<>();
        VariableDocument externalVariable = new VariableDocument();
        externalVariable.setVarId("TESTVARID");
        externalVariable.setValue("V1");
        documentExternalVariableList.add(externalVariable);
        surveyUnitDocumentStatic.setExternalVariables(documentExternalVariableList);

        List<VariableDocument> documentCollectedVariableList = new ArrayList<>();
        VariableDocument variableDocument = new VariableDocument();
        variableDocument.setVarId("TESTVARID");
        variableDocument.setValue("V1");
        documentCollectedVariableList.add(variableDocument);
        surveyUnitDocumentStatic.setCollectedVariables(documentCollectedVariableList);

        List<VariableModel> externalVariableModelList = new ArrayList<>();
        VariableModel variable =
                VariableModel.builder().varId("TESTVARID").value("V1").build();
        externalVariableModelList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .parentId("TESTPARENTID")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);


        surveyUnitStatic = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId("TESTINTERROGATIONID")
                .collectionInstrumentId("TESTQUESTIONNAIREID")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableModelList)
                .collectedVariables(collectedVariableList)
                .build();

    }

    //When + Then
    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.documentToModel(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.modelToDocument(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.listDocumentToListModel(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.listModelToListDocument(null)).isNull();
    }

    @Test
    @DisplayName("Should convert survey unit document to model")
    void shouldReturnModelFromDocument(){
        SurveyUnitModel surveyUnit = surveyUnitDocumentMapperImplStatic.documentToModel(surveyUnitDocumentStatic);

        Assertions.assertThat(surveyUnit.getCampaignId()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(surveyUnit.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnit.getInterrogationId()).isEqualTo("TESTINTERROGATIONID");
        Assertions.assertThat(surveyUnit.getCollectionInstrumentId()).isEqualTo("TESTQUESTIONNAIREID");
        Assertions.assertThat(surveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnit.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnit.getExternalVariables()).filteredOn(externalVariableModel ->
            externalVariableModel.varId().equals("TESTVARID")
            && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnit.getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals("TESTVARID")
                        && variableModel.value().equals("V1")
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert survey unit model to document")
    void shouldReturnDocumentFromModel(){
        SurveyUnitDocument surveyUnitDocument = surveyUnitDocumentMapperImplStatic.modelToDocument(surveyUnitStatic);

        Assertions.assertThat(surveyUnitDocument.getCampaignId()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(surveyUnitDocument.getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocument.getInterrogationId()).isEqualTo("TESTINTERROGATIONID");
        Assertions.assertThat(surveyUnitDocument.getCollectionInstrumentId()).isEqualTo("TESTQUESTIONNAIREID");
        Assertions.assertThat(surveyUnitDocument.getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocument.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocument.getExternalVariables()).filteredOn(externalVariableDocument ->
                externalVariableDocument.getVarId().equals("TESTVARID")
                        && externalVariableDocument.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocument.getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals("TESTVARID")
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();

    }


    @Test
    @DisplayName("Should convert survey unit document list to model list")
    void shouldReturnModelListFromDocumentList(){
        List<SurveyUnitDocument> surveyUnitDocumentList = new ArrayList<>();
        surveyUnitDocumentList.add(surveyUnitDocumentStatic);

        List<SurveyUnitModel> surveyUnitList = surveyUnitDocumentMapperImplStatic.listDocumentToListModel(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitList.getFirst().getCampaignId()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(surveyUnitList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitList.getFirst().getInterrogationId()).isEqualTo("TESTINTERROGATIONID");
        Assertions.assertThat(surveyUnitList.getFirst().getCollectionInstrumentId()).isEqualTo("TESTQUESTIONNAIREID");
        Assertions.assertThat(surveyUnitList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitList.getFirst().getExternalVariables()).filteredOn(externalVariableModel ->
                externalVariableModel.varId().equals("TESTVARID")
                        && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitList.getFirst().getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals("TESTVARID")
                        && variableModel.value().equals("V1")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert survey unit model list to document list")
    void shouldReturnDocumentListFromModelList(){
        List<SurveyUnitModel> surveyUnitList = new ArrayList<>();
        surveyUnitList.add(surveyUnitStatic);

        List<SurveyUnitDocument> surveyUnitDocumentList = surveyUnitDocumentMapperImplStatic.listModelToListDocument(surveyUnitList);

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCampaignId()).isEqualTo("TESTCAMPAIGNID");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getInterrogationId()).isEqualTo("TESTINTERROGATIONID");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectionInstrumentId()).isEqualTo("TESTQUESTIONNAIREID");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getExternalVariables()).filteredOn(externalVariableDocument ->
                externalVariableDocument.getVarId().equals("TESTVARID")
                        && externalVariableDocument.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals("TESTVARID")
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();
    }
}
