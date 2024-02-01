package fr.insee.genesis.domain.dtos;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class VariableDtoTest {
    @Test
    void toJSONTest(){
        VariableDto variableDto = new VariableDto("TESTIDVAR", new ArrayList<>(List.of(new String[]{"V1", "V2"})));

        Assertions.assertThat(variableDto.toJSONObject().get("idVar")).isEqualTo(
                "{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\",\"idLoop\":\"TESTIDLOOP\",\"idParent\":\"TESTIDPARENT\"}"
        );

    }
}
