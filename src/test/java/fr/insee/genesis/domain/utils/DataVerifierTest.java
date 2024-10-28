package fr.insee.genesis.domain.utils;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.CollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
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
        CollectedVariable cv1 = new CollectedVariable("var1", List.of("123"), "loop1", "parent1");
        CollectedVariable cv2 = new CollectedVariable("var2", List.of("true"), "loop2", "parent2");

        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .idUE("UE1100000001")
                .idQuest("Quest1")
                .idCampaign("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(cv1, cv2))
                .externalVariables(List.of())
                .build();

        surveyUnits.add(surveyUnit);
    }

    @Test
    void shouldHandleEmptyValuesList() {
        List<SurveyUnitModel> suDtosList = new ArrayList<>();

        // When
        DataVerifier.verifySurveyUnits(suDtosList, null);

        // Then
        assertTrue(suDtosList.isEmpty()); // Empty list, nothing invalid, so should return null
    }

    @Test
    void shouldAddForcedSurveyUnit_WhenInvalidCollectedVariable() {
        // Add invalid value
        surveyUnits.getFirst().getCollectedVariables().getFirst().setValues(List.of("invalid"));

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
        fr.insee.genesis.domain.model.surveyunit.Variable extVar = fr.insee.genesis.domain.model.surveyunit.Variable.builder().idVar("var2").values(List.of("notBoolean")).build();
        List<fr.insee.genesis.domain.model.surveyunit.Variable> listVarExt = new ArrayList<>();
        listVarExt.add(extVar);

        SurveyUnitModel surveyUnitWithInvalidExt = SurveyUnitModel.builder()
                .idUE("UE1100000002")
                .idQuest("Quest1")
                .idCampaign("Camp1")
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
        // ADD invalid values
        surveyUnits.get(0).getCollectedVariables().get(0).setValues(List.of("invalid", "456"));
        surveyUnits.get(0).getCollectedVariables().get(1).setValues(List.of("false"));

        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // FORCED values added
        SurveyUnitModel forcedUnit = surveyUnits.get(1);
        Assertions.assertEquals(DataState.FORCED, forcedUnit.getState());
        Assertions.assertEquals(1, forcedUnit.getCollectedVariables().size());
        Assertions.assertEquals("", forcedUnit.getCollectedVariables().getFirst().getValues().getFirst()); // Corrected values
    }

}
