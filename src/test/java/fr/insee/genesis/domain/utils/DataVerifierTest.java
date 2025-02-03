package fr.insee.genesis.domain.utils;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

class DataVerifierTest {

    private List<SurveyUnitModel> surveyUnits;
    private VariablesMap variablesMap;

    @BeforeEach
    void setUp() {
        surveyUnits = new ArrayList<>();
        variablesMap = new VariablesMap();
        Group group =new Group("group");

        // Setup variables with Types
        Variable variableDefinition1 = new Variable("var1",group,VariableType.INTEGER);
        variablesMap.putVariable(variableDefinition1);

        Variable variableDefinition2 = new Variable("var2", group, VariableType.BOOLEAN);
        variablesMap.putVariable(variableDefinition2);

        // Setup survey units
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .values(List.of("123"))
                .loopId("loop1")
                .parentId("parent1")
                .build();

        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var2")
                .values(List.of("true"))
                .loopId("loop2")
                .parentId("parent2")
                .build();

        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .interrogationId("UE1100000001")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(collectedVariable1, collectedVariable2))
                .externalVariables(List.of())
                .build();

        surveyUnits.add(surveyUnit);
    }

    @Test
    void shouldHandleEmptyValuesList() {
        List<SurveyUnitModel> surveyUnitModelsList = new ArrayList<>();

        // When
        DataVerifier.verifySurveyUnits(surveyUnitModelsList, null);

        // Then
        assertTrue(surveyUnitModelsList.isEmpty()); // Empty list, nothing invalid, so should return null
    }

    @Test
    void shouldAddForcedSurveyUnit_WhenInvalidCollectedVariable() {
        // GIVEN
        // Add invalid value
        surveyUnits.clear();
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .values(List.of("invalid"))
                .loopId("loop1")
                .parentId("parent1")
                .build();
        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var2")
                .values(List.of("true"))
                .loopId("loop2")
                .parentId("parent2")
                .build();
        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .interrogationId("UE1100000001")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(collectedVariable1, collectedVariable2))
                .externalVariables(List.of())
                .build();
        surveyUnits.add(surveyUnit);


        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // THEN, check FORCED value was added
        Assertions.assertEquals(2, surveyUnits.size());
        Assertions.assertEquals(DataState.FORCED, surveyUnits.get(1).getState());
    }

    @Test
    void shouldNotAddForcedSurveyUnit_WhenAllVariablesAreValid() {
        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // Check no data added
        Assertions.assertEquals(1, surveyUnits.size());
    }

    @Test
    void shouldAddForcedSurveyUnit_WhenInvalidExternalVariable() {
        //Add surveyUnit with invalid external Variable
        VariableModel extVar = VariableModel.builder().varId("var2").values(List.of(
                "notBoolean")).build();
        List<VariableModel> listVarExt = new ArrayList<>();
        listVarExt.add(extVar);

        SurveyUnitModel surveyUnitWithInvalidExt = SurveyUnitModel.builder()
                .interrogationId("UE1100000002")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of())
                .externalVariables(listVarExt)
                .build();

        surveyUnits.add(surveyUnitWithInvalidExt);

        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // THEN
        Assertions.assertEquals(3, surveyUnits.size());
        Assertions.assertEquals(DataState.FORCED, surveyUnits.get(2).getState());
        Assertions.assertEquals(1, surveyUnits.get(1).getExternalVariables().size());
    }

    @Test
    void shouldCorrectInvalidValuesInForcedSurveyUnit() {
        // GIVEN
        // ADD invalid values
        surveyUnits.clear();
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .values(List.of("invalid", "456"))
                .loopId("loop1")
                .parentId("parent1")
                .build();
        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var2")
                .values(List.of("false"))
                .loopId("loop2")
                .parentId("parent2")
                .build();
        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .interrogationId("UE1100000001")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(collectedVariable1, collectedVariable2))
                .externalVariables(List.of())
                .build();
        surveyUnits.add(surveyUnit);

        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // FORCED values added
        SurveyUnitModel forcedUnit = surveyUnits.get(1);
        Assertions.assertEquals(DataState.FORCED, forcedUnit.getState());
        Assertions.assertEquals(1, forcedUnit.getCollectedVariables().size());
        Assertions.assertEquals("", forcedUnit.getCollectedVariables().getFirst().values().getFirst()); // Corrected values
    }

}
