package fr.insee.genesis.domain.utils;

import fr.insee.bpm.metadata.Constants;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.opentest4j.AssertionFailedError;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Slf4j
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
                .value("123")
                .scope("loop1")
                .iteration(1)
                .parentId("parent1")
                .build();

        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var2")
                .value("true")
                .scope("loop2")
                .iteration(1)
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
    void shouldAddFormattedSurveyUnit_WhenInvalidCollectedVariable() {
        // GIVEN
        // Add invalid value
        surveyUnits.clear();
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .value("invalid")
                .scope("loop1")
                .iteration(1)
                .parentId("parent1")
                .build();
        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var2")
                .value("true")
                .scope("loop2")
                .iteration(1)
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

        // THEN, check FORMATTED value was added
        Assertions.assertEquals(2, surveyUnits.size());
        Assertions.assertEquals(DataState.FORMATTED, surveyUnits.get(1).getState());
    }

    @Test
    void shouldNotAddFormattedSurveyUnit_WhenAllVariablesAreValid() {
        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // Check no data added
        Assertions.assertEquals(1, surveyUnits.size());
    }

    @Test
    void shouldAddFormattedSurveyUnit_WhenInvalidExternalVariable() {
        //Add surveyUnit with invalid external Variable
        VariableModel extVar = VariableModel.builder()
                .varId("var2")
                .value("notBoolean")
                .build();
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
        Assertions.assertEquals(DataState.FORMATTED, surveyUnits.get(2).getState());
        Assertions.assertEquals(1, surveyUnits.get(1).getExternalVariables().size());
    }

    @Test
    void shouldCorrectInvalidValuesInFormattedSurveyUnit() {
        // GIVEN
        // ADD invalid values
        surveyUnits.clear();
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .value("invalid")
                .scope("loop1")
                .iteration(1)
                .parentId("parent1")
                .build();
        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var1")
                .value("456")
                .scope("loop1")
                .iteration(2)
                .parentId("parent1")
                .build();
        VariableModel collectedVariable3 = VariableModel.builder()
                .varId("var2")
                .value("false")
                .scope("loop2")
                .iteration(1)
                .parentId("parent2")
                .build();
        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .interrogationId("UE1100000001")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(collectedVariable1, collectedVariable2, collectedVariable3))
                .externalVariables(List.of())
                .build();
        surveyUnits.add(surveyUnit);

        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // THEN FORMATTED values added
        SurveyUnitModel formattedUnit = surveyUnits.get(1);
        Assertions.assertEquals(DataState.FORMATTED, formattedUnit.getState());
        Assertions.assertEquals(1, formattedUnit.getCollectedVariables().size());
        Assertions.assertEquals("", formattedUnit.getCollectedVariables().getFirst().value()); // Corrected values
    }

    @Test
    void shouldCorrectInvalidIterationOnFormattedSurveyUnit() {
        // GIVEN
        // ADD invalid values
        surveyUnits.clear();
        VariableModel collectedVariable1 = VariableModel.builder()
                .varId("var1")
                .value("123")
                .scope("loop1")
                .iteration(1)
                .parentId("parent1")
                .build();
        VariableModel collectedVariable2 = VariableModel.builder()
                .varId("var1")
                .value("invalid")
                .scope("loop1")
                .iteration(2)
                .parentId("parent1")
                .build();
        VariableModel collectedVariable3 = VariableModel.builder()
                .varId("var2")
                .value("false")
                .scope("loop2")
                .iteration(1)
                .parentId("parent2")
                .build();
        VariableModel collectedVariable4 = VariableModel.builder()
                .varId("var2")
                .value("Not a boolean")
                .scope("loop2")
                .iteration(2)
                .parentId("parent2")
                .build();
        SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                .interrogationId("UE1100000001")
                .questionnaireId("Quest1")
                .campaignId("Camp1")
                .state(DataState.COLLECTED)
                .collectedVariables(List.of(collectedVariable1, collectedVariable2, collectedVariable3, collectedVariable4))
                .externalVariables(List.of())
                .build();
        surveyUnits.add(surveyUnit);

        // WHEN
        DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

        // THEN FORMATTED values added
        Assertions.assertTrue(surveyUnits.size() > 1);
        SurveyUnitModel formattedUnit = surveyUnits.get(1);
        Assertions.assertEquals(DataState.FORMATTED, formattedUnit.getState());
        Assertions.assertEquals(2, formattedUnit.getCollectedVariables().size());
        Assertions.assertEquals("", formattedUnit.getCollectedVariables().getFirst().value()); // Corrected values
        Assertions.assertEquals("", formattedUnit.getCollectedVariables().get(1).value());
    }

    @ParameterizedTest
    @EnumSource(value = DataState.class, names = {"EDITED", "FORCED", "FORMATTED"}, mode = EnumSource.Mode.EXCLUDE)
    void shouldCreateFormattedIfNull(DataState dataState) {
        String variableName = "varnull";
        for(VariableType variableType : VariableType.values()){
            if(variableType.equals(VariableType.STRING)){
                continue; //Skip STRING
            }
            variablesMap.removeVariable(variableName);

            // GIVEN
            Variable variableDefinition = new Variable(
                    variableName,
                    new MetadataModel().getRootGroup(),
                    variableType
            );
            variablesMap.putVariable(variableDefinition);

            // Add null value
            surveyUnits.clear();
            VariableModel collectedVariable1 = VariableModel.builder()
                    .varId(variableName)
                    .value(null)
                    .scope(Constants.ROOT_GROUP_NAME)
                    .iteration(1)
                    .build();
            SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                    .interrogationId("UE1100000001")
                    .questionnaireId("Quest1")
                    .campaignId("Camp1")
                    .state(dataState)
                    .collectedVariables(List.of(collectedVariable1))
                    .externalVariables(List.of())
                    .build();
            surveyUnits.add(surveyUnit);

            // WHEN
            DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

            // THEN FORMATTED values added
            try{
                Assertions.assertTrue(surveyUnits.size() > 1);
                SurveyUnitModel formattedUnit = surveyUnits.get(1);
                Assertions.assertEquals(DataState.FORMATTED, formattedUnit.getState());
                Assertions.assertEquals(1, formattedUnit.getCollectedVariables().size());
                Assertions.assertEquals("", formattedUnit.getCollectedVariables().getFirst().value()); // Corrected value
            }catch (AssertionFailedError afe){
                log.error("Failed on type {}", variableType);
                throw afe;
            }
        }
    }

    @ParameterizedTest
    @EnumSource(value = DataState.class, names = {"EDITED", "FORCED", "FORMATTED"})
    void shouldNotCreateFormattedIfNull(DataState dataState) {
        String variableName = "varnull";
        for(VariableType variableType : VariableType.values()){
            if(variableType.equals(VariableType.STRING)){
                continue; //Skip STRING
            }
            variablesMap.removeVariable(variableName);

            // GIVEN
            Variable variableDefinition = new Variable(
                    variableName,
                    new MetadataModel().getRootGroup(),
                    variableType
            );
            variablesMap.putVariable(variableDefinition);

            // Add null value
            surveyUnits.clear();
            VariableModel collectedVariable1 = VariableModel.builder()
                    .varId(variableName)
                    .value(null)
                    .scope(Constants.ROOT_GROUP_NAME)
                    .iteration(1)
                    .build();
            SurveyUnitModel surveyUnit = SurveyUnitModel.builder()
                    .interrogationId("UE1100000001")
                    .questionnaireId("Quest1")
                    .campaignId("Camp1")
                    .state(dataState)
                    .collectedVariables(List.of(collectedVariable1))
                    .externalVariables(List.of())
                    .build();
            surveyUnits.add(surveyUnit);

            // WHEN
            DataVerifier.verifySurveyUnits(surveyUnits, variablesMap);

            // THEN no data added
            try{
                Assertions.assertEquals(1, surveyUnits.size());
            }catch (AssertionFailedError afe){
                log.error("Failed on type {}", variableType);
                throw afe;
            }
        }
    }
}
