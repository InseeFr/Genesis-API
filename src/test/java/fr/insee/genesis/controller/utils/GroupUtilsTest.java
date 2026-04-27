package fr.insee.genesis.controller.utils;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.utils.GroupUtils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GroupUtilsTest {

	MetadataModel metadataModel;

	private static final String LOOP_NAME = "BOUCLE_TEST";

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
	@DisplayName("Should return <LOOP_NAME>")
	void test01() {
		//When + Then
		Assertions.assertThat(GroupUtils.getGroupName("var1", metadataModel.getVariables())).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return the root group name")
	void test02() {
		//When + Then
		Assertions.assertThat(GroupUtils.getGroupName("var2", metadataModel.getVariables())).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return the root group name if the variable is not present in the variables map")
	void test03(){
		//When + Then
		Assertions.assertThat(GroupUtils.getGroupName("var3", metadataModel.getVariables())).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

	@Test
	@DisplayName("Should return var1 group if missing suffix")
	void test04(){
		//When + Then
		Assertions.assertThat(GroupUtils.getGroupName("var1_MISSING", metadataModel.getVariables())).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Should return var1 group if filter result prefix")
	void test05(){
		//When + Then
		Assertions.assertThat(GroupUtils.getGroupName("FILTER_RESULT_var1", metadataModel.getVariables())).isEqualTo(LOOP_NAME);
	}

	@Test
	@DisplayName("Parent group name of var1 should be root group name")
	void test06(){
		Assertions.assertThat(GroupUtils.getParentGroupName("var1",metadataModel.getVariables())).isEqualTo(Constants.ROOT_GROUP_NAME);
	}

}
