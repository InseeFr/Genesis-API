package fr.insee.genesis.controller.sources.json;

import org.junit.jupiter.api.Test;

class LunaticJsonExternalVariablesTest {
	@Test
	void setVariables() {
		LunaticJsonExternalVariables l = new LunaticJsonExternalVariables();
		String variable = "abc";
		String value = "abc";
		l.setVariables(variable, value);
	}
}
