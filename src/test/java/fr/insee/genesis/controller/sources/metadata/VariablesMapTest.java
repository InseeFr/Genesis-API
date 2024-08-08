package fr.insee.genesis.controller.sources.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.Set;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.metadata.Group;
import fr.insee.genesis.controller.sources.metadata.McqVariable;
import fr.insee.genesis.controller.sources.metadata.UcqVariable;
import fr.insee.genesis.controller.sources.metadata.Variable;
import fr.insee.genesis.controller.sources.metadata.VariableType;
import fr.insee.genesis.controller.sources.metadata.VariablesMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import lombok.extern.log4j.Log4j2;

@Log4j2
public class VariablesMapTest {

    private final static String LOOP1_NAME = "INDIVIDUALS_LOOP";
    private final static String LOOP2_NAME = "CARS_LOOP";
    private final static String ADDRESS_VARIABLE_NAME = "ADDRESS";
    private final static String HOUSEHOLD_INCOME_VARIABLE_NAME = "HOUSEHOLD_INCOME";
    private final static String FIRST_NAME_VARIABLE_NAME = "FIRST_NAME";
    private final static String LAST_NAME_VARIABLE_NAME = "LAST_NAME";
    private final static String GENDER_VARIABLE_NAME = "GENDER";
    private final static String CAR_COLOR_VARIABLE_NAME = "CAR_COLOR";
    private final static String VEHICLE_OWNER_VARIABLE_NAME = "VEHICLE_OWNER";
    private final static String CAR_OWNER_VARIABLE_NAME = "CAR_OWNER";
    private final static String MOTO_OWNER_VARIABLE_NAME = "MOTO_OWNER";
    private final static String SEX_VARIABLE_NAME = "SEXE";

    private final static String DUMMY_VARIABLE_NAME = "DUMMY";
    private final static String UNKNOWN_QUESTION_VARIABLE_NAME = "UNKNOWN_QUESTION";

    private final static String RELATIONSHIP_QUESTION_ITEM = "RELATIONSHIP";
    

    private VariablesMap variablesMap;

