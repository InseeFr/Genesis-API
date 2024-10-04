package fr.insee.genesis.domain.service;

import fr.insee.genesis.controller.dto.perret.SurveyUnitPerret;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.Variable;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
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

class SurveyUnitServiceTest {

    
    
    
    //Given
    static SurveyUnitService surveyUnitServiceStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init(){
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        surveyUnitServiceStatic = new SurveyUnitService(surveyUnitPersistencePortStub);
    }

    @BeforeEach
    void reset(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build());
    }

    //When + Then
    @Test
    @DisplayName("The survey unit should be saved into DB")
    void saveAllTest(){
        List<SurveyUnitModel> newSurveyUnitModelList = new ArrayList<>();

        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariableDto = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariableDto);

        newSurveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .idCampaign("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .idUE("TESTIDUE2")
                        .idQuest(defaultIdQuest)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableList)
                        .collectedVariables(collectedVariableList)
                        .build()
        );

        surveyUnitServiceStatic.saveSurveyUnits(newSurveyUnitModelList);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getIdCampaign().equals("TESTIDCAMPAIGN")
                && surveyUnitDto.getMode().equals(Mode.WEB)
                && surveyUnitDto.getIdUE().equals("TESTIDUE2")
                && surveyUnitDto.getIdQuest().equals(defaultIdQuest)
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
        Assertions.assertThat(surveyUnitServiceStatic.findByIdsUEAndQuestionnaire(defaultIdUE, defaultIdQuest)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getIdUE().equals(defaultIdUE)
                        && surveyUnitDto.getIdQuest().equals(defaultIdQuest)
        ).isNotEmpty();
    }

    @Test
    void findByIdUETest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByIdUE(defaultIdUE)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getIdUE().equals(defaultIdUE)
        ).isNotEmpty();
    }

    @Test
    void findByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByIdQuestionnaire(defaultIdQuest)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getIdQuest().equals(defaultIdQuest)
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByIdQuestionnaire(defaultIdUE, defaultIdQuest)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getIdUE().equals(defaultIdUE)
                && surveyUnitDto.getIdQuest().equals(defaultIdQuest)
                && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctIdUEsByIdQuestionnaireTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findDistinctIdUEsByIdQuestionnaire(defaultIdQuest)).filteredOn(
                surveyUnitId -> surveyUnitId.getIdUE().equals(defaultIdUE)
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findIdUEsByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findModesByIdQuestionnaire(defaultIdQuest)).filteredOn(
                mode -> mode.equals(Mode.WEB)
        ).isNotEmpty();
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        Assertions.assertThat(surveyUnitServiceStatic.findIdQuestionnairesByIdCampaign("TESTIDCAMPAIGN")).isNotEmpty().hasSize(2);

    }

    @Test
    void getAllCampaignsTest() {
        Assertions.assertThat(surveyUnitServiceStatic.findDistinctIdCampaigns()).contains("TESTIDCAMPAIGN");
    }

    @Test
    void findLatestByIdAndByIdQuestionnairePerretTest(){
        //Given
        //Recent Collected already in stub
        //Old Collected
        addAdditionnalDtoToMongoStub(DataState.COLLECTED,
                "C OLD C", //<Collected/External> <NEW or OLD> <Collected/Edited>
            "E OLD C",
            LocalDateTime.of(1999,2,2,0,0,0),
            LocalDateTime.of(1999,2,2,0,0,0)
        );

        //Recent Edited
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );

        //Old Edited
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C OLD E",
                "E OLD E",
                LocalDateTime.of(1999,2,2,0,0,0),
                LocalDateTime.of(1999,2,2,0,0,0)
        );


        //When
        SurveyUnitPerret surveyUnitPerret = surveyUnitServiceStatic.findLatestByIdAndByIdQuestionnairePerret(
                defaultIdUE,
                defaultIdQuest
        );


        //Then
        Assertions.assertThat(surveyUnitPerret.getSurveyUnitId()).isEqualTo(defaultIdUE);

        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretMap().get(DataState.COLLECTED).getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretMap().get(DataState.EDITED).getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretMap().get(DataState.COLLECTED).isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitPerret.getCollectedVariables().getFirst().getVariableStatePerretMap().get(DataState.EDITED).isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretMap().get(DataState.COLLECTED).getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretMap().get(DataState.EDITED).getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretMap().get(DataState.COLLECTED).isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitPerret.getExternalVariables().getFirst().getVariableStatePerretMap().get(DataState.EDITED).isActive())
                .isTrue();
    }

    private void addAdditionnalDtoToMongoStub(){
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(idQuestionnaire)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 2, 2, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 2, 2, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(DataState state,
                                                         String collectedVariableValue,
                                                         String externalVariableValue,
                                                         LocalDateTime fileDate,
                                                         LocalDateTime recordDate) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{collectedVariableValue}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE(defaultIdUE)
                .idQuest(defaultIdQuest)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }
}
