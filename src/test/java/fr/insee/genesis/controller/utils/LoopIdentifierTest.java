package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.metadata.Group;
import fr.insee.genesis.controller.sources.metadata.Variable;
import fr.insee.genesis.controller.sources.metadata.VariableType;
import fr.insee.genesis.controller.sources.metadata.VariablesMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoopIdentifierTest {

	VariablesMap variablesMap;

	private static final String LOOP_NAME = "BOUCLE1";

	@BeforeEach
	void setUp() {
		// Given
		this.variablesMap = new VariablesMap();
		Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
		Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
		Variable var2 = new Variable("var2", variablesMap.getRootGroup(), VariableType.STRING, "1");
		variablesMap.putVariable(var1);
		variablesMap.putVariable(var2);
	}

	@Test
	@DisplayName("Should return <LOOP_NAME>_2")
	void test01() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var1", variablesMap, 2)).isEqualTo(String.format("%s_2",LOOP_NAME));
	}

	@Test
	@DisplayName("Should return the root group name")
	void test02() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var2", variablesMap, 1)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group name if the variable is not present in the variables map")
	void test03(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var3", variablesMap, 2)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group as related")
	void test04(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var1", variablesMap)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return null as related")
	void test05(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var2", variablesMap)).isNull();
	}

	@Test
	@DisplayName("Should return var1 group if missing suffix")
	void test06(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var1_MISSING", variablesMap,1)).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return var1 as related if missing suffix")
	void test07(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var1_MISSING", variablesMap)).isEqualTo("var1");
	}

	@Test
	@DisplayName("Should return var1 group if filter result prefix")
	void test08(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("FILTER_RESULT_var1", variablesMap,1)).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return var1 as related if filter result prefix")
	void test09(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("FILTER_RESULT_var1", variablesMap)).isEqualTo("var1");
	}

	@Test
	@DisplayName("Should return root as related if eno variable")
	void test10(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName(Constants.getEnoVariables()[0], variablesMap)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

}
