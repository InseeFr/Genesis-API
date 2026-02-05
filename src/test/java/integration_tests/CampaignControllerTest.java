package integration_tests;

import fr.insee.genesis.controller.dto.CampaignWithQuestionnaire;
import fr.insee.genesis.controller.rest.responses.CampaignController;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import integration_tests.stubs.ConfigStub;
import integration_tests.stubs.QuestionnaireMetadataPersistencePortStub;
import integration_tests.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import static fr.insee.genesis.TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;

class CampaignControllerTest {
    //Given
    static CampaignController campaignControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new QuestionnaireMetadataService(new QuestionnaireMetadataPersistencePortStub()),
                new FileUtils(new ConfigStub())
        );

        campaignControllerStatic = new CampaignController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
       Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    @Test
    void getCampaignsTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = campaignControllerStatic.getCampaigns();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                "TEST-TABLEAUX","TESTCAMPAIGN2");
    }

    @Test
    void getCampaignsWithQuestionnairesTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTCAMPAIGN2","TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<List<CampaignWithQuestionnaire>> response = campaignControllerStatic.getCampaignsWithQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty();
        Assertions.assertThat(response.getBody().stream().filter(campaignWithQuestionnaire ->
                        campaignWithQuestionnaire.getCampaignId().equals("TEST-TABLEAUX")
                                || campaignWithQuestionnaire.getCampaignId().equals("TESTCAMPAIGN2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getCampaignId().equals("TEST-TABLEAUX")
        ).findFirst().get().getQuestionnaires()).containsOnly(DEFAULT_COLLECTION_INSTRUMENT_ID, "TESTQUESTIONNAIRE2");

        Assertions.assertThat(response.getBody().stream().filter(
                campaignWithQuestionnaire -> campaignWithQuestionnaire.getCampaignId().equals("TESTCAMPAIGN2")
        ).findFirst().get().getQuestionnaires()).containsOnly("TESTQUESTIONNAIRE2");
    }

}
