package fr.insee.genesis.domain.model.surveyunit;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.Test;
import org.junit.jupiter.api.Assertions;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SurveyUnitModelTest {
    @Test
    public void toJSONTest() throws JsonProcessingException {
        List<Variable> externalVariableList = new ArrayList<>();
        Variable variable = Variable.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableList.add(variable);

        List<CollectedVariable> collectedVariableList = new ArrayList<>();
        CollectedVariable collectedVariable = new CollectedVariable("TESTIDVAR", List.of(new String[]{"V1", "V2"}),"TESTIDLOOP","TESTIDPARENT");
        collectedVariableList.add(collectedVariable);

        SurveyUnitModel surveyUnitModel = SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .questionnaireId("TESTQUESTIONNAIREID")
                .interrogationId("TESTINTERROGATIONID")
                .mode(Mode.WEB)
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2000,1,1,0,0,0))
                .recordDate(LocalDateTime.of(2000,1,1,0,0,0))
                .collectedVariables(collectedVariableList)
                .externalVariables(externalVariableList)
                .build();

        Assertions.assertNotNull(surveyUnitModel);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.findAndRegisterModules();

        Assertions.assertEquals(
                objectMapper.readTree("{\"questionnaireId\":\"TESTQUESTIONNAIREID\",\"campaignId\":\"TESTCAMPAIGNID\",\"interrogationId\":\"TESTINTERROGATIONID\",\"state\":\"COLLECTED\",\"mode\":\"WEB\",\"recordDate\":\"2000-01-01T12:00\",\"fileDate\":\"2000-01-01T12:00\",\"collectedVariables\":[{\"varId\":\"TESTIDVAR\",\"values\":[\"V1\",\"V2\"],\"loopId\":\"TESTIDLOOP\",\"parentId\":\"TESTIDPARENT\"}],\"externalVariables\":[{\"varId\":\"TESTIDVAR\",\"values\":[\"V1\",\"V2\"]}]}"),
                objectMapper.readTree(objectMapper.writeValueAsString(surveyUnitModel))
        );
    }

}
