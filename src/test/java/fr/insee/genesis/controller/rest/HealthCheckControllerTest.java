package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.stubs.ScheduleApiPortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
class HealthCheckControllerTest {
    static HealthCheckController healthCheckController;

    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static ScheduleApiPortStub scheduleApiPortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);
        List<VariableModel> externalVariableList = new ArrayList<>();
        VariableModel variable = VariableModel.builder().varId("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();

        externalVariableList.add(variable);

        List<VariableModel> collectedVariableList = new ArrayList<>();
        VariableModel collectedVariable = VariableModel.builder()
                .varId("TESTIDVAR")
                .values(List.of(new String[]{"V1", "V2"}))
                .loopId("TESTIDLOOP")
                .parentId("TESTIDPARENT")
                .build();


        collectedVariableList.add(collectedVariable);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitModel.builder()
                .campaignId("TESTCAMPAIGNID")
                .mode(Mode.WEB)
                .interrogationId("TESTINTERROGATIONID")
                .questionnaireId("TESTQUESTIONNAIREID")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .externalVariables(externalVariableList)
                .collectedVariables(collectedVariableList)
                .build());


        scheduleApiPortStub = new ScheduleApiPortStub();

        healthCheckController = new HealthCheckController(
                surveyUnitApiPort,
                scheduleApiPortStub
        );
    }

    @Test
    void mongoCountTest() {
        ResponseEntity<String> response = healthCheckController.healthcheckMongo();
        log.info(response.getBody());
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
    }
}
