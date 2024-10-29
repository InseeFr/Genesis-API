package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.SurveyUnitId;
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

class IdUEControllerTest {
    //Given
    static IdUEController idUeControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        idUeControllerStatic = new IdUEController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then
      @Test
    void getAllIdUEsByQuestionnaireTest() {
        ResponseEntity<List<SurveyUnitId>> response = idUeControllerStatic.getAllIdUEsByQuestionnaire(defaultIdQuest);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().getFirst().getIdUE()).isEqualTo(defaultIdUE);
    }



}
