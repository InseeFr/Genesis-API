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

@SuppressWarnings("deprecation") //Useless warnings, we test deprecated formats
class SurveyUnitDocumentMapperImplTest {
    //Given
    //Test constants
    //Current format
    public static final String COLLECTION_INSTRUMENT_ID = "TESTCOLLECTIONINSTRUMENTID";
    public static final String MODE = "WEB";
    public static final String USUAL_SURVEY_UNIT_ID = "TESTUSUALSURVEYUNITID";
    public static final String INTERROGATION_ID = "TESTINTERROGATIONID";
    public static final String VAR_ID = "TESTVARID";

    //Deprecated format
    public static final String CAMPAIGN_ID = "TESTCAMPAIGNID";
    public static final String ID_UE = "TESTIDUE";
    public static final String QUESTIONNAIRE_ID = "TESTQUESTIONNAIREID";

    static SurveyUnitDocumentMapper surveyUnitDocumentMapperImplStatic;
    static SurveyUnitDocument surveyUnitDocumentStatic;
    static SurveyUnitDocument deprecatedSurveyUnitDocumentStatic;

    static SurveyUnitModel surveyUnitStatic;

    @BeforeAll
    static void init(){
        surveyUnitDocumentMapperImplStatic = new SurveyUnitDocumentMapperImpl();

        //Current format document
        surveyUnitDocumentStatic = new SurveyUnitDocument();
        surveyUnitDocumentStatic.setCollectionInstrumentId(COLLECTION_INSTRUMENT_ID);
        surveyUnitDocumentStatic.setMode(MODE);
        surveyUnitDocumentStatic.setUsualSurveyUnitId(USUAL_SURVEY_UNIT_ID);
        surveyUnitDocumentStatic.setInterrogationId(INTERROGATION_ID);
        surveyUnitDocumentStatic.setState("COLLECTED");
        surveyUnitDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        List<VariableDocument> documentExternalVariableList = new ArrayList<>();
        VariableDocument externalVariable = new VariableDocument();
        externalVariable.setVarId(VAR_ID);
        externalVariable.setValue("V1");
        documentExternalVariableList.add(externalVariable);
        surveyUnitDocumentStatic.setExternalVariables(documentExternalVariableList);

        List<VariableDocument> documentCollectedVariableList = new ArrayList<>();
        VariableDocument variableDocument = new VariableDocument();
        variableDocument.setVarId(VAR_ID);
        variableDocument.setValue("V1");
        documentCollectedVariableList.add(variableDocument);
        surveyUnitDocumentStatic.setCollectedVariables(documentCollectedVariableList);

        List<VariableModel> externalVariableModelList = new ArrayList<>();
        VariableModel variable =
                VariableModel.builder().varId(VAR_ID).value("V1").build();
        externalVariableModelList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId(VAR_ID)
                .value("V1")
                .scope("TESTSCOPE")
                .parentId("TESTPARENTID")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);

        //TODO deprecated document
        deprecatedSurveyUnitDocumentStatic = new SurveyUnitDocument();
        deprecatedSurveyUnitDocumentStatic.setCampaignId(CAMPAIGN_ID);
        deprecatedSurveyUnitDocumentStatic.setQuestionnaireId(QUESTIONNAIRE_ID);
        deprecatedSurveyUnitDocumentStatic.setMode(MODE);
        deprecatedSurveyUnitDocumentStatic.setIdUE(ID_UE);
        deprecatedSurveyUnitDocumentStatic.setInterrogationId(INTERROGATION_ID);
        deprecatedSurveyUnitDocumentStatic.setState("COLLECTED");
        deprecatedSurveyUnitDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        documentExternalVariableList = new ArrayList<>();
        externalVariable = new VariableDocument();
        externalVariable.setVarId(VAR_ID);
        externalVariable.setValue("V1");
        documentExternalVariableList.add(externalVariable);
        deprecatedSurveyUnitDocumentStatic.setExternalVariables(documentExternalVariableList);

        documentCollectedVariableList = new ArrayList<>();
        variableDocument = new VariableDocument();
        variableDocument.setVarId(VAR_ID);
        variableDocument.setValue("V1");
        documentCollectedVariableList.add(variableDocument);
        deprecatedSurveyUnitDocumentStatic.setCollectedVariables(documentCollectedVariableList);

        externalVariableModelList = new ArrayList<>();
        variable = VariableModel.builder().varId(VAR_ID).value("V1").build();
        externalVariableModelList.add(variable);

