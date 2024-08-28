package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapperImpl;
import fr.insee.genesis.infrastructure.model.document.surveyunit.ExternalVariable;
import fr.insee.genesis.infrastructure.model.document.surveyunit.VariableState;
import fr.insee.genesis.infrastructure.model.document.surveyunit.SurveyUnitDocument;
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

    static SurveyUnit surveyUnitStatic;

    @BeforeAll
    static void init(){
        surveyUnitDocumentMapperImplStatic = new SurveyUnitDocumentMapperImpl();

        surveyUnitDocumentStatic = new SurveyUnitDocument();
        surveyUnitDocumentStatic.setIdCampaign("TESTIDCAMPAIGN");
        surveyUnitDocumentStatic.setMode("WEB");
        surveyUnitDocumentStatic.setIdUE("TESTIDUE");
        surveyUnitDocumentStatic.setIdQuestionnaire("TESTIDQUESTIONNAIRE");
        surveyUnitDocumentStatic.setState("COLLECTED");
        surveyUnitDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        List<ExternalVariable> externalVariableList = new ArrayList<>();
        ExternalVariable externalVariable = new ExternalVariable();
        externalVariable.setIdVar("TESTIDVAR");
        externalVariable.setValues(List.of(new String[]{"V1", "V2"}));
        externalVariableList.add(externalVariable);
        surveyUnitDocumentStatic.setExternalVariables(externalVariableList);

        List<VariableState> variableStateList = new ArrayList<>();
        VariableState variableState = new VariableState();
        variableState.setIdVar("TESTIDVAR");
        variableState.setValues(List.of(new String[]{"V1", "V2"}));
        variableStateList.add(variableState);
        surveyUnitDocumentStatic.setCollectedVariables(variableStateList);

        List<Variable> externalVariableDtoList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);


        surveyUnitStatic = SurveyUnit.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
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
        SurveyUnit surveyUnit = surveyUnitDocumentMapperImplStatic.documentToModel(surveyUnitDocumentStatic);

        Assertions.assertThat(surveyUnit.getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnit.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnit.getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnit.getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnit.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnit.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnit.getExternalVariables()).filteredOn(externalVariableDto ->
            externalVariableDto.getIdVar().equals("TESTIDVAR")
            && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnit.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        SurveyUnitDocument surveyUnitDocument = surveyUnitDocumentMapperImplStatic.modelToDocument(surveyUnitStatic);

        Assertions.assertThat(surveyUnitDocument.getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDocument.getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocument.getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDocument.getIdQuestionnaire()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDocument.getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocument.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocument.getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocument.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

    }


    @Test
    @DisplayName("Should convert document list to DTO list")
    void shouldReturnDocumentLDtoListFromDocumentList(){
        List<SurveyUnitDocument> surveyUnitDocumentList = new ArrayList<>();
        surveyUnitDocumentList.add(surveyUnitDocumentStatic);

        List<SurveyUnit> surveyUnitList = surveyUnitDocumentMapperImplStatic.listDocumentToListModel(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitList.getFirst().getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitList.getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitList.getFirst().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO list to document list")
    void shouldReturnDocumentListFromDocumentDtoList(){
        List<SurveyUnit> surveyUnitList = new ArrayList<>();
        surveyUnitList.add(surveyUnitStatic);

        List<SurveyUnitDocument> surveyUnitDocumentList = surveyUnitDocumentMapperImplStatic.listModelToListDocument(surveyUnitList);

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getIdQuestionnaire()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitDocumentList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDocumentList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();
    }
}
