package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.QuestionnaireWithCampaign;
import fr.insee.genesis.domain.ports.api.SurveyUnitApiPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.infrastructure.document.context.DataProcessingContextDocument;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static fr.insee.genesis.TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;

class QuestionnaireControllerTest {
    //Given
    static QuestionnaireController questionnaireControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new QuestionnaireMetadataService(new QuestionnaireMetadataPersistencePortStub()),
                new FileUtils(new ConfigStub())
        );

        questionnaireControllerStatic = new QuestionnaireController(
                surveyUnitApiPort,
                new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub)
        );

    }

    @BeforeEach
    void reset() throws IOException {
        dataProcessingContextPersistancePortStub.getMongoStub().clear();
        Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then

    @Test
    void getQuestionnairesTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnaires();

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID,"TESTQUESTIONNAIRE2");
    }

    @Test
    void getQuestionnairesWithReviewTest() {

        String questionnaireId = "TESTQUESTIONNAIRE2";
        Utils.addAdditionalSurveyUnitModelToMongoStub(questionnaireId, surveyUnitPersistencePortStub);

        ResponseEntity<List<String>> response = questionnaireControllerStatic.getQuestionnairesWithReview(true);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isEmpty();

        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setCollectionInstrumentId(DEFAULT_COLLECTION_INSTRUMENT_ID);
        doc.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc.setWithReview(true);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc);

        response = questionnaireControllerStatic.getQuestionnairesWithReview(true);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID);

        DataProcessingContextDocument doc2 = new DataProcessingContextDocument();
        doc2.setCollectionInstrumentId(questionnaireId);
        doc2.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc2.setWithReview(false);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc2);

        response = questionnaireControllerStatic.getQuestionnairesWithReview(true);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID);
    }

    @Test
    void getQuestionnairesWithoutReviewTest() {

        String questionnaireId = "TESTQUESTIONNAIRE2";
        Utils.addAdditionalSurveyUnitModelToMongoStub(questionnaireId, surveyUnitPersistencePortStub);

        ResponseEntity<List<String>> response = questionnaireControllerStatic.getQuestionnairesWithReview(false);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isEmpty();

        DataProcessingContextDocument doc = new DataProcessingContextDocument();
        doc.setCollectionInstrumentId(DEFAULT_COLLECTION_INSTRUMENT_ID);
        doc.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc.setWithReview(false);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc);

        response = questionnaireControllerStatic.getQuestionnairesWithReview(false);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID);

        DataProcessingContextDocument doc2 = new DataProcessingContextDocument();
        doc2.setCollectionInstrumentId(questionnaireId);
        doc2.setKraftwerkExecutionScheduleList(new ArrayList<>());
        doc2.setWithReview(true);
        dataProcessingContextPersistancePortStub.getMongoStub().add(doc2);
        response = questionnaireControllerStatic.getQuestionnairesWithReview(false);
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID);
    }

    @Test
    void getQuestionnairesByCampaignTest() {
        Utils.addAdditionalSurveyUnitModelToMongoStub("TESTQUESTIONNAIRE2", surveyUnitPersistencePortStub);

        ResponseEntity<Set<String>> response = questionnaireControllerStatic.getQuestionnairesByCampaign("TEST-TABLEAUX");

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().containsOnly(
                DEFAULT_COLLECTION_INSTRUMENT_ID,"TESTQUESTIONNAIRE2");
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
                questionnaireWithCampaign.getQuestionnaireId().equals(DEFAULT_COLLECTION_INSTRUMENT_ID)
                        || questionnaireWithCampaign.getQuestionnaireId().equals("TESTQUESTIONNAIRE2")
        )).isNotNull().isNotEmpty().hasSize(2);

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getQuestionnaireId().equals(DEFAULT_COLLECTION_INSTRUMENT_ID)
        ).findFirst().get().getCampaigns()).containsExactly("TEST-TABLEAUX");

        Assertions.assertThat(response.getBody().stream().filter(
                questionnaireWithCampaign -> questionnaireWithCampaign.getQuestionnaireId().equals("TESTQUESTIONNAIRE2")
        ).findFirst().get().getCampaigns()).containsExactly("TEST-TABLEAUX", "TESTCAMPAIGN2");
    }

}
