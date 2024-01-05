package fr.insee.genesis.controller.utils;


import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import fr.insee.genesis.domain.dtos.*;
import fr.insee.genesis.domain.dtos.VariableDto;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DataVerifierTest {
    static List<SurveyUnitUpdateDto> testSurveyUnitUpdateDtos = new ArrayList<>();
    static int initialSize = 0;
    static VariablesMap variablesMap;

    // Parameters
    static int EXPECTED_VOLUMETRY = 21;

    // Given
    @BeforeAll
    static void setUp() {
        //Variable definitions
        variablesMap = new VariablesMap();
        Variable var1 = new Variable("testString", variablesMap.getRootGroup(), VariableType.STRING, "10");
        Variable var2 = new Variable("testInteger", variablesMap.getRootGroup(), VariableType.INTEGER, "10");
        Variable var3 = new Variable("testNumber", variablesMap.getRootGroup(), VariableType.NUMBER, "10");
        Variable var4 = new Variable("testBoolean", variablesMap.getRootGroup(), VariableType.BOOLEAN, "10");
        Variable var5 = new Variable("testDate", variablesMap.getRootGroup(), VariableType.DATE, "20");
        Variable var6 = new Variable("testStringExt", variablesMap.getRootGroup(), VariableType.STRING, "10");
        Variable var7 = new Variable("testIntegerExt", variablesMap.getRootGroup(), VariableType.INTEGER, "10");
        Variable var8 = new Variable("testNumberExt", variablesMap.getRootGroup(), VariableType.NUMBER, "10");
        Variable var9 = new Variable("testBooleanExt", variablesMap.getRootGroup(), VariableType.BOOLEAN, "10");
        Variable var10 = new Variable("testDateExt", variablesMap.getRootGroup(), VariableType.DATE, "20");
        variablesMap.putVariable(var1);
        variablesMap.putVariable(var2);
        variablesMap.putVariable(var3);
        variablesMap.putVariable(var4);
        variablesMap.putVariable(var5);
        variablesMap.putVariable(var6);
        variablesMap.putVariable(var7);
        variablesMap.putVariable(var8);
        variablesMap.putVariable(var9);
        variablesMap.putVariable(var10);

        //Case 1 : Correct variables types
        List<CollectedVariableDto> variableUpdates = new ArrayList<>();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        List<VariableDto> externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto correctSurveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE1")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(correctSurveyUnitUpdateDto);
        variableUpdates.clear();
        externalVariables.clear();

        //Case 2 : Incorrect update variable type (1 value)
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2000-99-99"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );

        SurveyUnitUpdateDto incorrectUpdateVariableSurveyUnitUpdateDto1 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE2")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(incorrectUpdateVariableSurveyUnitUpdateDto1);
        variableUpdates.clear();
        externalVariables.clear();


        //Case 3 : Incorrect update variable type (2 values)
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1","?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );

        SurveyUnitUpdateDto incorrectUpdateVariableSurveyUnitUpdateDto2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE3")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(incorrectUpdateVariableSurveyUnitUpdateDto2);
        variableUpdates.clear();
        externalVariables.clear();



        // Case 4 : Incorrect external variable type (1 value)
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );

        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2000-99-99"})))
                .build()
        );

        SurveyUnitUpdateDto incorrectExternalVariableSurveyUnitUpdateDto1 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE4")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(incorrectExternalVariableSurveyUnitUpdateDto1);
        variableUpdates.clear();
        externalVariables.clear();

        // Case 5 : Incorrect external variable type (2 values)
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1","?"})))
                .build()
        );

        SurveyUnitUpdateDto incorrectExternalVariableSurveyUnitUpdateDto2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE5")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(incorrectExternalVariableSurveyUnitUpdateDto2);
        variableUpdates.clear();
        externalVariables.clear();

        //Assert correct order for the 5 first survey units
        for(int i = 0; i < testSurveyUnitUpdateDtos.size(); i++)
            assertThat(testSurveyUnitUpdateDtos.get(i).getIdUE()).isEqualTo("TestUE" + (i+1));

        // for DataStates tests

        // Case 6 : Both COLLECTED and EDITED invalid
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto invalidSurveyUnitUpdateDtoCollected = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE6")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(invalidSurveyUnitUpdateDtoCollected);
        variableUpdates.clear();
        externalVariables.clear();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto invalidSurveyUnitUpdateDtoEdited = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE6")
                .state(DataState.EDITED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(invalidSurveyUnitUpdateDtoEdited);
        variableUpdates.clear();
        externalVariables.clear();

        // Case 7 : COLLECTED valid and EDITED invalid
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto validSurveyUnitUpdateDtoCollected = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE7")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(validSurveyUnitUpdateDtoCollected);
        variableUpdates.clear();
        externalVariables.clear();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto invalidSurveyUnitUpdateDtoEdited2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE7")
                .state(DataState.EDITED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(invalidSurveyUnitUpdateDtoEdited2);
        variableUpdates.clear();
        externalVariables.clear();


        // Case 8 : COLLECTED invalid and EDITED valid
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto invalidSurveyUnitUpdateDtoCollected2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE8")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(invalidSurveyUnitUpdateDtoCollected2);
        variableUpdates.clear();
        externalVariables.clear();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto validSurveyUnitUpdateDtoEdited = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE8")
                .state(DataState.EDITED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(validSurveyUnitUpdateDtoEdited);
        variableUpdates.clear();
        externalVariables.clear();

        // Case 9 : both COLLECTED and EDITED valid
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto validSurveyUnitUpdateDtoCollected2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE9")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(validSurveyUnitUpdateDtoCollected2);
        variableUpdates.clear();
        externalVariables.clear();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto validSurveyUnitUpdateDtoEdited2 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE9")
                .state(DataState.EDITED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(validSurveyUnitUpdateDtoEdited2);
        variableUpdates.clear();
        externalVariables.clear();

        // Case 10 : INPUTED (non-concerned) invalid
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto validSurveyUnitUpdateDtoCollected3 = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE10")
                .state(DataState.COLLECTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(validSurveyUnitUpdateDtoCollected3);
        variableUpdates.clear();
        externalVariables.clear();

        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testString")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testInteger")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testNumber")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testBoolean")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        variableUpdates.add(CollectedVariableDto.collectedVariableBuilder()
                .idVar("testDate")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        externalVariables = new ArrayList<>();
        externalVariables.add(VariableDto.builder()
                .idVar("testStringExt")
                .values(new ArrayList<>(List.of(new String[]{"test"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testIntegerExt")
                .values(new ArrayList<>(List.of(new String[]{"?"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testNumberExt")
                .values(new ArrayList<>(List.of(new String[]{"1.1"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testBooleanExt")
                .values(new ArrayList<>(List.of(new String[]{"true"})))
                .build()
        );
        externalVariables.add(VariableDto.builder()
                .idVar("testDateExt")
                .values(new ArrayList<>(List.of(new String[]{"2001-01-01"})))
                .build()
        );

        SurveyUnitUpdateDto invalidSurveyUnitUpdateDtoInputed = SurveyUnitUpdateDto.builder()
                .idQuest("IdQuest1")
                .idCampaign("IdCampaign1")
                .idUE("TestUE10")
                .state(DataState.INPUTED)
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .collectedVariables(new ArrayList<>(variableUpdates))
                .externalVariables(new ArrayList<>(externalVariables))
                .build();

        testSurveyUnitUpdateDtos.add(invalidSurveyUnitUpdateDtoInputed);
        variableUpdates.clear();
        externalVariables.clear();

        initialSize = testSurveyUnitUpdateDtos.size();

        //When
        DataVerifier.verifySurveyUnits(testSurveyUnitUpdateDtos,variablesMap);
    }

    //Then
    @Test
    @DisplayName("Must have <EXPECTED_VOLUMETRY> survey units")
    void surveyUnitsDtoVolumetryTest(){
        assertThat(testSurveyUnitUpdateDtos).hasSize(EXPECTED_VOLUMETRY); //10 collected + 6 forced + 4 Edited + 1 Inputed
    }

    @Test
    @DisplayName("Case 1 : Correct variables types, don't create FORCED Survey Unit if correct")
    void correctUpdateVariableTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                surveyUnit.getIdUE().equals("TestUE1")
                && surveyUnit.getState() == DataState.FORCED)
            .isEmpty();
    }

    @Test
    @DisplayName("Case 2 : Incorrect update variable type (1 value), incorrect variable kept with empty value (except dates)")
    void incorrectUpdateVariable1Test(){
        assertThat(testSurveyUnitUpdateDtos.get(initialSize)).isNotNull();
        //Integer assert
        assertThat(testSurveyUnitUpdateDtos.get(initialSize).getCollectedVariables().get(0).getValues().get(0)).isEmpty();
        //Date assert
        assertThat(testSurveyUnitUpdateDtos.get(initialSize).getCollectedVariables()).hasSize(1);
    }

    @Test
    @DisplayName("Case 3 : Incorrect update variable type (2 values), incorrect value set to empty")
    void incorrectUpdateVariable2Test(){
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 1)).isNotNull();
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 1).getCollectedVariables().get(0).getValues()).hasSize(2).contains("");
    }

    @Test
    @DisplayName("Case 4 : Incorrect external variable type (1 value), incorrect variable kept with empty value (except dates)")
    void incorrectExternalVariable1Test(){
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 2)).isNotNull();
        //Integer assert
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 2).getExternalVariables().get(0).getValues().get(0)).isEmpty();
        //Date assert
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 2).getExternalVariables()).hasSize(1);
    }

    @Test
    @DisplayName("Case 5 : Incorrect external variable type (2 values), incorrect value set to null")
    void incorrectExternalVariable2Test(){
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 3)).isNotNull();
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 3).getExternalVariables()).hasSize(1);
        assertThat(testSurveyUnitUpdateDtos.get(initialSize + 3).getExternalVariables().get(0).getValues()).hasSize(2).contains("");
    }

    @Test
    @DisplayName("Case 6 : Both COLLECTED and EDITED invalid, only 1 FORCED")
    void bothStatesInvalidTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                        surveyUnit.getIdUE().equals("TestUE6")
                                && surveyUnit.getState() == DataState.FORCED)
                .hasSize(1);
    }

    @Test
    @DisplayName("Case 7 : COLLECTED valid and EDITED invalid, 1 FORCED with COLLECTED value")
    void editedInvalidTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                        surveyUnit.getIdUE().equals("TestUE7")
                                && surveyUnit.getState() == DataState.FORCED)
                .hasSize(1);
//TODO à valider
        assertThat(testSurveyUnitUpdateDtos.stream().filter(surveyUnit ->
                surveyUnit.getIdUE().equals("TestUE7")
                        && surveyUnit.getState() == DataState.FORCED).findFirst().get().getCollectedVariables().get(0).getValues().get(0)).isEqualTo("1");
    }

    @Test
    @DisplayName("Case 8 : COLLECTED invalid and EDITED valid, no FORCED")
    void collectedInvalidTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                        surveyUnit.getIdUE().equals("TestUE8")
                                && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    @Test
    @DisplayName("Case 9 : both COLLECTED and EDITED valid, no FORCED")
    void bothvalidTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                        surveyUnit.getIdUE().equals("TestUE9")
                                && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

    @Test
    @DisplayName("Case 10 : INPUTED (non-concerned) invalid, no forced")
    void inputedTest(){
        assertThat(testSurveyUnitUpdateDtos).filteredOn(surveyUnit ->
                        surveyUnit.getIdUE().equals("TestUE10")
                                && surveyUnit.getState() == DataState.FORCED)
                .isEmpty();
    }

}
