package fr.insee.genesis.domain.service;

import fr.insee.genesis.controller.dto.SurveyUnitDto;
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
    static final String defaultInterrogationId = "TESTINTERROGATIONID";
    static final String defaultQuestionnaireId = "TESTQUESTIONNAIREID";

    @BeforeAll
    static void init(){
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        surveyUnitServiceStatic = new SurveyUnitService(surveyUnitPersistencePortStub);
    }

    @BeforeEach
    void reset(){
        surveyUnitPersistencePortStub.getMongoStub().clear();
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(defaultInterrogationId)
                .questionnaireId(defaultQuestionnaireId)
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
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariableDto = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariableDto);

        newSurveyUnitModelList.add(
                SurveyUnitModel.builder()
                        .campaignId("TESTCAMPAIGNID")
                        .mode(Mode.WEB)
                        .interrogationId("TESTINTERROGATIONID2")
                        .questionnaireId(defaultQuestionnaireId)
                        .state(DataState.COLLECTED)
                        .fileDate(LocalDateTime.of(2023,1,1,0,0,0))
                        .recordDate(LocalDateTime.of(2024,1,1,0,0,0))
                        .externalVariables(externalVariableList)
                        .collectedVariables(collectedVariableList)
                        .build()
        );

        surveyUnitServiceStatic.saveSurveyUnits(newSurveyUnitModelList);

        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).filteredOn(surveyUnitDto ->
                surveyUnitDto.getCampaignId().equals("TESTCAMPAIGNID")
                && surveyUnitDto.getMode().equals(Mode.WEB)
                && surveyUnitDto.getInterrogationId().equals("TESTINTERROGATIONID2")
                && surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
                && surveyUnitDto.getState().equals(DataState.COLLECTED)
                && surveyUnitDto.getFileDate().equals(LocalDateTime.of(2023,1,1,0,0,0))
                && surveyUnitDto.getRecordDate().equals(LocalDateTime.of(2024,1,1,0,0,0))
                && !surveyUnitDto.getExternalVariables().stream().filter(
                        externalVariable -> externalVariable.getVarId().equals("TESTIDVAR")
                        && externalVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                        && !surveyUnitDto.getCollectedVariables().stream().filter(
                        collectedVariable -> collectedVariable.getVarId().equals("TESTIDVAR")
                                && collectedVariable.getValues().containsAll(List.of(new String[]{"V1", "V2"}))
                ).toList().isEmpty()
                ).isNotEmpty();
    }

    @Test
    void findByIdsUEAndQuestionnaireTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByIdsInterrogationAndQuestionnaire(defaultInterrogationId, defaultQuestionnaireId)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getInterrogationId().equals(defaultInterrogationId)
                        && surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
        ).isNotEmpty();
    }

    @Test
    void findByInterrogationIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByInterrogationId(defaultInterrogationId)).filteredOn(
                surveyUnitDto ->
                        surveyUnitDto.getInterrogationId().equals(defaultInterrogationId)
        ).isNotEmpty();
    }

    @Test
    void findByQuestionnaireIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findByQuestionnaireId(defaultQuestionnaireId)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
        ).isNotEmpty();
    }

    @Test
    void findLatestByIdAndByModeTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(defaultInterrogationId, defaultQuestionnaireId)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(defaultInterrogationId)
                && surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
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

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(defaultInterrogationId, defaultQuestionnaireId)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(defaultInterrogationId)
                        && surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
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

        Assertions.assertThat(surveyUnitServiceStatic.findLatestByIdAndByQuestionnaireId(defaultInterrogationId, defaultQuestionnaireId)).filteredOn(
                surveyUnitDto -> surveyUnitDto.getInterrogationId().equals(defaultInterrogationId)
                        && surveyUnitDto.getQuestionnaireId().equals(defaultQuestionnaireId)
                        && surveyUnitDto.getFileDate().getMonth().equals(Month.FEBRUARY)
        ).isNotEmpty();
    }

    @Test
    void findDistinctInterrogationIdsByQuestionnaireIdTest(){
        addAdditionnalDtoToMongoStub();

        Assertions.assertThat(surveyUnitServiceStatic.findDistinctInterrogationIdsByQuestionnaireId(defaultQuestionnaireId)).filteredOn(
                surveyUnitId -> surveyUnitId.getInterrogationId().equals(defaultInterrogationId)
        ).isNotEmpty().hasSize(1);
    }

    @Test
    void findInterrogationIdsByQuestionnaireIdTest(){
        Assertions.assertThat(surveyUnitServiceStatic.findModesByQuestionnaireId(defaultQuestionnaireId)).filteredOn(
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
                defaultInterrogationId,
                defaultQuestionnaireId
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(defaultInterrogationId);

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
                defaultInterrogationId,
                defaultQuestionnaireId
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(defaultInterrogationId);

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
                defaultInterrogationId,
                defaultQuestionnaireId
        );


        //Then
        Assertions.assertThat(surveyUnitDto.getSurveyUnitId()).isEqualTo(defaultInterrogationId);

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
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(defaultInterrogationId)
                .questionnaireId(defaultQuestionnaireId)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023,2,2,0,0,0))
                .recordDate(LocalDateTime.of(2024,2,2,0,0,0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }

    private void addAdditionnalDtoToMongoStub(String questionnaireId) {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(defaultInterrogationId)
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
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{externalVariableValue})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{collectedVariableValue}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel recentDTO = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId(defaultInterrogationId)
                .questionnaireId(defaultQuestionnaireId)
                .state(state)
                .fileDate(fileDate)
                .recordDate(recordDate)
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build();
        surveyUnitPersistencePortStub.getMongoStub().add(recentDTO);
    }
}
