package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitUpdateDocumentMapperImpl;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
import fr.insee.genesis.infrastructure.model.document.SurveyUnitUpdateDocument;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

class SurveyUnitUpdateDocumentMapperImplTest {

    //Given
    static SurveyUnitUpdateDocumentMapperImpl  surveyUnitUpdateDocumentMapperImplStatic;
    static SurveyUnitUpdateDocument surveyUnitUpdateDocumentStatic;

    static SurveyUnitUpdateDto surveyUnitUpdateDtoStatic;

    @BeforeAll
    static void init(){
        surveyUnitUpdateDocumentMapperImplStatic = new SurveyUnitUpdateDocumentMapperImpl();


        surveyUnitUpdateDocumentStatic = new SurveyUnitUpdateDocument();
        surveyUnitUpdateDocumentStatic.setIdCampaign("TESTIDCAMPAIGN");
        surveyUnitUpdateDocumentStatic.setMode("WEB");
        surveyUnitUpdateDocumentStatic.setIdUE("TESTIDUE");
        surveyUnitUpdateDocumentStatic.setIdQuestionnaire("TESTIDQUESTIONNAIRE");
        surveyUnitUpdateDocumentStatic.setState("COLLECTED");
        surveyUnitUpdateDocumentStatic.setFileDate(LocalDateTime.of(2023,1,1,0,0,0));

        List<ExternalVariable> externalVariableList = new ArrayList<>();
        ExternalVariable externalVariable = new ExternalVariable();
        externalVariable.setIdVar("TESTIDVAR");
        externalVariable.setValues(List.of(new String[]{"V1", "V2"}));
        externalVariableList.add(externalVariable);
        surveyUnitUpdateDocumentStatic.setExternalVariables(externalVariableList);

        List<VariableState> variableStateList = new ArrayList<>();
        VariableState variableState = new VariableState();
        variableState.setIdVar("TESTIDVAR");
        variableState.setValues(List.of(new String[]{"V1", "V2"}));
        variableStateList.add(variableState);
        surveyUnitUpdateDocumentStatic.setCollectedVariables(variableStateList);

        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);


        surveyUnitUpdateDtoStatic = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();

    }

    //When + Then
    @Test
    @DisplayName("Should return null if null parameter")
    void shouldReturnNull(){
        Assertions.assertThat(surveyUnitUpdateDocumentMapperImplStatic.documentToDto(null)).isNull();
        Assertions.assertThat(surveyUnitUpdateDocumentMapperImplStatic.dtoToDocument(null)).isNull();
        Assertions.assertThat(surveyUnitUpdateDocumentMapperImplStatic.listDocumentToListDto(null)).isNull();
        Assertions.assertThat(surveyUnitUpdateDocumentMapperImplStatic.listDtoToListDocument(null)).isNull();
    }

    @Test
    @DisplayName("Should convert document to DTO")
    void shouldReturnDocumentDtoFromDocument(){
        SurveyUnitUpdateDto surveyUnitUpdateDto = surveyUnitUpdateDocumentMapperImplStatic.documentToDto(surveyUnitUpdateDocumentStatic);

        Assertions.assertThat(surveyUnitUpdateDto.getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitUpdateDto.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitUpdateDto.getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitUpdateDto.getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitUpdateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitUpdateDto.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitUpdateDto.getExternalVariables()).filteredOn(externalVariableDto ->
            externalVariableDto.getIdVar().equals("TESTIDVAR")
            && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitUpdateDto.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        SurveyUnitUpdateDocument surveyUnitUpdateDocument = surveyUnitUpdateDocumentMapperImplStatic.dtoToDocument(surveyUnitUpdateDtoStatic);

        Assertions.assertThat(surveyUnitUpdateDocument.getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitUpdateDocument.getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitUpdateDocument.getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitUpdateDocument.getIdQuestionnaire()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitUpdateDocument.getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitUpdateDocument.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitUpdateDocument.getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitUpdateDocument.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

    }


    @Test
    @DisplayName("Should convert document list to DTO list")
    void shouldReturnDocumentLDtoListFromDocumentList(){
        List<SurveyUnitUpdateDocument> surveyUnitUpdateDocumentList = new ArrayList<>();
        surveyUnitUpdateDocumentList.add(surveyUnitUpdateDocumentStatic);

        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = surveyUnitUpdateDocumentMapperImplStatic.listDocumentToListDto(surveyUnitUpdateDocumentList);

        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitUpdateDtoList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO list to document list")
    void shouldReturnDocumentListFromDocumentDtoList(){
        List<SurveyUnitUpdateDto> surveyUnitUpdateDtoList = new ArrayList<>();
        surveyUnitUpdateDtoList.add(surveyUnitUpdateDtoStatic);

        List<SurveyUnitUpdateDocument> surveyUnitUpdateDocumentList = surveyUnitUpdateDocumentMapperImplStatic.listDtoToListDocument(surveyUnitUpdateDtoList);

        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getMode()).isEqualTo("WEB");
        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getIdQuestionnaire()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getState()).isEqualTo("COLLECTED");
        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitUpdateDocumentList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();
    }
}
