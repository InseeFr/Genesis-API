package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.model.surveyunit.InterrogationId;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static fr.insee.genesis.TestConstants.DEFAULT_INTERROGATION_ID;
import static fr.insee.genesis.TestConstants.DEFAULT_QUESTIONNAIRE_ID;

class InterrogationControllerTest {
    //Given
    static InterrogationController interrogationControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new MetadataService(),
                new FileUtils(new ConfigStub())
        );

        interrogationControllerStatic = new InterrogationController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then
    @Test
    void getAllInterrogationIdsByQuestionnaireTest() {
        ResponseEntity<List<InterrogationId>> response = interrogationControllerStatic.getAllInterrogationIdsByQuestionnaire(DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getInterrogationId()).isEqualTo(DEFAULT_INTERROGATION_ID);
    }

    @Test
    void countAllInterrogationIdsByQuestionnaireTest() {
        ResponseEntity<Long> response = interrogationControllerStatic.countAllInterrogationIdsByQuestionnaire(DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody()).isEqualTo(1L);
    }

}
