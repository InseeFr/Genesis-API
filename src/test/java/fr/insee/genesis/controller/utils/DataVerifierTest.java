package fr.insee.genesis.controller.utils;


import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnit;
import fr.insee.genesis.domain.model.surveyunit.Variable;
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
    static List<SurveyUnit> testSurveyUnits = new ArrayList<>();
    static MetadataModel metadataModel;

    // Given
    @BeforeAll
    static void setUp() {
        //Variable definitions
        metadataModel = new MetadataModel();

        //Invalid Collected Variables only
        //1 Variable 1 State 1 Value
        createCase(1,1,1,true,false,"TestUE1", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,false,"TestUE2", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,false,"TestUE3", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,false,"TestUE4", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,false,"TestUE5", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,false,"TestUE6", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,false,"TestUE7", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,false,"TestUE8", testSurveyUnits,metadataModel.getVariables());


        //With invalid ExternalVariables
        createCase(1,1,1,true,true,"TestUE9", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,true,"TestUE10", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,true,"TestUE11", testSurveyUnits,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,true,"TestUE12", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,true,"TestUE13", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,true,"TestUE14", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,true,"TestUE15", testSurveyUnits,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,true,"TestUE16", testSurveyUnits,metadataModel.getVariables());


        //Valid variables only
        createCase(1,1,1,false,true,"TestUE17", testSurveyUnits,metadataModel.getVariables());
        createCase(2,2,2,false,true,"TestUE18", testSurveyUnits,metadataModel.getVariables());

        //Manual modifications
        //Valid 2nd variable on 5th and 13th case
        SurveyUnit suDto = testSurveyUnits.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE5")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");

        suDto = testSurveyUnits.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE13")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");
        suDto.getExternalVariables().get(1).getValues().set(0,"1");


        //Valid EDITED variables on 3rd and 7th case for priority test
        SurveyUnit suDtoEdited = testSurveyUnits.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE3")
                && surveyUnitDto.getState().equals(DataState.EDITED)
                ).toList().getFirst();

        suDtoEdited.getCollectedVariables().getFirst().getValues().set(0,"1");

        suDtoEdited = testSurveyUnits.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE7")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().get(0).getValues().set(0,"1");
        suDtoEdited.getCollectedVariables().get(1).getValues().set(0,"1");

        //Remove EDITED variable on 8th case
        suDtoEdited = testSurveyUnits.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE8")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().remove(1);

        //When
        DataVerifier.verifySurveyUnits(testSurveyUnits,metadataModel.getVariables());
    }

    private static void createCase(int variableNumber, int stateNumber, int valueNumber, boolean hasIncorrectValues, boolean hasExternalVariables, String idUE, List<SurveyUnit> testSurveyUnits, VariablesMap variablesMap) {
        for(int stateIndex = 0; stateIndex < stateNumber; stateIndex++){
            List<CollectedVariable> variableUpdates = new ArrayList<>();
            List<Variable> externalVariables = new ArrayList<>();

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

                variableUpdates.add(CollectedVariable.collectedVariableBuilder()
                        .idVar("testInteger" + variableIndex)
                        .values(new ArrayList<>(values))
                        .build()
                );

                if(hasExternalVariables){
                    externalVariables.add(Variable.builder()
                            .idVar("testInteger" + variableIndex)
                            .values(new ArrayList<>(values))
                            .build()
                    );
                }
            }

            SurveyUnit surveyUnit = SurveyUnit.builder()
                    .idQuest("IdQuest1")
                    .idCampaign("IdCampaign1")
                    .idUE(idUE)
                    .state(stateIndex % 2 == 0 ? DataState.COLLECTED : DataState.EDITED)
                    .mode(Mode.WEB)
                    .recordDate(LocalDateTime.now())
                    .collectedVariables(new ArrayList<>(variableUpdates))
                    .externalVariables(new ArrayList<>(externalVariables))
                    .build();

            variableUpdates.clear();
            externalVariables.clear();

            testSurveyUnits.add(surveyUnit);
        }
    }

    //Then
    //Assertions
    private void assertForcedExistence(List<SurveyUnit> testSurveyUnits, String idUE, boolean hasToExist) {
        if(hasToExist)
            assertThat(testSurveyUnits).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                    .hasSize(1);
        else
            assertThat(testSurveyUnits).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    private void assertCollectedVariableContent(List<SurveyUnit> testSurveyUnits, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnits,idUE,true);

        Optional<SurveyUnit> suDtoOpt = testSurveyUnits.stream().filter(surveyUnit ->
                        surveyUnit.getIdUE().equals(idUE)
                                && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnit suDto = suDtoOpt.get();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals(variableName)
                )).isNotEmpty();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals(variableName)
        ).findFirst()).isPresent();

        assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto ->
                collectedVariableDto.getIdVar().equals(variableName)
        ).findFirst().get().getValues().get(valueIndex)).isEqualTo(expectedContent);
    }

    private void assertForcedCollectedVariableExistence(List<SurveyUnit> testSurveyUnits, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnits,idUE, true);
        Optional<SurveyUnit> suDtoOpt = testSurveyUnits.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnit suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertForcedExternalVariableExistence(List<SurveyUnit> testSurveyUnits, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnits,idUE, true);
        Optional<SurveyUnit> suDtoOpt = testSurveyUnits.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnit suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertExternalVariableContent(List<SurveyUnit> testSurveyUnits, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnits,idUE,true);

        Optional<SurveyUnit> suDtoOpt = testSurveyUnits.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnit suDto = suDtoOpt.get();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.getIdVar().equals(variableName)
        )).isNotEmpty();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.getIdVar().equals(variableName)
        ).findFirst()).isPresent();

        assertThat(suDto.getExternalVariables().stream().filter(variableDto ->
                variableDto.getIdVar().equals(variableName)
        ).findFirst().get().getValues().get(valueIndex)).isEqualTo(expectedContent);
    }

    //Tests
    @Test
    @DisplayName("If there is invalid values, there must be one FORCED document")
    void forcedExistenceTest(){
        assertForcedExistence(testSurveyUnits, "TestUE1", true);
    }

    @Test
    @DisplayName("If there is no invalid values, there must be one FORCED document")
    void forcedNoExistenceTest(){
        assertForcedExistence(testSurveyUnits, "TestUE17", false);
    }

    @Test
    @DisplayName("The invalid values must be replaced by empty string")
    void invalidValueReplaceTest(){
        assertCollectedVariableContent(testSurveyUnits,"TestUE1","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnits,"TestUE2","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnits,"TestUE6","testInteger0",0,"");

        assertExternalVariableContent(testSurveyUnits,"TestUE9","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnits,"TestUE10","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnits,"TestUE14","testInteger0",0,"");

    }

    @Test
    @DisplayName("The FORCED document must contain only the invalid variables")
    void variableCountTest(){
        //Collected
        //5 à 8
        assertForcedCollectedVariableExistence(testSurveyUnits, "TestUE5", "testInteger0", true);
        assertForcedCollectedVariableExistence(testSurveyUnits, "TestUE5", "testInteger1", false);

        //External
        //13 à 16
        assertForcedExternalVariableExistence(testSurveyUnits, "TestUE13", "testInteger0", true);
        assertForcedExternalVariableExistence(testSurveyUnits, "TestUE13", "testInteger1", false);
    }

    @Test
    @DisplayName("The dataverifier must verify only the most priority variables")
    void priorityTest(){
        assertForcedExistence(testSurveyUnits, "TestUE3",false);
        assertForcedExistence(testSurveyUnits, "TestUE7",false);
    }

    @Test
    @DisplayName("If a variable is absent in more priority variable but invalid in less priority," +
                        "the variable must be present in FORCED")
    void priorityVariableAbsenceTest(){
        assertForcedCollectedVariableExistence(testSurveyUnits,"TestUE8","testInteger1",true);
    }
}
