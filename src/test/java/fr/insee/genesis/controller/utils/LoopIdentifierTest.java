package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.domain.utils.LoopIdentifier;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class LoopIdentifierTest {

	MetadataModel metadataModel;

	private static final String LOOP_NAME = "BOUCLE1";

	@BeforeEach
	void setUp() {
		// Given
		this.metadataModel = new MetadataModel();
		Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
		Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
		Variable var2 = new Variable("var2", metadataModel.getRootGroup(), VariableType.STRING, "1");
		metadataModel.getVariables().putVariable(var1);
		metadataModel.getVariables().putVariable(var2);
	}

	@Test
	@DisplayName("Should return <LOOP_NAME>_2")
	void test01() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var1", metadataModel.getVariables(), 2)).isEqualTo(String.format("%s_2",LOOP_NAME));
	}

	@Test
	@DisplayName("Should return the root group name")
	void test02() {
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var2", metadataModel.getVariables(), 1)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group name if the variable is not present in the variables map")
	void test03(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var3", metadataModel.getVariables(), 2)).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group as related")
	void test04(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var1", metadataModel.getVariables())).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return null as related")
	void test05(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var2", metadataModel.getVariables())).isNull();
	}

	@Test
	@DisplayName("Should return var1 group if missing suffix")
	void test06(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("var1_MISSING", metadataModel.getVariables(),1)).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return var1 as related if missing suffix")
	void test07(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("var1_MISSING", metadataModel.getVariables())).isEqualTo("var1");
	}

	@Test
	@DisplayName("Should return var1 group if filter result prefix")
	void test08(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getLoopIdentifier("FILTER_RESULT_var1", metadataModel.getVariables(),1)).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return var1 as related if filter result prefix")
	void test09(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName("FILTER_RESULT_var1", metadataModel.getVariables())).isEqualTo("var1");
	}

	@Test
	@DisplayName("Should return root as related if eno variable")
	void test10(){
		//When + Then
		Assertions.assertThat(LoopIdentifier.getRelatedVariableName(Constants.getEnoVariables()[0], metadataModel.getVariables())).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

}
