package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Group;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariableType;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class LoopIdentifierTest {

	VariablesMap variablesMap;

	private static final String LOOP_NAME = "BOUCLE1";

	@BeforeEach
	public void setUp() {
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
	public void test01() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var1", variablesMap, 2)).isEqualTo(String.format("%s_2",LOOP_NAME));
	}

	@Test
	@DisplayName("Should return the root group name")
	public void test02() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var2", variablesMap, 1)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group name if the variable is not present in the variables map")
	public void test03(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var3", variablesMap, 2)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group as parent")
	public void test04(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getParentGroupName("var1", variablesMap)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return null as parent")
	public void test05(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getParentGroupName("var2", variablesMap)).isNull();
	}

}
