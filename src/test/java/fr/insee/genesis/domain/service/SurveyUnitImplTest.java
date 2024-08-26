package fr.insee.genesis.domain.service;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.ArrayList;
import java.util.List;

class SurveyUnitImplTest {
    //Given
    static SurveyUnitImpl surveyUnitImplStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;


    @BeforeAll
    static void init(){
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        surveyUnitImplStatic = new SurveyUnitImpl(surveyUnitPersistencePortStub);
    }

    @BeforeEach
    void reset(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitDto.builder()
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
        List<SurveyUnitDto> newSurveyUnitDtoList = new ArrayList<>();

        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        newSurveyUnitDtoList.add(
                SurveyUnitDto.builder()
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

        surveyUnitImplStatic.saveSurveyUnits(newSurveyUnitDtoList);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getIdCampaign().equals("TESTIDCAMPAIGN")
                && surveyUnitDto.getMode().equals(Mode.WEB)
                && surveyUnitDto.getIdUE().equals("TESTIDUE2")
                && surveyUnitDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
                && surveyUnitDto.getState().equals(DataState.COLLECTED)
                && surveyUnitDto.getFileDate().equals(LocalDateTime.of(2023,1,1,0,0,0))
                && surveyUnitDto.getRecordDate().equals(LocalDateTime.of(2024,1,1,0,0,0))
                && !surveyUnitDto.getExternalVariables().stream().filter(
                        externalVariable -> externalVariable.getIdVar().equals("TESTIDVAR")
                        && externalVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                        && !surveyUnitDto.getCollectedVariables().stream().filter(
                        collectedVariable -> collectedVariable.getIdVar().equals("TESTIDVAR")
                                && collectedVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                ).isNotEmpty();
    }

    @Test
    void findByIdsUEAndQuestionnaireTest(){
        Assertions.assertThat(surveyUnitImplStatic.findByIdsUEAndQuestionnaire("TESTIDUE","TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getIdUE().equals("TESTIDUE")
                        && surveyUnitDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
        ).isNotEmpty();
    }

    @Test
    void findByIdUETest(){
        Assertions.assertThat(surveyUnitImplStatic.findByIdUE("TESTIDUE")).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getIdUE().equals("TESTIDUE")
        ).isNotEmpty();
    }

    @Test
    void findByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitImplStatic.findByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitDto -> surveyUnitDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitImplStatic.findLatestByIdAndByIdQuestionnaire("TESTIDUE","TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitDto -> surveyUnitDto.getIdUE().equals("TESTIDUE")
                && surveyUnitDto.getIdQuest().equals("TESTIDQUESTIONNAIRE")
                && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctIdUEsByIdQuestionnaireTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitImplStatic.findDistinctIdUEsByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                surveyUnitId -> surveyUnitId.getIdUE().equals("TESTIDUE")
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findIdUEsByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitImplStatic.findModesByIdQuestionnaire("TESTIDQUESTIONNAIRE")).filteredOn(
                mode -> mode.equals(Mode.WEB)
        ).isNotEmpty();
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        Assertions.assertThat(surveyUnitImplStatic.findIdQuestionnairesByIdCampaign("TESTIDCAMPAIGN")).isNotEmpty().hasSize(2);

    }

    @Test
    void getAllCampaignsTest() {
        Assertions.assertThat(surveyUnitImplStatic.findDistinctIdCampaigns()).contains("TESTIDCAMPAIGN");
    }

    private void addAdditionnalDtoToMongoStub(){
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitDto recentDTO = SurveyUnitDto.builder()
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
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitDto recentDTO = SurveyUnitDto.builder()
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
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

}
