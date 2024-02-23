package fr.insee.genesis.infrastructure.utils;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitUpdateDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import org.assertj.core.api.Assertions;

import org.json.simple.JSONArray;
import org.junit.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class JSONUtilsTest {
    @Test
    public void getJSONArrayFromResponsesTest(){
        //Given
        List<SurveyUnitUpdateDto> responses = new ArrayList<>();

        for(int i = 0; i < 2; i++) {
            List<VariableDto> externalVariableDtoList = new ArrayList<>();
            VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
            externalVariableDtoList.add(variableDto);

            List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
            CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
            collectedVariableDtoList.add(collectedVariableDto);

            SurveyUnitUpdateDto surveyUnitUpdateDto = SurveyUnitUpdateDto.builder()
                    .idCampaign("TESTIDCAMPAIGN")
                    .idQuest("TESTIDQUEST")
                    .idUE("TESTIDUE" + i)
                    .mode(Mode.WEB)
                    .state(DataState.COLLECTED)
                    .fileDate(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                    .recordDate(LocalDateTime.of(2000, 1, 1, 0, 0, 0))
                    .collectedVariables(collectedVariableDtoList)
                    .externalVariables(externalVariableDtoList)
                    .build();

            responses.add(surveyUnitUpdateDto);
        }

        //When
        JSONArray jsonArray = JSONUtils.getJSONArrayFromResponses(responses);

        //Then
        Assertions.assertThat(jsonArray).hasSize(2);
    }
}
