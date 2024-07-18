package fr.insee.genesis.controller.utils;


import fr.insee.genesis.domain.dtos.*;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.metadataparserlib.metadata.model.MetadataModel;
import fr.insee.metadataparserlib.metadata.model.Variable;
import fr.insee.metadataparserlib.metadata.model.VariableType;
import fr.insee.metadataparserlib.metadata.model.VariablesMap;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataVerifierTest {
    static List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos = new ArrayList<>();
    static MetadataModel metadataModel;

    // Given
    @BeforeAll
    static void setUp() {
        //Variable definitions
        metadataModel = new MetadataModel();

        //Invalid Collected Variables only
        //1 Variable 1 State 1 Value
        createCase(1,1,1,true,false,"TestUE1",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,false,"TestUE2",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,false,"TestUE3",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,false,"TestUE4",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,false,"TestUE5",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,false,"TestUE6",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,false,"TestUE7",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,false,"TestUE8",testSurveyUnitUpdateDtos,metadataModel.getVariables());


        //With invalid ExternalVariables
        createCase(1,1,1,true,true,"TestUE9",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,true,"TestUE10",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,true,"TestUE11",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,true,"TestUE12",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,true,"TestUE13",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,true,"TestUE14",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,true,"TestUE15",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,true,"TestUE16",testSurveyUnitUpdateDtos,metadataModel.getVariables());


        //Valid variables only
        createCase(1,1,1,false,true,"TestUE17",testSurveyUnitUpdateDtos,metadataModel.getVariables());
        createCase(2,2,2,false,true,"TestUE18",testSurveyUnitUpdateDtos,metadataModel.getVariables());

        //Manual modifications
        //Valid 2nd variable on 5th and 13th case
        SurveyUnitUpdateDto suDto = testSurveyUnitUpdateDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdUE().equals("TestUE5")
        ).toList().get(0);

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");

        suDto = testSurveyUnitUpdateDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdUE().equals("TestUE13")
        ).toList().get(0);

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");
        suDto.getExternalVariables().get(1).getValues().set(0,"1");


        //Valid EDITED variables on 3rd and 7th case for priority test
        SurveyUnitUpdateDto suDtoEdited = testSurveyUnitUpdateDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdUE().equals("TestUE3")
                && surveyUnitUpdateDto.getState().equals(DataState.EDITED)
                ).toList().get(0);

        suDtoEdited.getCollectedVariables().get(0).getValues().set(0,"1");

        suDtoEdited = testSurveyUnitUpdateDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdUE().equals("TestUE7")
                        && surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).toList().get(0);

        suDtoEdited.getCollectedVariables().get(0).getValues().set(0,"1");
        suDtoEdited.getCollectedVariables().get(1).getValues().set(0,"1");

        //Remove EDITED variable on 8th case
        suDtoEdited = testSurveyUnitUpdateDtos.stream().filter(surveyUnitUpdateDto ->
                surveyUnitUpdateDto.getIdUE().equals("TestUE8")
                        && surveyUnitUpdateDto.getState().equals(DataState.EDITED)
        ).toList().get(0);

        suDtoEdited.getCollectedVariables().remove(1);

        //When
        DataVerifier.verifySurveyUnits(testSurveyUnitUpdateDtos,metadataModel.getVariables());
    }

    private static void createCase(int variableNumber, int stateNumber, int valueNumber, boolean hasIncorrectValues, boolean hasExternalVariables, String idUE, List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, VariablesMap variablesMap) {
        for(int stateIndex = 0; stateIndex < stateNumber; stateIndex++){
            List<CollectedVariableDto> variableUpdates = new ArrayList<>();
            List<VariableDto> externalVariables = new ArrayList<>();

            for(int variableIndex = 0; variableIndex < variableNumber; variableIndex++){
                List<String> values = new ArrayList<>();

                if(!variablesMap.hasVariable("testInteger" + variableIndex)) {
                    Variable var = new Variable("testInteger" + variableIndex, metadataModel.getRootGroup(), VariableType.INTEGER, "10");
                    variablesMap.putVariable(var);
                }

                for(int valueIndex = 0; valueIndex < valueNumber; valueIndex++){
                    values.add(hasIncorrectValues
                            && valueIndex % 2 == 0  // Only 1 wrong value if multiple
                            ? "?" : String.valueOf(valueIndex + 1));
                }

                variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                        .idVar("testInteger" + variableIndex)
                        .values(new ArrayList<>(values))
                        .build()
                );

                if(hasExternalVariables){
                    externalVariables.add(VariableDto.builder()
                            .idVar("testInteger" + variableIndex)
                            .values(new ArrayList<>(values))
                            .build()
                    );
                }
            }

            SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
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

            testSurveyUnitUpdateDtos.add(surveyUnitUpdateDto);
        }
    }

    //Then
    //Assertions
    private void assertForcedExistence(List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, String idUE, boolean hasToExist) {
        if(hasToExist)
            assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                    .hasSize(1);
        else
            assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    private void assertCollectedVariableContent(List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitUpdateDtos,idUE,true);

        Optional<SurveyUnitUpdateDto> suDtoOpt = testSurveyUnitUpdateDtos.stream().filter(surveyUnit ->
                        surveyUnit.getIdUE().equals(idUE)
                                && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitUpdateDto suDto = suDtoOpt.get();

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

    private void assertForcedCollectedVariableExistence(List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitUpdateDtos,idUE, true);
        Optional<SurveyUnitUpdateDto> suDtoOpt = testSurveyUnitUpdateDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitUpdateDto suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertForcedExternalVariableExistence(List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitUpdateDtos,idUE, true);
        Optional<SurveyUnitUpdateDto> suDtoOpt = testSurveyUnitUpdateDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitUpdateDto suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertExternalVariableContent(List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitUpdateDtos,idUE,true);

        Optional<SurveyUnitUpdateDto> suDtoOpt = testSurveyUnitUpdateDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitUpdateDto suDto = suDtoOpt.get();

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
        assertForcedExistence(testSurveyUnitUpdateDtos, "TestUE1", true);
    }

    @Test
    @DisplayName("If there is no invalid values, there must be one FORCED document")
    void forcedNoExistenceTest(){
        assertForcedExistence(testSurveyUnitUpdateDtos, "TestUE17", false);
    }

    @Test
    @DisplayName("The invalid values must be replaced by empty string")
    void invalidValueReplaceTest(){
        assertCollectedVariableContent(testSurveyUnitUpdateDtos,"TestUE1","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitUpdateDtos,"TestUE2","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitUpdateDtos,"TestUE6","testInteger0",0,"");

        assertExternalVariableContent(testSurveyUnitUpdateDtos,"TestUE9","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitUpdateDtos,"TestUE10","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitUpdateDtos,"TestUE14","testInteger0",0,"");

    }

    @Test
    @DisplayName("The FORCED document must contain only the invalid variables")
    void variableCountTest(){
        //Collected
        //5 à 8
        assertForcedCollectedVariableExistence(testSurveyUnitUpdateDtos, "TestUE5", "testInteger0", true);
        assertForcedCollectedVariableExistence(testSurveyUnitUpdateDtos, "TestUE5", "testInteger1", false);

        //External
        //13 à 16
        assertForcedExternalVariableExistence(testSurveyUnitUpdateDtos, "TestUE13", "testInteger0", true);
        assertForcedExternalVariableExistence(testSurveyUnitUpdateDtos, "TestUE13", "testInteger1", false);
    }

    @Test
    @DisplayName("The dataverifier must verify only the most priority variables")
    void priorityTest(){
        assertForcedExistence(testSurveyUnitUpdateDtos, "TestUE3",false);
        assertForcedExistence(testSurveyUnitUpdateDtos, "TestUE7",false);
    }

    @Test
    @DisplayName("If a variable is absent in more priority variable but invalid in less priority," +
                        "the variable must be present in FORCED")
    void priorityVariableAbsenceTest(){
        assertForcedCollectedVariableExistence(testSurveyUnitUpdateDtos,"TestUE8","testInteger1",true);
    }
}
