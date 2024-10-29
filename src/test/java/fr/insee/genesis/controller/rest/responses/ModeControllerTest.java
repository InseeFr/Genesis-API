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

class ModeControllerTest {
    //Given
    static ModeController modeControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

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
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByQuestionnaire(defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignTest() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

}
