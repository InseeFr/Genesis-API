package fr.insee.genesis.controller.utils;


import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.utils.DataVerifier;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataVerifierTest {
    static List<SurveyUnitModel> testSurveyUnitModels = new ArrayList<>();
    static MetadataModel metadataModel;

    // Given
    @BeforeAll
    static void setUp() {
        //Variable definitions
        metadataModel = new MetadataModel();

        //Invalid Collected Variables only
        //1 Variable 1 State 1 Value
        createCase(1,1,1,true,false,"TestUE1", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,false,"TestUE2", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,false,"TestUE3", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,false,"TestUE4", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,false,"TestUE5", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,false,"TestUE6", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,false,"TestUE7", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,false,"TestUE8", testSurveyUnitModels,metadataModel.getVariables());


        //With invalid ExternalVariables
        createCase(1,1,1,true,true,"TestUE9", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,true,"TestUE10", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,true,"TestUE11", testSurveyUnitModels,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,true,"TestUE12", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,true,"TestUE13", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,true,"TestUE14", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,true,"TestUE15", testSurveyUnitModels,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,true,"TestUE16", testSurveyUnitModels,metadataModel.getVariables());


        //Valid variables only
        createCase(1,1,1,false,true,"TestUE17", testSurveyUnitModels,metadataModel.getVariables());
        createCase(2,2,2,false,true,"TestUE18", testSurveyUnitModels,metadataModel.getVariables());

        //Manual modifications
        //Valid 2nd variable on 5th and 13th case
        SurveyUnitModel suDto = testSurveyUnitModels.stream().filter(surveyUnitDto ->
                surveyUnitDto.getInterrogationId().equals("TestUE5")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).values().set(0,"1");

        suDto = testSurveyUnitModels.stream().filter(surveyUnitDto ->
                surveyUnitDto.getInterrogationId().equals("TestUE13")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).values().set(0,"1");
        suDto.getExternalVariables().get(1).values().set(0,"1");


        //Valid EDITED variables on 3rd and 7th case for priority test
        SurveyUnitModel suDtoEdited = testSurveyUnitModels.stream().filter(surveyUnitDto ->
                surveyUnitDto.getInterrogationId().equals("TestUE3")
                && surveyUnitDto.getState().equals(DataState.EDITED)
                ).toList().getFirst();

        suDtoEdited.getCollectedVariables().getFirst().values().set(0,"1");

        suDtoEdited = testSurveyUnitModels.stream().filter(surveyUnitDto ->
                surveyUnitDto.getInterrogationId().equals("TestUE7")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().get(0).values().set(0,"1");
        suDtoEdited.getCollectedVariables().get(1).values().set(0,"1");

        //Remove EDITED variable on 8th case
        suDtoEdited = testSurveyUnitModels.stream().filter(surveyUnitDto ->
                surveyUnitDto.getInterrogationId().equals("TestUE8")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().remove(1);

        //When
        DataVerifier.verifySurveyUnits(testSurveyUnitModels,metadataModel.getVariables());
    }

    private static void createCase(int variableNumber, int stateNumber, int valueNumber, boolean hasIncorrectValues, boolean hasExternalVariables, String idUE, List<SurveyUnitModel> testSurveyUnitModels, VariablesMap variablesMap) {
        for(int stateIndex = 0; stateIndex < stateNumber; stateIndex++){
            List<VariableModel> collectedVariables = new ArrayList<>();
            List<VariableModel> externalVariables = new ArrayList<>();

            for(int variableIndex = 0; variableIndex < variableNumber; variableIndex++){
                List<String> values = new ArrayList<>();

                if(!variablesMap.hasVariable("testInteger" + variableIndex)) {
                    fr.insee.bpm.metadata.model.Variable varTest = new fr.insee.bpm.metadata.model.Variable("testInteger" + variableIndex, metadataModel.getRootGroup(), VariableType.INTEGER, "10");
                    variablesMap.putVariable(varTest);
                }

                for(int valueIndex = 0; valueIndex < valueNumber; valueIndex++){
                    values.add(hasIncorrectValues
                            && valueIndex % 2 == 0  // Only 1 wrong value if multiple
                            ? "?" : String.valueOf(valueIndex + 1));
                }

                collectedVariables.add(VariableModel.builder()
                        .idVar("testInteger" + variableIndex)
                        .values(new ArrayList<>(values))
                        .build()
                );

                if(hasExternalVariables){
                    externalVariables.add(VariableModel.builder()
                            .idVar("testInteger" + variableIndex)
                            .values(new ArrayList<>(values))
                            .build()
                    );
                }
            }

            SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                    .questionnaireId("IdQuest1")
                    .campaignId("IdCampaign1")
                    .interrogationId(idUE)
                    .state(stateIndex % 2 == 0 ? DataState.COLLECTED : DataState.EDITED)
                    .mode(Mode.WEB)
                    .recordDate(LocalDateTime.now())
                    .collectedVariables(new ArrayList<>(collectedVariables))
                    .externalVariables(new ArrayList<>(externalVariables))
                    .build();

            collectedVariables.clear();
            externalVariables.clear();

            testSurveyUnitModels.add(surveyUnitModel);
        }
    }

    //Then
    //Assertions
    private void assertForcedExistence(List<SurveyUnitModel> testSurveyUnitModels, String idUE, boolean hasToExist) {
        if(hasToExist)
            assertThat(testSurveyUnitModels).filteredOn(surveyUnit ->
                            surveyUnit.getInterrogationId().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                    .hasSize(1);
        else
            assertThat(testSurveyUnitModels).filteredOn(surveyUnit ->
                            surveyUnit.getInterrogationId().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    private void assertCollectedVariableContent(List<SurveyUnitModel> testSurveyUnitModels, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitModels,idUE,true);

        Optional<SurveyUnitModel> suDtoOpt = testSurveyUnitModels.stream().filter(surveyUnit ->
                        surveyUnit.getInterrogationId().equals(idUE)
                                && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitModel suDto = suDtoOpt.get();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.idVar().equals(variableName)
                )).isNotEmpty();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.idVar().equals(variableName)
        ).findFirst()).isPresent();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.idVar().equals(variableName)
        ).findFirst().get().values().get(valueIndex)).isEqualTo(expectedContent);
    }

    private void assertForcedCollectedVariableExistence(List<SurveyUnitModel> testSurveyUnitModels, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitModels,idUE, true);
        Optional<SurveyUnitModel> suDtoOpt = testSurveyUnitModels.stream().filter(surveyUnit ->
                surveyUnit.getInterrogationId().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitModel suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.idVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.idVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertForcedExternalVariableExistence(List<SurveyUnitModel> testSurveyUnitModels, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitModels,idUE, true);
        Optional<SurveyUnitModel> suDtoOpt = testSurveyUnitModels.stream().filter(surveyUnit ->
                surveyUnit.getInterrogationId().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitModel suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.idVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.idVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertExternalVariableContent(List<SurveyUnitModel> testSurveyUnitModels, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitModels,idUE,true);

        Optional<SurveyUnitModel> suDtoOpt = testSurveyUnitModels.stream().filter(surveyUnit ->
                surveyUnit.getInterrogationId().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitModel suDto = suDtoOpt.get();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.idVar().equals(variableName)
        )).isNotEmpty();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.idVar().equals(variableName)
        ).findFirst()).isPresent();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.idVar().equals(variableName)
        ).findFirst().get().values().get(valueIndex)).isEqualTo(expectedContent);
    }

    //Tests
    @Test
    @DisplayName("If there is invalid values, there must be one FORCED document")
    void forcedExistenceTest(){
        assertForcedExistence(testSurveyUnitModels, "TestUE1", true);
    }

    @Test
    @DisplayName("If there is no invalid values, there must be one FORCED document")
    void forcedNoExistenceTest(){
        assertForcedExistence(testSurveyUnitModels, "TestUE17", false);
    }

    @Test
    @DisplayName("The invalid values must be replaced by empty string")
    void invalidValueReplaceTest(){
        assertCollectedVariableContent(testSurveyUnitModels,"TestUE1","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitModels,"TestUE2","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitModels,"TestUE6","testInteger0",0,"");

        assertExternalVariableContent(testSurveyUnitModels,"TestUE9","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitModels,"TestUE10","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitModels,"TestUE14","testInteger0",0,"");

    }

    @Test
    @DisplayName("The FORCED document must contain only the invalid variables")
    void variableCountTest(){
        //Collected
        //5 à 8
        assertForcedCollectedVariableExistence(testSurveyUnitModels, "TestUE5", "testInteger0", true);
        assertForcedCollectedVariableExistence(testSurveyUnitModels, "TestUE5", "testInteger1", false);

        //External
        //13 à 16
        assertForcedExternalVariableExistence(testSurveyUnitModels, "TestUE13", "testInteger0", true);
        assertForcedExternalVariableExistence(testSurveyUnitModels, "TestUE13", "testInteger1", false);
    }

    @Test
    @DisplayName("The dataverifier must verify only the most priority variables")
    void priorityTest(){
        assertForcedExistence(testSurveyUnitModels, "TestUE3",false);
        assertForcedExistence(testSurveyUnitModels, "TestUE7",false);
    }

    @Test
    @DisplayName("If a variable is absent in more priority variable but invalid in less priority," +
                        "the variable must be present in FORCED")
    void priorityVariableAbsenceTest(){
        assertForcedCollectedVariableExistence(testSurveyUnitModels,"TestUE8","testInteger1",true);
    }
}
