package fr.insee.genesis.controller.sources.metadata.lunatic;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.sources.metadata.VariablesMap;
import fr.insee.genesis.exceptions.GenesisException;
import org.junit.jupiter.api.Test;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LunaticReaderTest {

    static final Path lunaticSamplesPath = Path.of(TestConstants.UNIT_TESTS_DDI_DIRECTORY, "LUNATIC-TEST");

    @Test
        //Same test  with DDI [2 groups, 463 variables]
    void readVariablesFromLogX21WebLunaticFile() throws GenesisException {
        //
        VariablesMap variables = LunaticReader.getVariablesFromLunaticJson(
                lunaticSamplesPath.resolve("log2021x21_web.json"));

        //
        assertNotNull(variables);
        assertEquals(2,variables.getGroupsCount());
        assertEquals(683, variables.getVariables().size());

    }

}