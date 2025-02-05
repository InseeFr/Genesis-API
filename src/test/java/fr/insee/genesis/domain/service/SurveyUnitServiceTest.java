package fr.insee.genesis.domain.service;

import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
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
    static final String DEFAULT_ID_UE = "TESTIDUE";
    static final String DEFAULT_ID_QUEST = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init(){
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        surveyUnitServiceStatic = new SurveyUnitService(surveyUnitPersistencePortStub);
    }

    @BeforeEach
    void reset(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();


        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_ID_UE)
                .questionnaireId(DEFAULT_ID_QUEST)
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

        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariableModel = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();

        collectedVariableList.add(collectedVariableModel);

        newSurveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .campaignId("TESTIDCAMPAIGN")
                        .mode(Mode.WEB)
                        .interrogationId("TESTIDUE2")
                        .questionnaireId(DEFAULT_ID_QUEST)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableList)
                        .collectedVariables(collectedVariableList)
                        .build()
        );

        surveyUnitServiceStatic.saveSurveyUnits(newSurveyUnitModelList);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitModel ->
                surveyUnitModel.getCampaignId().equals("TESTIDCAMPAIGN")
                && surveyUnitModel.getMode().equals(Mode.WEB)
                && surveyUnitModel.getInterrogationId().equals("TESTIDUE2")
                && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
                && surveyUnitModel.getState().equals(DataState.COLLECTED)
                && surveyUnitModel.getFileDate().equals(LocalDateTime.of(2023,1,1,0,0,0))
                && surveyUnitModel.getRecordDate().equals(LocalDateTime.of(2024,1,1,0,0,0))
                && !surveyUnitModel.getExternalVariables().stream().filter(
                        externalVariable -> externalVariable.varId().equals("TESTIDVAR")
                        && externalVariable.values().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                        && !surveyUnitModel.getCollectedVariables().stream().filter(
                        collectedVariable -> collectedVariable.varId().equals("TESTIDVAR")
                                && collectedVariable.values().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                ).isNotEmpty();
    }

    @Test
    void findByIdsUEAndQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByIdsInterrogationAndQuestionnaire(DEFAULT_ID_UE, DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getInterrogationId().equals(DEFAULT_ID_UE)
                        && surveyUnitDto.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
        ).isNotEmpty();
    }

    @Test
    void findByIdUETest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByInterrogationId(DEFAULT_ID_UE)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getInterrogationId().equals(DEFAULT_ID_UE)
        ).isNotEmpty();
    }

    @Test
    void findByIdQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByQuestionnaireId(DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_ID_UE, DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(DEFAULT_ID_UE)
                && surveyUnitDto.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
                && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findResponsesByUEAndQuestionnaireTest_null_collectedVariables() {
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );
        surveyUnitPersistencePortStub.getMongoStub().getLast().setCollectedVariables(null);

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_ID_UE, DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(DEFAULT_ID_UE)
                        && surveyUnitDto.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
                        && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }
    @Test
    void findResponsesByUEAndQuestionnaireTest_null_externalVariables() {
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );
        surveyUnitPersistencePortStub.getMongoStub().getLast().setExternalVariables(null);

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_ID_UE, DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(DEFAULT_ID_UE)
                        && surveyUnitDto.getQuestionnaireId().equals(DEFAULT_ID_QUEST)
                        && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctIdUEsByQuestionnaireIdTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findDistinctInterrogationIdsByQuestionnaireId(DEFAULT_ID_QUEST)).filteredOn(
                surveyUnitId -> surveyUnitId.getInterrogationId().equals(DEFAULT_ID_UE)
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findIdUEsByQuestionnaireIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findModesByQuestionnaireId(DEFAULT_ID_QUEST)).filteredOn(
                mode -> mode.equals(Mode.WEB)
        ).isNotEmpty();
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        Assertions.assertThat(surveyUnitServiceStatic.findQuestionnaireIdsByCampaignId("TESTIDCAMPAIGN")).isNotEmpty().hasSize(2);

    }

    @Test
    void getAllCampaignsTest() {
        Assertions.assertThat(surveyUnitServiceStatic.findDistinctCampaignIds()).contains("TESTIDCAMPAIGN");
    }

    @Test
    void findLatestByIdAndByQuestionnaireIdPerretTest(){
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
        SurveyUnitDto surveyUnitDto = surveyUnitServiceStatic.findLatestValuesByStateByIdAndByQuestionnaireId(
                DEFAULT_ID_UE,
                DEFAULT_ID_QUEST
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(DEFAULT_ID_UE);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                            variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                .stream().filter(
                        variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                .stream().filter(
                        variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                ).toList().getFirst().isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }

    @Test
    void findLatestByIdAndByQuestionnaireIdPerretTest_null_collectedVariables(){
        //Given
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );
        surveyUnitPersistencePortStub.getMongoStub().getLast().setCollectedVariables(null);


        //When
        SurveyUnitDto surveyUnitDto = surveyUnitServiceStatic.findLatestValuesByStateByIdAndByQuestionnaireId(
                DEFAULT_ID_UE,
                DEFAULT_ID_QUEST
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(DEFAULT_ID_UE);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("E NEW E");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }

    @Test
    void findLatestByIdAndByQuestionnaireIdPerretTest_null_externalVariables(){
        //Given
        addAdditionnalDtoToMongoStub(DataState.EDITED,
                "C NEW E",
                "E NEW E",
                LocalDateTime.of(2025,2,2,0,0,0),
                LocalDateTime.of(2025,2,2,0,0,0)
        );
        surveyUnitPersistencePortStub.getMongoStub().getLast().setExternalVariables(null);


        //When
        SurveyUnitDto surveyUnitDto = surveyUnitServiceStatic.findLatestValuesByStateByIdAndByQuestionnaireId(
                DEFAULT_ID_UE,
                DEFAULT_ID_QUEST
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(DEFAULT_ID_UE);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().getValue())
                .isEqualTo("C NEW E");
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isFalse();
        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.EDITED)
                        ).toList().getFirst().isActive())
                .isTrue();

        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableName())
                .isEqualTo("TESTIDVAR");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().getValue())
                .isEqualTo("V1");
        Assertions.assertThat(surveyUnitDto.getExternalVariables().getFirst().getVariableStateDtoList()
                        .stream().filter(
                                variableStatePerret -> variableStatePerret.getState().equals(DataState.COLLECTED)
                        ).toList().getFirst().isActive())
                .isTrue();
    }

    private void addAdditionnalDtoToMongoStub(){
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariableModel = VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1"
                , "V2"})).build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_ID_UE)
                .questionnaireId(DEFAULT_ID_QUEST)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String idQuestionnaire) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariableModel = VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1"
                , "V2"})).build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_ID_UE)
                .questionnaireId(idQuestionnaire)
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
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariableModel =
                VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{collectedVariableValue}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_ID_UE)
                .questionnaireId(DEFAULT_ID_QUEST)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }
}
