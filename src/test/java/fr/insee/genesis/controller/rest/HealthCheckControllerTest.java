package fr.insee.genesis.controller.rest;

import fr.insee.genesis.domain.dtos.CollectedVariableDto;
import fr.insee.genesis.domain.dtos.DataState;
import fr.insee.genesis.domain.dtos.Mode;
import fr.insee.genesis.domain.dtos.SurveyUnitDto;
import fr.insee.genesis.domain.dtos.VariableDto;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.SurveyUnitService;
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
        List<VariableDto> externalVariableDtoList = new ArrayList<>();
        VariableDto variableDto = VariableDto.builder().idVar("TESTIDVAR").values(List.of(new String[]{"V1", "V2"})).build();
        externalVariableDtoList.add(variableDto);

        List<CollectedVariableDto> collectedVariableDtoList = new ArrayList<>();
        CollectedVariableDto collectedVariableDto = new CollectedVariableDto("TESTIDVAR", List.of(new String[]{"V1", "V2"}), "TESTIDLOOP", "TESTIDPARENT");
        collectedVariableDtoList.add(collectedVariableDto);
        surveyUnitPersistencePortStub.getMongoStub().add(SurveyUnitDto.builder()
                .idCampaign("TESTIDCAMPAIGN")
                .mode(Mode.WEB)
                .idUE("TESTIDUE")
                .idQuest("TESTIDQUESTIONNAIRE")
                .state(DataState.COLLECTED)
                .fileDate(LocalDateTime.of(2023, 1, 1, 0, 0, 0))
                .recordDate(LocalDateTime.of(2024, 1, 1, 0, 0, 0))
                .externalVariables(externalVariableDtoList)
                .collectedVariables(collectedVariableDtoList)
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
