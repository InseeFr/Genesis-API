package fr.insee.genesis.domain.dtos;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VariableDtoTest {
    @Test
    void toJSONTest() throws JsonProcessingException {
        VariableDto variableDto = new VariableDto("TESTIDVAR", new ArrayList<>(List.of(new String[]{"V1", "V2"})));
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();
        Assertions.assertEquals(objectMapper.readTree(objectMapper.writeValueAsString(variableDto)),
                objectMapper.readTree("{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\"}"));

    }
}
