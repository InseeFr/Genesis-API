package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VariableModelTest {
    @Test
    void toJSONTest() throws JsonProcessingException {
        VariableModel variable = VariableModel.builder()
                .idVar("TESTIDVAR")
                .idParent("TESTIDPARENT")
                .idLoop("TESTIDLOOP")
                .values(new ArrayList<>(List.of(new String[]{"V1", "V2"})))
                .build();

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Assertions.assertEquals(objectMapper.readTree(objectMapper.writeValueAsString(variable)),
                objectMapper.readTree("{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\",\"idLoop\":\"TESTIDLOOP\",\"idParent\":\"TESTIDPARENT\"}"));
    }
}