package fr.insee.genesis.controller.sources.metadata.lunatic;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.bpm.metadata.reader.lunatic.LunaticReader;
import fr.insee.genesis.TestConstants;
import org.junit.jupiter.api.Test;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class LunaticReaderTest {

    static final Path lunaticSamplesPath = Path.of(TestConstants.UNIT_TESTS_DDI_DIRECTORY, "LUNATIC-TEST");

    @Test
        //Same test  with DDI [2 groups, 463 variables]
    void readVariablesFromLogX21WebLunaticFile() throws FileNotFoundException {
        //
        VariablesMap variables = LunaticReader.getMetadataFromLunatic(
                new FileInputStream(lunaticSamplesPath.resolve("lunaticlog2021x21_web.json").toString())).getVariables();

        //
        assertNotNull(variables);
        Set<String> groupNames = new HashSet<>();
        for(String variableName : variables.getVariables().keySet()){
            groupNames.add(variables.getVariable(variableName).getGroupName());
        }
        assertEquals(2,groupNames.size());
        assertEquals(683, variables.getVariables().size());

    }

}