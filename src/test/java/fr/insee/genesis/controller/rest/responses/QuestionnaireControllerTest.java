package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
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
import java.util.Set;

class QuestionnaireControllerTest {
    //Given
    static QuestionnaireController questionnaireControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        questionnaireControllerStatic = new QuestionnaireController(surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    @Test
    void getQuestionnairesTest() {
        Utils.addAdditionalDtoToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                defaultIdQuest,"TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        Utils.addAdditionalDtoToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnairesByCampaign("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                defaultIdQuest,"TESTQUESTIONNAIRE2");
    }


    @Test
    void getQuestionnairesWithCampaignsTest() {
        Utils.addAdditionalDtoToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);
        Utils.addAdditionalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<List<QuestionnaireWithCampaign>> response = questionnaireControllerStatic.getQuestionnairesWithCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(questionnaireWithCampaign ->
                questionnaireWithCampaign.getIdQuestionnaire().equals(defaultIdQuest)
                        || questionnaireWithCampaign.getIdQuestionnaire().equals("TESTQUESTIONNAIRE2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getIdQuestionnaire().equals(defaultIdQuest)
        ).findFirst().get().getCampaigns()).containsExactly("TESTIDCAMPAIGN");

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getIdQuestionnaire().equals("TESTQUESTIONNAIRE2")
        ).findFirst().get().getCampaigns()).containsExactly("TESTIDCAMPAIGN", "TESTCAMPAIGN2");
    }

}
