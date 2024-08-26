package fr.insee.genesis.controller.utils;


import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class DataVerifierTest {
    static List<SurveyUnitDto> testSurveyUnitDtos = new ArrayList<>();
    static MetadataModel metadataModel;

    // Given
    @BeforeAll
    static void setUp() {
        //Variable definitions
        metadataModel = new MetadataModel();

        //Invalid Collected Variables only
        //1 Variable 1 State 1 Value
        createCase(1,1,1,true,false,"TestUE1",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,false,"TestUE2",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,false,"TestUE3",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,false,"TestUE4",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,false,"TestUE5",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,false,"TestUE6",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,false,"TestUE7",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,false,"TestUE8",testSurveyUnitDtos,metadataModel.getVariables());


        //With invalid ExternalVariables
        createCase(1,1,1,true,true,"TestUE9",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 1 State 2 Values
        createCase(1,1,2,true,true,"TestUE10",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 2 States 1 Value
        createCase(1,2,1,true,true,"TestUE11",testSurveyUnitDtos,metadataModel.getVariables());

        //1 Variable 2 States 2 Values
        createCase(1,2,2,true,true,"TestUE12",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 1 State 1 Value
        createCase(2,1,1,true,true,"TestUE13",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 1 State 2 Values
        createCase(2,1,2,true,true,"TestUE14",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 2 States 1 Value
        createCase(2,2,1,true,true,"TestUE15",testSurveyUnitDtos,metadataModel.getVariables());

        //2 Variables 2 State 2 Value
        createCase(2,2,2,true,true,"TestUE16",testSurveyUnitDtos,metadataModel.getVariables());


        //Valid variables only
        createCase(1,1,1,false,true,"TestUE17",testSurveyUnitDtos,metadataModel.getVariables());
        createCase(2,2,2,false,true,"TestUE18",testSurveyUnitDtos,metadataModel.getVariables());

        //Manual modifications
        //Valid 2nd variable on 5th and 13th case
        SurveyUnitDto suDto = testSurveyUnitDtos.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE5")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");

        suDto = testSurveyUnitDtos.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE13")
        ).toList().getFirst();

        suDto.getCollectedVariables().get(1).getValues().set(0,"1");
        suDto.getExternalVariables().get(1).getValues().set(0,"1");


        //Valid EDITED variables on 3rd and 7th case for priority test
        SurveyUnitDto suDtoEdited = testSurveyUnitDtos.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE3")
                && surveyUnitDto.getState().equals(DataState.EDITED)
                ).toList().getFirst();

        suDtoEdited.getCollectedVariables().getFirst().getValues().set(0,"1");

        suDtoEdited = testSurveyUnitDtos.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE7")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().get(0).getValues().set(0,"1");
        suDtoEdited.getCollectedVariables().get(1).getValues().set(0,"1");

        //Remove EDITED variable on 8th case
        suDtoEdited = testSurveyUnitDtos.stream().filter(surveyUnitDto ->
                surveyUnitDto.getIdUE().equals("TestUE8")
                        && surveyUnitDto.getState().equals(DataState.EDITED)
        ).toList().getFirst();

        suDtoEdited.getCollectedVariables().remove(1);

        //When
        DataVerifier.verifySurveyUnits(testSurveyUnitDtos,metadataModel.getVariables());
    }

    private static void createCase(int variableNumber, int stateNumber, int valueNumber, boolean hasIncorrectValues, boolean hasExternalVariables, String idUE, List<SurveyUnitDto> testSurveyUnitDtos, VariablesMap variablesMap) {
        for(int stateIndex = 0; stateIndex < stateNumber; stateIndex++){
            List<CollectedVariableDto> variableUpdates = new ArrayList<>();
            List<VariableDto> externalVariables = new ArrayList<>();

            for(int variableIndex = 0; variableIndex < variableNumber; variableIndex++){
                List<String> values = new ArrayList<>();

                if(!variablesMap.hasVariable("testInteger" + variableIndex)) {
                    Variable varTest = new Variable("testInteger" + variableIndex, metadataModel.getRootGroup(), VariableType.INTEGER, "10");
                    variablesMap.putVariable(varTest);
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

            SurveyUnitDto surveyUnitDto = SurveyUnitDto.builder()
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

            testSurveyUnitDtos.add(surveyUnitDto);
        }
    }

    //Then
    //Assertions
    private void assertForcedExistence(List<SurveyUnitDto> testSurveyUnitDtos, String idUE, boolean hasToExist) {
        if(hasToExist)
            assertThat(testSurveyUnitDtos).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                    .hasSize(1);
        else
            assertThat(testSurveyUnitDtos).filteredOn(surveyUnit ->
                            surveyUnit.getIdUE().equals(idUE)
                                    && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    private void assertCollectedVariableContent(List<SurveyUnitDto> testSurveyUnitDtos, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitDtos,idUE,true);

        Optional<SurveyUnitDto> suDtoOpt = testSurveyUnitDtos.stream().filter(surveyUnit ->
                        surveyUnit.getIdUE().equals(idUE)
                                && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitDto suDto = suDtoOpt.get();

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

    private void assertForcedCollectedVariableExistence(List<SurveyUnitDto> testSurveyUnitDtos, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitDtos,idUE, true);
        Optional<SurveyUnitDto> suDtoOpt = testSurveyUnitDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitDto suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getCollectedVariables().stream().filter(collectedVariableDto -> collectedVariableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertForcedExternalVariableExistence(List<SurveyUnitDto> testSurveyUnitDtos, String idUE, String variableName, boolean hasToExist) {
        assertForcedExistence(testSurveyUnitDtos,idUE, true);
        Optional<SurveyUnitDto> suDtoOpt = testSurveyUnitDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();
        assertThat(suDtoOpt).isPresent();

        SurveyUnitDto suDto = suDtoOpt.get();

        if(hasToExist)
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isNotEmpty();
        else
            assertThat(suDto.getExternalVariables().stream().filter(variableDto -> variableDto.getIdVar().equals(variableName)).toList()).isEmpty();
    }

    private void assertExternalVariableContent(List<SurveyUnitDto> testSurveyUnitDtos, String idUE, String variableName, int valueIndex, String expectedContent) {
        assertForcedExistence(testSurveyUnitDtos,idUE,true);

        Optional<SurveyUnitDto> suDtoOpt = testSurveyUnitDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals(idUE)
                        && surveyUnit.getState() == DataState.FORCED).findFirst();

        assertThat(suDtoOpt).isPresent();


        SurveyUnitDto suDto = suDtoOpt.get();

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
        assertForcedExistence(testSurveyUnitDtos, "TestUE1", true);
    }

    @Test
    @DisplayName("If there is no invalid values, there must be one FORCED document")
    void forcedNoExistenceTest(){
        assertForcedExistence(testSurveyUnitDtos, "TestUE17", false);
    }

    @Test
    @DisplayName("The invalid values must be replaced by empty string")
    void invalidValueReplaceTest(){
        assertCollectedVariableContent(testSurveyUnitDtos,"TestUE1","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitDtos,"TestUE2","testInteger0",0,"");
        assertCollectedVariableContent(testSurveyUnitDtos,"TestUE6","testInteger0",0,"");

        assertExternalVariableContent(testSurveyUnitDtos,"TestUE9","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitDtos,"TestUE10","testInteger0",0,"");
        assertExternalVariableContent(testSurveyUnitDtos,"TestUE14","testInteger0",0,"");

    }

    @Test
    @DisplayName("The FORCED document must contain only the invalid variables")
    void variableCountTest(){
        //Collected
        //5 à 8
        assertForcedCollectedVariableExistence(testSurveyUnitDtos, "TestUE5", "testInteger0", true);
        assertForcedCollectedVariableExistence(testSurveyUnitDtos, "TestUE5", "testInteger1", false);

        //External
        //13 à 16
        assertForcedExternalVariableExistence(testSurveyUnitDtos, "TestUE13", "testInteger0", true);
        assertForcedExternalVariableExistence(testSurveyUnitDtos, "TestUE13", "testInteger1", false);
    }

    @Test
    @DisplayName("The dataverifier must verify only the most priority variables")
    void priorityTest(){
        assertForcedExistence(testSurveyUnitDtos, "TestUE3",false);
        assertForcedExistence(testSurveyUnitDtos, "TestUE7",false);
    }

    @Test
    @DisplayName("If a variable is absent in more priority variable but invalid in less priority," +
                        "the variable must be present in FORCED")
    void priorityVariableAbsenceTest(){
        assertForcedCollectedVariableExistence(testSurveyUnitDtos,"TestUE8","testInteger1",true);
    }
}
