package fr.insee.genesis.domain.dtos;

import org.assertj.core.api.Assertions;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyUnitUpdateDtoTest {
    @Test
    public void toJSONTest(){
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);

        SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .idQuest("TESTIDQUEST")
                .idUE("TESTIDUE")
                .mode(Mode.WEB)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2000,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2000,1,1,0,0,0))
                .collectedVariables(collectedVariableDtoList)
                .externalVariables(externalVariableDtoList)
                .build();

        Assertions.assertThat(surveyUnitUpdateDto).isNotNull();

        Assertions.assertThat(surveyUnitUpdateDto.toJSONObject().toJSONString()).isEqualTo(
                "{\"mode\":\"WEB\",\"idCampaign\":\"TESTIDCAMPAIGN\",\"externalVariables\":[{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\"}],\"collectedVariables\":[{\"values\":[\"V1\",\"V2\"],\"idVar\":\"TESTIDVAR\",\"idLoop\":\"TESTIDLOOP\",\"idParent\":\"TESTIDPARENT\"}],\"idQuest\":\"TESTIDQUEST\",\"recordDate\":\"2000-01-01T00:00\",\"idUE\":\"TESTIDUE\",\"state\":\"COLLECTED\",\"fileDate\":\"2000-01-01T00:00\"}"
        );
    }

}
