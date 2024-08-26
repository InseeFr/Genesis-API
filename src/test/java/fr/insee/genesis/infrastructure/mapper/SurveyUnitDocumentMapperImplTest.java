package fr.insee.genesis.infrastructure.mapper;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.SurveyUnitDocumentMapperImpl;
import fr.insee.genesis.infrastructure.model.ExternalVariable;
import fr.insee.genesis.infrastructure.model.VariableState;
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

    static SurveyUnitDto surveyUnitDtoStatic;

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

        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);


        surveyUnitDtoStatic = SurveyUnitDto.builder()
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
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.documentToDto(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.dtoToDocument(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.listDocumentToListDto(null)).isNull();
        Assertions.assertThat(surveyUnitDocumentMapperImplStatic.listDtoToListDocument(null)).isNull();
    }

    @Test
    @DisplayName("Should convert document to DTO")
    void shouldReturnDocumentDtoFromDocument(){
        SurveyUnitDto surveyUnitDto = surveyUnitDocumentMapperImplStatic.documentToDto(surveyUnitDocumentStatic);

        Assertions.assertThat(surveyUnitDto.getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDto.getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitDto.getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDto.getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitDto.getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDto.getExternalVariables()).filteredOn(externalVariableDto ->
            externalVariableDto.getIdVar().equals("TESTIDVAR")
            && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDto.getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

    }

    @Test
    @DisplayName("Should convert DTO to document")
    void shouldReturnDocumentFromDocumentDto(){
        SurveyUnitDocument surveyUnitDocument = surveyUnitDocumentMapperImplStatic.dtoToDocument(surveyUnitDtoStatic);

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

        List<SurveyUnitDto> surveyUnitDtoList = surveyUnitDocumentMapperImplStatic.listDocumentToListDto(surveyUnitDocumentList);

        Assertions.assertThat(surveyUnitDtoList.getFirst().getIdCampaign()).isEqualTo("TESTIDCAMPAIGN");
        Assertions.assertThat(surveyUnitDtoList.getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitDtoList.getFirst().getIdUE()).isEqualTo("TESTIDUE");
        Assertions.assertThat(surveyUnitDtoList.getFirst().getIdQuest()).isEqualTo("TESTIDQUESTIONNAIRE");
        Assertions.assertThat(surveyUnitDtoList.getFirst().getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(surveyUnitDtoList.getFirst().getFileDate()).isEqualTo(LocalDateTime.of(2023,1,1,0,0,0));

        Assertions.assertThat(surveyUnitDtoList.getFirst().getExternalVariables()).filteredOn(externalVariableDto ->
                externalVariableDto.getIdVar().equals("TESTIDVAR")
                        && externalVariableDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();

        Assertions.assertThat(surveyUnitDtoList.getFirst().getCollectedVariables()).filteredOn(variableStateDto ->
                variableStateDto.getIdVar().equals("TESTIDVAR")
                        && variableStateDto.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
        ).isNotEmpty();
    }

    @Test
    @DisplayName("Should convert DTO list to document list")
    void shouldReturnDocumentListFromDocumentDtoList(){
        List<SurveyUnitDto> surveyUnitDtoList = new ArrayList<>();
        surveyUnitDtoList.add(surveyUnitDtoStatic);

        List<SurveyUnitDocument> surveyUnitDocumentList = surveyUnitDocumentMapperImplStatic.listDtoToListDocument(surveyUnitDtoList);

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
