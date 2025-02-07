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
    static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";
    static final String DEFAULT_QUESTIONNAIRE_ID = "TESTQUESTIONNAIREID";

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
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(variable);
        variable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariable);
        collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .scope("TESTSCOPE")
                .iteration(2)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
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
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(externalVariableModel);
        externalVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(externalVariableModel);


        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);
        collectedVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .scope("TESTSCOPE")
                .iteration(2)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);

        newSurveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .campaignId("TESTCAMPAIGNID")
                        .mode(Mode.WEB)
                        .interrogationId("TESTINTERROGATIONID2")
                        .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableList)
                        .collectedVariables(collectedVariableList)
                        .build()
        );

        surveyUnitServiceStatic.saveSurveyUnits(newSurveyUnitModelList);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitModel ->
                        surveyUnitModel.getCampaignId().equals("TESTCAMPAIGNID")
                        && surveyUnitModel.getMode().equals(Mode.WEB)
                        && surveyUnitModel.getInterrogationId().equals("TESTINTERROGATIONID2")
                        && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
                        && surveyUnitModel.getState().equals(DataState.COLLECTED)
                        && surveyUnitModel.getFileDate().equals(LocalDateTime.of(2023,1,1,0,0,0))
                        && surveyUnitModel.getRecordDate().equals(LocalDateTime.of(2024,1,1,0,0,0))
                        && !surveyUnitModel.getExternalVariables().stream().filter(
                                externalVariable -> externalVariable.varId().equals("TESTVARID")
                                        && externalVariable.iteration().equals(1)
                                        && externalVariable.value().equals("V1")
                        ).toList().isEmpty()
                        && !surveyUnitModel.getCollectedVariables().stream().filter(
                                collectedVariable -> collectedVariable.varId().equals("TESTVARID")
                                        && collectedVariable.iteration().equals(2)
                                        && collectedVariable.value().equals("V2")
                        ).toList().isEmpty()
                ).isNotEmpty();
    }

    @Test
    void findByIdsUEAndQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByIdsInterrogationAndQuestionnaire(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                surveyUnitModel ->
                        surveyUnitModel.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
                        && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
        ).isNotEmpty();
    }

    @Test
    void findByInterrogationIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByInterrogationId(DEFAULT_INTERROGATION_ID)).filteredOn(
                surveyUnitModel ->
                        surveyUnitModel.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
        ).isNotEmpty();
    }

    @Test
    void findByQuestionnaireIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByQuestionnaireId(DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                surveyUnitModel -> surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
                && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
                && surveyUnitModel.getFileDate().getMonth().equals(Month.FEBRUARY)
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

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
                        && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
                        && surveyUnitModel.getFileDate().getMonth().equals(Month.FEBRUARY)
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

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(DEFAULT_INTERROGATION_ID, DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                surveyUnitModel -> surveyUnitModel.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
                        && surveyUnitModel.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
                        && surveyUnitModel.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctInterrogationIdsByQuestionnaireIdTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findDistinctInterrogationIdsByQuestionnaireId(DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                interrogationId -> interrogationId.getInterrogationId().equals(DEFAULT_INTERROGATION_ID)
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findInterrogationIdsByQuestionnaireIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findModesByQuestionnaireId(DEFAULT_QUESTIONNAIRE_ID)).filteredOn(
                mode -> mode.equals(Mode.WEB)
        ).isNotEmpty();
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        addAdditionnalDtoToMongoStub("TESTQUESTIONNAIRE2");

        Assertions.assertThat(surveyUnitServiceStatic.findQuestionnaireIdsByCampaignId("TESTCAMPAIGNID")).isNotEmpty().hasSize(2);

    }

    @Test
    void getAllCampaignsTest() {
        Assertions.assertThat(surveyUnitServiceStatic.findDistinctCampaignIds()).contains("TESTCAMPAIGNID");
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
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTVARID");
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
                .isEqualTo("TESTVARID");
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
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTVARID");
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
                .isEqualTo("TESTVARID");
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
                DEFAULT_INTERROGATION_ID,
                DEFAULT_QUESTIONNAIRE_ID
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);

        Assertions.assertThat(surveyUnitDto.getCollectedVariables().getFirst().getVariableName())
                .isEqualTo("TESTVARID");
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
                .isEqualTo("TESTVARID");
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
        VariableModel externalVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(externalVariableModel);
        externalVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String questionnaireId) {
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel externalVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .iteration(1)
                .build();
        externalVariableList.add(externalVariableModel);
        externalVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .iteration(2)
                .build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V1")
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);
        collectedVariableModel = VariableModel.builder()
                .varId("TESTVARID")
                .value("V2")
                .scope("TESTSCOPE")
                .iteration(2)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariableModel);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .questionnaireId(questionnaireId)
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
                VariableModel.builder()
                        .varId("TESTVARID")
                        .value(externalVariableValue)
                        .iteration(1)
                        .build();
        externalVariableList.add(externalVariableModel);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTVARID")
                .value(collectedVariableValue)
                .scope("TESTSCOPE")
                .iteration(1)
                .parentId("TESTIDPARENT")
                .build();
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(DEFAULT_INTERROGATION_ID)
                .questionnaireId(DEFAULT_QUESTIONNAIRE_ID)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }
}
