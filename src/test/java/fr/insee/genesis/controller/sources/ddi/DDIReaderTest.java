package fr.insee.genesis.controller.sources.ddi;

import fr.insee.genesis.Constants;
import fr.insee.genesis.exceptions.GenesisException;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.net.MalformedURLException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Set;

class DDIReaderTest {

	static VariablesMap simpsonsVariables;

	@BeforeAll
	static void setUp() throws MalformedURLException, GenesisException {
		String DDI_FILE= "src/test/resources/ddi/simpson2305.xml";
		Path ddiFilePath = Paths.get(DDI_FILE);
		simpsonsVariables = DDIReader.getVariablesFromDDI(ddiFilePath.toFile().toURI().toURL());
	}
	@Test
	void variablesShouldNotBeNull(){
		Assertions.assertThat(simpsonsVariables).isNotNull();
	}

	@Test
	void checkGroups(){
		Assertions.assertThat(simpsonsVariables.hasGroup("FAVOURITE_CHAR")).isTrue();
		Assertions.assertThat(simpsonsVariables.hasGroup(Constants.ROOT_GROUP_NAME)).isTrue();
		Assertions.assertThat(simpsonsVariables.hasGroup("m1")).isFalse();
	}

	@Test
	void expectedVariablesShouldBePresent(){
		Set<String> expectedVariables = Set.of(
				"FAVOURITE_CHAR1", "FAVOURITE_CHAR2","FAVOURITE_CHAR3","FAVOURITE_CHAR33CL",
				"SUM_EXPENSES", "LAST_BROADCAST", "COMMENT", "READY", "PRODUCER", "SEASON_NUMBER", "DATEFIRST",
				"CITY", "MAYOR", "STATE", "PET1", "PET4", "PETOCL", "ICE_FLAVOUR1", "ICE_FLAVOUR4", "ICE_FLAVOUROTCL",
				"NUCLEAR_CHARACTER1", "NUCLEAR_CHARACTER4", "BIRTH_CHARACTER1", "BIRTH_CHARACTER5",
				"PERCENTAGE_EXPENSES11", "PERCENTAGE_EXPENSES71",
				"LAST_FOOD_SHOPPING11", "LAST_FOOD_SHOPPING813CL","CLOWNING11", "CLOWNING43", "TRAVEL11", "TRAVEL46",
				"FEELCHAREV1", "FEELCHAREV4","LEAVDURATION11", "LEAVDURATION52", "NB_CHAR",
				"SURVEY_COMMENT");

		for (String variableName : expectedVariables) {
			Assertions.assertThat(simpsonsVariables.hasVariable(variableName)).isTrue();
		}
	}

	@Test
	void variablesShouldBeInExpectedGroup() {
		Assertions.assertThat(simpsonsVariables.getVariable("SUM_EXPENSES").getGroup().getName()).isEqualTo("FAVOURITE_CHAR");
		Assertions.assertThat(simpsonsVariables.getVariable("SURVEY_COMMENT").getGroup().getName()).isEqualTo(Constants.ROOT_GROUP_NAME);
		Assertions.assertThat(simpsonsVariables.getVariable("FAVOURITE_CHAR1").getGroup().getName()).isEqualTo("FAVOURITE_CHAR");
		Assertions.assertThat(simpsonsVariables.getVariable("FAVOURITE_CHAR33CL").getGroup().getName()).isEqualTo("FAVOURITE_CHAR");
	}

}
