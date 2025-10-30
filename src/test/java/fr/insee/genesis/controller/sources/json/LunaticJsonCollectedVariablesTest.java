package fr.insee.genesis.controller.sources.json;

import org.junit.jupiter.api.Test;

class LunaticJsonCollectedVariablesTest {
	@Test
	void setVariables() {
		LunaticJsonCollectedVariables l = new LunaticJsonCollectedVariables();
		String variable = "abc";
		LunaticJsonVariableData value = new LunaticJsonVariableData();
		l.setVariables(variable, value);
	}
}
