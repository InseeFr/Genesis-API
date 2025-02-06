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

    @BeforeAll
    static void init(){
        surveyUnitDocumentMapperImplStatic = new SurveyUnitDocumentMapperImpl();

        surveyUnitDocumentStatic = new SurveyUnitDocument();
        surveyUnitDocumentStatic.setCampaignId("TESTIDCAMPAIGN");
        surveyUnitDocumentStatic.setMode("WEB");
        surveyUnitDocumentStatic.setInterrogationId("TESTIDUE");
        surveyUnitDocumentStatic.setQuestionnaireId("TESTIDQUESTIONNAIRE");
        surveyUnitDocumentStatic.setState("COLLECTED");
        surveyUnitDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        List<VariableDocument> documentExternalVariableList = new ArrayList<>();
        VariableDocument externalVariable = new VariableDocument();
        externalVariable.setVarId("TESTIDVAR");
        externalVariable.setValue("V1");
        documentExternalVariableList.add(externalVariable);
        surveyUnitDocumentStatic.setExternalVariables(documentExternalVariableList);

        List<VariableDocument> documentCollectedVariableList = new ArrayList<>();
        VariableDocument variableDocument = new VariableDocument();
        variableDocument.setVarId("TESTIDVAR");
        variableDocument.setValue("V1");
        documentCollectedVariableList.add(variableDocument);
        surveyUnitDocumentStatic.setCollectedVariables(documentCollectedVariableList);

        List<VariableModel> externalVariableDtoList = new ArrayList<>();
        VariableModel variable =
                VariableModel.builder().varId("TESTIDVAR").value("V1").build();
        externalVariableDtoList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .value("V1")
                .scope("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .iteration(1)
                .build();
        collectedVariableList.add(collectedVariable);


        surveyUnitStatic = SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId("TESTIDUE")
                .questionnaireId("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableDtoList)
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
    @DisplayName("Should convert document to DTO")
    void shouldReturnDocumentDtoFromDocument(){
        SurveyUnitModel surveyUnit = surveyUnitDocumentMapperImplStatic.documentToModel(surveyUnitDocumentStatic);

        Assertions.assertThat(surveyUnit.getCampaignId()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnit.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnit.getInterrogationId()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnit.getQuestionnaireId()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnit.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnit.getExternalVariables()).filteredOn(externalVariableDto ->
            externalVariableDto.varId().equals("TESTIDVAR")
            && externalVariableDto.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnit.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.varId().equals("TESTIDVAR")
                        && variableStateDto.value().equals("V1")
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        SurveyUnitDocument surveyUnitDocument = surveyUnitDocumentMapperImplStatic.modelToDocument(surveyUnitStatic);

        Assertions.assertThat(surveyUnitDocument.getCampaignId()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDocument.getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocument.getInterrogationId()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDocument.getQuestionnaireId()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDocument.getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocument.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocument.getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getVarId().equals("TESTIDVAR")
                        && externalVariableDto.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocument.getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals("TESTIDVAR")
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();

    }


    @Test
    @DisplayName("Should convert document list to DTO list")
    void shouldReturnDocumentLDtoListFromDocumentList(){
        List<SurveyUnitDocument> surveyUnitDocumentList = new ArrayList<>();
        surveyUnitDocumentList.add(surveyUnitDocumentStatic);

        List<SurveyUnitModel> surveyUnitList = surveyUnitDocumentMapperImplStatic.listDocumentToListModel(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitList.getFirst().getCampaignId()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitList.getFirst().getInterrogationId()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitList.getFirst().getQuestionnaireId()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.varId().equals("TESTIDVAR")
                        && externalVariableDto.value().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.varId().equals("TESTIDVAR")
                        && variableStateDto.value().equals("V1")
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO list to document list")
    void shouldReturnDocumentListFromDocumentDtoList(){
        List<SurveyUnitModel> surveyUnitList = new ArrayList<>();
        surveyUnitList.add(surveyUnitStatic);

        List<SurveyUnitDocument> surveyUnitDocumentList = surveyUnitDocumentMapperImplStatic.listModelToListDocument(surveyUnitList);

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCampaignId()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getInterrogationId()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getQuestionnaireId()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getVarId().equals("TESTIDVAR")
                        && externalVariableDto.getValue().equals("V1")
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectedVariables()).filteredOn(variableDocument ->
                variableDocument.getVarId().equals("TESTIDVAR")
                        && variableDocument.getValue().equals("V1")
        ).isNotEmpty();
    }
}
