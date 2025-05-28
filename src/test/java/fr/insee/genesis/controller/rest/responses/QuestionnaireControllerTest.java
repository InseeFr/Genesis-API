package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
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
import java.util.Set;

import static fr.insee.genesis.TestConstants.DEFAULT_QUESTIONNAIRE_ID;

class QuestionnaireControllerTest {
    //Given
    static QuestionnaireController questionnaireControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new MetadataService(),
                new FileUtils(new ConfigStub())
        );

        questionnaireControllerStatic = new QuestionnaireController(surveyUnitApiPort);

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    @Test
    void getQuestionnairesTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_QUESTIONNAIRE_ID,"TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnairesByCampaign("TESTCAMPAIGNID");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_QUESTIONNAIRE_ID,"TESTQUESTIONNAIRE2");
    }


    @Test
    void getQuestionnairesWithCampaignsTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<List<QuestionnaireWithCampaign>> response = questionnaireControllerStatic.getQuestionnairesWithCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(questionnaireWithCampaign ->
                questionnaireWithCampaign.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
                        || questionnaireWithCampaign.getQuestionnaireId().equals("TESTQUESTIONNAIRE2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getQuestionnaireId().equals(DEFAULT_QUESTIONNAIRE_ID)
        ).findFirst().get().getCampaigns()).containsExactly("TESTCAMPAIGNID");

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getQuestionnaireId().equals("TESTQUESTIONNAIRE2")
        ).findFirst().get().getCampaigns()).containsExactly("TESTCAMPAIGNID", "TESTCAMPAIGN2");
    }

}
