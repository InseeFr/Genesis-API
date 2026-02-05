package integration_tests;

import fr.insee.genesis.controller.rest.responses.ModeController;
import fr.insee.genesis.domain.model.surveyunit.Mode;
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

import static fr.insee.genesis.TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID;

class ModeControllerTest {
    public static final String CAMPAIGN_ID = "TEST-TABLEAUX";
    //Given
    static ModeController modeControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
        SurveyUnitApiPort surveyUnitApiPort = new SurveyUnitService(
                surveyUnitPersistencePortStub,
                new QuestionnaireMetadataService(new QuestionnaireMetadataPersistencePortStub()),
                new FileUtils(new ConfigStub())
        );

        modeControllerStatic = new ModeController( surveyUnitApiPort );

    }

    @BeforeEach
    void reset() throws IOException {
        Utils.reset(surveyUnitPersistencePortStub);
    }


    //When + Then
    @Test
    void getModesByQuestionnaireTest() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByQuestionnaire(DEFAULT_COLLECTION_INSTRUMENT_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignTest() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByCampaign(CAMPAIGN_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    //========= OPTIMISATIONS PERFS (START) ==========
    @Test
    void getModesByQuestionnaireV2Test() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByQuestionnaireV2(DEFAULT_COLLECTION_INSTRUMENT_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }

    @Test
    void getModesByCampaignV2Test() {
        ResponseEntity<List<Mode>> response = modeControllerStatic.getModesByCampaignV2(CAMPAIGN_ID);

        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(response.getBody().getFirst()).isEqualTo(Mode.WEB);
    }
    //========= OPTIMISATIONS PERFS (END) ==========

}