        collectedVariableList = new ArrayList<>();
        collectedVariable = VariableModel.builder()
                .varId(VAR_ID)
                .value("V1")
                .scope("TESTSCOPE")
                .parentId("TESTPARENTID")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);


        //Current format model
        surveyUnitStatic = SurveyUnitModel.builder()
                .mode(Mode.WEB)
                .interrogationId(INTERROGATION_ID)
                .collectionInstrumentId(COLLECTION_INSTRUMENT_ID)
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

        Assertions.assertThat(surveyUnit.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnit.getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnit.getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(surveyUnit.getUsualSurveyUnitId()).isEqualTo(USUAL_SURVEY_UNIT_ID);
        Assertions.assertThat(surveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnit.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnit.getExternalVariables()).filteredOn(externalVariableModel ->
            externalVariableModel.varId().equals(VAR_ID)
            && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnit.getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals(VAR_ID)
                        && variableModel.value().equals("V1")
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert deprecated survey unit document to model")
    void shouldReturnModelFromDeprecatedDocument(){
        SurveyUnitModel surveyUnit = surveyUnitDocumentMapperImplStatic.documentToModel(deprecatedSurveyUnitDocumentStatic);

        Assertions.assertThat(surveyUnit.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnit.getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnit.getCollectionInstrumentId()).isEqualTo(QUESTIONNAIRE_ID);
        Assertions.assertThat(surveyUnit.getUsualSurveyUnitId()).isEqualTo(ID_UE);
        Assertions.assertThat(surveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnit.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnit.getExternalVariables()).filteredOn(externalVariableModel ->
                externalVariableModel.varId().equals(VAR_ID)
                        && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnit.getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals(VAR_ID)
                        && variableModel.value().equals("V1")
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert survey unit model to document")
    void shouldReturnDocumentFromModel(){
        SurveyUnitDocument surveyUnitDocument = surveyUnitDocumentMapperImplStatic.modelToDocument(surveyUnitStatic);

        Assertions.assertThat(surveyUnitDocument.getMode()).isEqualTo(MODE);
        Assertions.assertThat(surveyUnitDocument.getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnitDocument.getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(surveyUnitDocument.getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocument.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocument.getExternalVariables()).filteredOn(externalVariableDocument ->
                externalVariableDocument.getVarId().equals(VAR_ID)
                        && externalVariableDocument.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocument.getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals(VAR_ID)
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();

    }


    @Test
    @DisplayName("Should convert survey unit document list to model list")
    void shouldReturnModelListFromDocumentList(){
        List<SurveyUnitDocument> surveyUnitDocumentList = new ArrayList<>();
        surveyUnitDocumentList.add(surveyUnitDocumentStatic);

        List<SurveyUnitModel> surveyUnitList = surveyUnitDocumentMapperImplStatic.listDocumentToListModel(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitList.getFirst().getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnitList.getFirst().getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(surveyUnitList.getFirst().getUsualSurveyUnitId()).isEqualTo(USUAL_SURVEY_UNIT_ID);
        Assertions.assertThat(surveyUnitList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitList.getFirst().getExternalVariables()).filteredOn(externalVariableModel ->
                externalVariableModel.varId().equals(VAR_ID)
                        && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitList.getFirst().getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals(VAR_ID)
                        && variableModel.value().equals("V1")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert deprecated survey unit document list to model list")
    void shouldReturnModelListFromDeprecatedDocumentList(){
        List<SurveyUnitDocument> surveyUnitDocumentList = new ArrayList<>();
        surveyUnitDocumentList.add(deprecatedSurveyUnitDocumentStatic);

        List<SurveyUnitModel> surveyUnitList = surveyUnitDocumentMapperImplStatic.listDocumentToListModel(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitList.getFirst().getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnitList.getFirst().getCollectionInstrumentId()).isEqualTo(QUESTIONNAIRE_ID);
        Assertions.assertThat(surveyUnitList.getFirst().getUsualSurveyUnitId()).isEqualTo(ID_UE);
        Assertions.assertThat(surveyUnitList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitList.getFirst().getExternalVariables()).filteredOn(externalVariableModel ->
                externalVariableModel.varId().equals(VAR_ID)
                        && externalVariableModel.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitList.getFirst().getCollectedVariables()).filteredOn(variableModel ->
                variableModel.varId().equals(VAR_ID)
                        && variableModel.value().equals("V1")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert survey unit model list to document list")
    void shouldReturnDocumentListFromModelList(){
        List<SurveyUnitModel> surveyUnitList = new ArrayList<>();
        surveyUnitList.add(surveyUnitStatic);

        List<SurveyUnitDocument> surveyUnitDocumentList = surveyUnitDocumentMapperImplStatic.listModelToListDocument(surveyUnitList);

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getMode()).isEqualTo(MODE);
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getInterrogationId()).isEqualTo(INTERROGATION_ID);
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectionInstrumentId()).isEqualTo(COLLECTION_INSTRUMENT_ID);
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getExternalVariables()).filteredOn(externalVariableDocument ->
                externalVariableDocument.getVarId().equals(VAR_ID)
                        && externalVariableDocument.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals(VAR_ID)
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();
    }
}
