package fr.insee.genesis.domain.dtos;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

class CollectedVariableDtoTest {
    @Test
    void toJSONTest(){
        CollectedVariableDto collectedVariableDto = CollectedVariableDto.collectedVariableBuilder()
                .idVar("TESTIDVAR")
                .idParent("TESTIDPARENT")
                .idLoop("TESTIDLOOP")
                .values(new ArrayList<>(List.of(new String[]{"V1", "V2"})))
                .build();

        Assertions.assertThat(collectedVariableDto.toJSONObject().toJSONString()).isEqualTo(
                "{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\",\"idLoop\":\"TESTIDLOOP\",\"idParent\":\"TESTIDPARENT\"}"
        );
    }
}
