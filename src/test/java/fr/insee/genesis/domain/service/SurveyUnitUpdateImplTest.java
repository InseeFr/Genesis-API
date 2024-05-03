package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.stubs.SurveyUnitUpdatePersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

class SurveyUnitUpdateImplTest {
    //Given
    static SurveyUnitUpdateImpl surveyUnitUpdateImplStatic;
    static SurveyUnitUpdatePersistencePortStub surveyUnitUpdatePersistencePortStub;


    @BeforeAll
    static void init(){
        surveyUnitUpdatePersistencePortStub = new SurveyUnitUpdatePersistencePortStub();

        surveyUnitUpdateImplStatic = new SurveyUnitUpdateImpl(surveyUnitUpdatePersistencePortStub);
    }

    @BeforeEach
    void reset(){
        surveyUnitUpdatePersistencePortStub.getMongoStub().clear();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build());
    }

    //When + Then
    @Test
    @DisplayName("The survey unit should be saved into DB")
    void saveAllTest(){
        List<SurveyUnitUpdateDto> newSurveyUnitUpdateDtoList = new ArrayList<>();

        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        newSurveyUnitUpdateDtoList.add(
                SurveyUnitUpdateDto.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE("TESTIDUE2")
                        .idQuest("TESTIDQUESTIONNAIRE")
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableDtoList)
                        .collectedVariables(collectedVariableDtoList)
                        .build()
        );

        surveyUnitUpdateImplStatic.saveSurveyUnits(newSurveyUnitUpdateDtoList);

        Assertions.assertThat(surveyUnitUpdatePersistencePortStub.getMongoStub()).filteredOn(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdCampaign().equals("TESTIDCAMPAIGN")
                && surveyUnitUpdateDto.getMode().equals(Mode.WEB)
                && surveyUnitUpdateDto.getIdUE().equals("TESTIDUE2")
                && surveyUnitUpdateDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
                && surveyUnitUpdateDto.getState().equals(DataState.COLLECTED)
                && surveyUnitUpdateDto.getFileDate().equals(LocalDateTime.of(2023,1,1,0,0,0))
                && surveyUnitUpdateDto.getRecordDate().equals(LocalDateTime.of(2024,1,1,0,0,0))
                && !surveyUnitUpdateDto.getExternalVariables().stream().filter(
                        externalVariable -> externalVariable.getIdVar().equals("TESTIDVAR")
                        && externalVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                        && !surveyUnitUpdateDto.getCollectedVariables().stream().filter(
                        collectedVariable -> collectedVariable.getIdVar().equals("TESTIDVAR")
                                && collectedVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                ).isNotEmpty();
    }

    @Test
    void findByIdsUEAndQuestionnaireTest(){
        Assertions.assertThat(surveyUnitUpdateImplStatic.findByIdsUEAndQuestionnaire("TESTIDUE","TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitUpdateDto ->
                        surveyUnitUpdateDto.getIdUE().equals("TESTIDUE")
                        && surveyUnitUpdateDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
        ).isNotEmpty();
    }

    @Test
    void findByIdUETest(){
        Assertions.assertThat(surveyUnitUpdateImplStatic.findByIdUE("TESTIDUE")).filteredOn(
                surveyUnitUpdateDto ->
                        surveyUnitUpdateDto.getIdUE().equals("TESTIDUE")
        ).isNotEmpty();
    }

    @Test
    void findByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitUpdateImplStatic.findByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitUpdateDto -> surveyUnitUpdateDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitUpdateImplStatic.findLatestByIdAndByMode("TESTIDUE","TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitUpdateDto -> surveyUnitUpdateDto.getIdUE().equals("TESTIDUE")
                && surveyUnitUpdateDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
                && surveyUnitUpdateDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctIdUEsByIdQuestionnaireTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitUpdateImplStatic.findDistinctIdUEsByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitId -> surveyUnitId.getIdUE().equals("TESTIDUE")
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findIdUEsByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitUpdateImplStatic.findModesByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                mode -> mode.equals(Mode.WEB)
        ).isNotEmpty();
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        Assertions.assertThat(surveyUnitUpdateImplStatic.findIdQuestionnairesByIdCampaign("TESTIDCAMPAIGN")).isNotEmpty().hasSize(2);

    }

    private void addAdditionnalDtoToMongoStub(){
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto recentDTO = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
                .build();
        surveyUnitUpdatePersistencePortStub.getMongoStub().add(recentDTO);
    }

}