    @BeforeEach
    public void createTestVariablesMap() {
        variablesMap = new VariablesMap();


        Group rootGroup = variablesMap.getRootGroup();
        Group individualsGroup = new Group(LOOP1_NAME, Constants.ROOT_GROUP_NAME);
        Group carsGroup = new Group(LOOP2_NAME, LOOP1_NAME);

        variablesMap.putGroup(individualsGroup);
        variablesMap.putGroup(carsGroup);

        variablesMap.putVariable(
                new Variable(ADDRESS_VARIABLE_NAME, rootGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(HOUSEHOLD_INCOME_VARIABLE_NAME, rootGroup, VariableType.NUMBER));
        variablesMap.putVariable(
                new Variable(FIRST_NAME_VARIABLE_NAME, individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(LAST_NAME_VARIABLE_NAME, individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(GENDER_VARIABLE_NAME, individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(CAR_COLOR_VARIABLE_NAME, carsGroup, VariableType.STRING));
    }

    @Test
    void testRootGroup() {
        assertTrue(variablesMap.hasGroup(Constants.ROOT_GROUP_NAME));
        assertEquals(Constants.ROOT_GROUP_NAME, variablesMap.getRootGroup().getName());
        assertNull(variablesMap.getRootGroup().getParentName());
    }

    @Test
    void testGetVariableByName() {

        // Get
        Variable rootVariable = variablesMap.getVariable(HOUSEHOLD_INCOME_VARIABLE_NAME);
        Variable group1Variable = variablesMap.getVariable(FIRST_NAME_VARIABLE_NAME);
        Variable group2Variable = variablesMap.getVariable(CAR_COLOR_VARIABLE_NAME);

        // Get a variable that does not exist
        log.debug("Trying to get a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        Variable dummyVariable = variablesMap.getVariable(DUMMY_VARIABLE_NAME);

        //
        assertEquals(HOUSEHOLD_INCOME_VARIABLE_NAME, rootVariable.getName());
        assertEquals(FIRST_NAME_VARIABLE_NAME, group1Variable.getName());
        assertEquals(CAR_COLOR_VARIABLE_NAME, group2Variable.getName());
        assertNull(dummyVariable);
    }

    @Test
    void testRemoveAndHasVariable() {

        // Remove
        variablesMap.removeVariable(HOUSEHOLD_INCOME_VARIABLE_NAME);
        variablesMap.removeVariable(CAR_COLOR_VARIABLE_NAME);

        // Remove a variable that does not exist
        log.debug("Trying to remove a variable that does not exist in a test function, " +
                "a second message should pop in the log.");
        variablesMap.removeVariable("FOO");

        //
        assertFalse(variablesMap.hasVariable(HOUSEHOLD_INCOME_VARIABLE_NAME));
        assertTrue(variablesMap.hasVariable(FIRST_NAME_VARIABLE_NAME));
        assertFalse(variablesMap.hasVariable(CAR_COLOR_VARIABLE_NAME));
    }

    @Test
    void getIdentifierNamesTest() {
        assertEquals(
                List.of(Constants.ROOT_IDENTIFIER_NAME, LOOP1_NAME, LOOP2_NAME),
                variablesMap.getIdentifierNames()
        );
    }

    @Test
    void getFullyQualifiedNameTest() {
        assertEquals(HOUSEHOLD_INCOME_VARIABLE_NAME,
                variablesMap.getFullyQualifiedName(HOUSEHOLD_INCOME_VARIABLE_NAME));
        assertEquals("INDIVIDUALS_LOOP.FIRST_NAME",
                variablesMap.getFullyQualifiedName(FIRST_NAME_VARIABLE_NAME));
        assertEquals("INDIVIDUALS_LOOP.CARS_LOOP.CAR_COLOR",
                variablesMap.getFullyQualifiedName(CAR_COLOR_VARIABLE_NAME));
    }

    @Test
    void testGetGroupVariableNames() {
        assertTrue(variablesMap.getGroupVariableNames(Constants.ROOT_GROUP_NAME)
                .containsAll(Set.of(ADDRESS_VARIABLE_NAME, HOUSEHOLD_INCOME_VARIABLE_NAME)));
        assertTrue(variablesMap.getGroupVariableNames(LOOP1_NAME)
                .containsAll(Set.of(FIRST_NAME_VARIABLE_NAME, LAST_NAME_VARIABLE_NAME, GENDER_VARIABLE_NAME)));
        assertTrue(variablesMap.getGroupVariableNames(LOOP2_NAME)
                .contains(CAR_COLOR_VARIABLE_NAME));
    }

    @Test
    void testMcqMethods() {
        //
        Group group = variablesMap.getGroup(LOOP1_NAME);
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_A").group(group).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Spouse").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_B").group(group).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Child").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_C").group(group).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Parent").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_D").group(group).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Other").build());
        //
        assertTrue(variablesMap.hasMcq(RELATIONSHIP_QUESTION_ITEM));
        assertSame(RELATIONSHIP_QUESTION_ITEM, variablesMap.getVariable(RELATIONSHIP_QUESTION_ITEM + "_A").getQuestionItemName());
        assertFalse(variablesMap.hasMcq(ADDRESS_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(FIRST_NAME_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(CAR_COLOR_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(UNKNOWN_QUESTION_VARIABLE_NAME));
        //
        assertSame(group, variablesMap.getMcqGroup(RELATIONSHIP_QUESTION_ITEM));
        assertNull(variablesMap.getMcqGroup(ADDRESS_VARIABLE_NAME));
        assertNull(variablesMap.getMcqGroup(FIRST_NAME_VARIABLE_NAME));
        assertNull(variablesMap.getMcqGroup(CAR_COLOR_VARIABLE_NAME));
        assertNull(variablesMap.getMcqGroup(UNKNOWN_QUESTION_VARIABLE_NAME));
    }

    @Test
    void testGetVariablesNames() {
        variablesMap = createCompleteFakeVariablesMap();
        // KSE et KGA à trouver, une par liste
        List<String> ucqMcqVariablesNames = variablesMap.getUcqVariablesNames();
        List<String> mcqVariablesNames = variablesMap.getMcqVariablesNames();
        Set<String> variablesNames = variablesMap.getVariableNames();
        // Check ucq
        assertTrue(ucqMcqVariablesNames.contains(VEHICLE_OWNER_VARIABLE_NAME));
        assertFalse(ucqMcqVariablesNames.contains(CAR_OWNER_VARIABLE_NAME));
        // Check mcq
        assertFalse(mcqVariablesNames.contains(VEHICLE_OWNER_VARIABLE_NAME));
        assertTrue(mcqVariablesNames.contains(RELATIONSHIP_QUESTION_ITEM));
        assertFalse(mcqVariablesNames.contains(RELATIONSHIP_QUESTION_ITEM + "_A"));
        // Check mcq
        assertFalse(variablesNames.contains(VEHICLE_OWNER_VARIABLE_NAME));
        assertTrue(variablesNames.contains(CAR_OWNER_VARIABLE_NAME));
        assertTrue(variablesNames.contains(MOTO_OWNER_VARIABLE_NAME));

        assertTrue(variablesMap.hasMcq(RELATIONSHIP_QUESTION_ITEM));
        assertTrue(variablesMap.hasUcq(CAR_OWNER_VARIABLE_NAME));
        assertTrue(variablesMap.hasUcqMcq(CAR_OWNER_VARIABLE_NAME));
        assertFalse(variablesMap.hasUcqMcq(VEHICLE_OWNER_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(ADDRESS_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(FIRST_NAME_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(CAR_COLOR_VARIABLE_NAME));
        assertFalse(variablesMap.hasMcq(UNKNOWN_QUESTION_VARIABLE_NAME));
    }

    /* Variables map objects to test multimode management */

    /**
     * Return a VariablesMap object containing variables named as follows:
     * - FIRST_NAME, LAST_NAME, AGE at the root
     * - CAR_COLOR in a group named CARS_LOOP
     */
    public static VariablesMap createCompleteFakeVariablesMap(){

        VariablesMap variablesMap = new VariablesMap();

        // Groups
        Group rootGroup = variablesMap.getRootGroup();
        Group carsGroup = new Group(LOOP2_NAME, Constants.ROOT_GROUP_NAME);
        variablesMap.putGroup(carsGroup);

        // Variables
        variablesMap.putVariable(new Variable(LAST_NAME_VARIABLE_NAME, rootGroup, VariableType.STRING, "20"));
        variablesMap.putVariable(new Variable(FIRST_NAME_VARIABLE_NAME, rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable("AGE", rootGroup, VariableType.INTEGER, "50"));
        variablesMap.putVariable(new Variable(CAR_COLOR_VARIABLE_NAME, carsGroup, VariableType.STRING, "50"));

        // unique choice question variable
        UcqVariable ucq = new UcqVariable(SEX_VARIABLE_NAME, rootGroup, VariableType.STRING, "50");
        ucq.addModality("1", "Male");
        ucq.addModality("2", "Female");
        variablesMap.putVariable(ucq);

        // unique choice question variable related to multiple choices question
        UcqVariable ucqMcq1 = new UcqVariable(CAR_OWNER_VARIABLE_NAME, rootGroup, VariableType.STRING, "50");
        ucqMcq1.setQuestionItemName(VEHICLE_OWNER_VARIABLE_NAME);
        ucqMcq1.addModality("1", "Yes");
        ucqMcq1.addModality("2", "No");
        UcqVariable ucqMcq2 = new UcqVariable(MOTO_OWNER_VARIABLE_NAME, rootGroup, VariableType.STRING, "50");
        ucqMcq2.setQuestionItemName(VEHICLE_OWNER_VARIABLE_NAME);
        ucqMcq2.addModality("1", "Yes");
        ucqMcq2.addModality("2", "No");
        variablesMap.putVariable(ucqMcq1);
        variablesMap.putVariable(ucqMcq2);

        // multiple choices question variable
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_A").group(rootGroup).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Spouse").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_B").group(rootGroup).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Child").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_C").group(rootGroup).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Parent").build());
        variablesMap.putVariable(McqVariable.builder()
                .name(RELATIONSHIP_QUESTION_ITEM + "_D").group(rootGroup).questionItemName(RELATIONSHIP_QUESTION_ITEM).text("Other").build());

        return variablesMap;
    }

    public static VariablesMap createAnotherFakeVariablesMap(){

        VariablesMap variablesMap = new VariablesMap();

        // Groups
        Group rootGroup = variablesMap.getRootGroup();
        Group carsGroup = new Group(LOOP2_NAME, Constants.ROOT_GROUP_NAME);
        variablesMap.putGroup(carsGroup);

        // Variables
        variablesMap.putVariable(new Variable(LAST_NAME_VARIABLE_NAME, rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable(FIRST_NAME_VARIABLE_NAME, rootGroup, VariableType.STRING, "20"));
        variablesMap.putVariable(new Variable(ADDRESS_VARIABLE_NAME, rootGroup, VariableType.STRING, "50"));
        variablesMap.putVariable(new Variable(CAR_COLOR_VARIABLE_NAME, carsGroup, VariableType.STRING, "500"));

        return variablesMap;
    }

    /* Variables map objects to test information levels management */

    public static VariablesMap createVariablesMap_rootOnly() {
        VariablesMap variablesMap = new VariablesMap();

        Group rootGroup = variablesMap.getRootGroup();

        variablesMap.putGroup(rootGroup);

        variablesMap.putVariable(
                new Variable(ADDRESS_VARIABLE_NAME, rootGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(HOUSEHOLD_INCOME_VARIABLE_NAME, rootGroup, VariableType.NUMBER));

        return variablesMap;
    }

    public static VariablesMap createVariablesMap_oneLevel() {
        VariablesMap variablesMap = createVariablesMap_rootOnly();

        Group individualsGroup = new Group(LOOP1_NAME, Constants.ROOT_GROUP_NAME);

        variablesMap.putGroup(individualsGroup);

        variablesMap.putVariable(
                new Variable(FIRST_NAME_VARIABLE_NAME, individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(LAST_NAME_VARIABLE_NAME, individualsGroup, VariableType.STRING));
        variablesMap.putVariable(
                new Variable(GENDER_VARIABLE_NAME, individualsGroup, VariableType.STRING));

        return variablesMap;
    }

    public static VariablesMap createVariablesMap_twoLevels() {
        VariablesMap variablesMap = createVariablesMap_oneLevel();

        Group carsGroup = new Group(LOOP2_NAME, LOOP1_NAME);

        variablesMap.putGroup(carsGroup);

        variablesMap.putVariable(
                new Variable(CAR_COLOR_VARIABLE_NAME, carsGroup, VariableType.STRING));

        return variablesMap;
    }

}
