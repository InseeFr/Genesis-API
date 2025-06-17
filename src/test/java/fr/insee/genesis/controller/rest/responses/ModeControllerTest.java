package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;

import static fr.insee.genesis.TestConstants.DEFAULT_QUESTIONNAIRE_ID;

class ModeControllerTest {
    //Given
    static ModeController modeControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        modeControllerStatic = new ModeController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
        Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then
    @Test
    void getModesByQuestionnaireTest() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByQuestionnaire(DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignTest() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByCampaign("TESTCAMPAIGNID");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    @Test
    void getModesByQuestionnaireV2Test() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByQuestionnaireV2(DEFAULT_QUESTIONNAIRE_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignV2Test() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByCampaignV2("TESTCAMPAIGNID");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

}
