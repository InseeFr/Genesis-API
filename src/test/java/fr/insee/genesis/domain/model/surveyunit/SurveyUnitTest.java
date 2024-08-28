package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyUnitTest {
    @Test
    public void toJSONTest() throws JsonProcessingException {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnit surveyUnit = SurveyUnit.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .idQuest("TESTIDQUEST")
                .idUE("TESTIDUE")
                .mode(Mode.WEB)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2000,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2000,1,1,0,0,0))
                .collectedVariables(collectedVariableList)
                .externalVariables(externalVariableList)
                .build();

        Assertions.assertNotNull(surveyUnit);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Assertions.assertEquals(
                objectMapper.readTree("{\"idQuest\":\"TESTIDQUEST\",\"idCampaign\":\"TESTIDCAMPAIGN\",\"idUE\":\"TESTIDUE\",\"state\":\"COLLECTED\",\"mode\":\"WEB\",\"recordDate\":\"2000-01-01T12:00\",\"fileDate\":\"2000-01-01T12:00\",\"collectedVariables\":[{\"idVar\":\"TESTIDVAR\",\"values\":[\"V1\",\"V2\"],\"idLoop\":\"TESTIDLOOP\",\"idParent\":\"TESTIDPARENT\"}],\"externalVariables\":[{\"idVar\":\"TESTIDVAR\",\"values\":[\"V1\",\"V2\"]}]}"),
                objectMapper.readTree(objectMapper.writeValueAsString(surveyUnit))
        );
    }

}
