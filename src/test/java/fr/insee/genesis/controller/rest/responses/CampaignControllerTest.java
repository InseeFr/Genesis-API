package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
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

class CampaignControllerTest {
    //Given
    static CampaignController campaignControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    //Constants
    static final String defaultIdUE = "TESTIDUE";
    static final String defaultIdQuest = "TESTIDQUESTIONNAIRE";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(surveyUnitPersistencePortStub);

        campaignControllerStatic = new CampaignController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    @Test
    void getCampaignsTest() {
        Utils.addAdditionalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = campaignControllerStatic.getCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsExactly(
                "TESTIDCAMPAIGN","TESTCAMPAIGN2");
    }

    @Test
    void getCampaignsWithQuestionnairesTest() {
        Utils.addAdditionalDtoToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);
        Utils.addAdditionalDtoToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<List<CampaignWithQuestionnaire>> response = campaignControllerStatic.getCampaignsWithQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(campaignWithQuestionnaire ->
                        campaignWithQuestionnaire.getIdCampaign().equals("TESTIDCAMPAIGN")
                                || campaignWithQuestionnaire.getIdCampaign().equals("TESTCAMPAIGN2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getIdCampaign().equals("TESTIDCAMPAIGN")
        ).findFirst().get().getQuestionnaires()).containsExactly(defaultIdQuest, "TESTQUESTIONNAIRE2");

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getIdCampaign().equals("TESTCAMPAIGN2")
        ).findFirst().get().getQuestionnaires()).containsExactly("TESTQUESTIONNAIRE2");
    }

}
