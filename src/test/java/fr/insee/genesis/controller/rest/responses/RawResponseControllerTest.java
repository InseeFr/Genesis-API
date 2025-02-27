package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.controller.dto.InterrogationId;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.stubs.LunaticJsonPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

class RawResponseControllerTest {
    //Given
    static RawResponseController rawResponseControllerStatic;
    static SurveyUnitPersistencePortStub surveyUnitPersistencePortStub;
    static LunaticJsonPersistanceStub lunaticJsonPersistanceStub;
    static List<InterrogationId> interrogationIdList;
    //Constants
    static final String DEFAULT_INTERROGATION_ID = "TESTINTERROGATIONID";

    @BeforeAll
    static void init() {
        surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();

        lunaticJsonPersistanceStub = new LunaticJsonPersistanceStub();
        LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonPersistanceStub);

        rawResponseControllerStatic = new RawResponseController(lunaticJsonRawDataApiPort);

        interrogationIdList = new ArrayList<>();
        interrogationIdList.add(new InterrogationId(DEFAULT_INTERROGATION_ID));
    }

    @BeforeEach
    void reset() throws IOException {
        Utils.reset(surveyUnitPersistencePortStub);
    }



    @Test
    void saveJsonRawDataFromStringTest(){
        lunaticJsonPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String interrogationId = "TESTUE1";
        String idUE = "TESTUE1";
        String questionnaireId = "SAMPLETEST-PARADATA-v1_quest";

        rawResponseControllerStatic.saveRawResponsesFromJsonBody(
                campaignId,
                interrogationId,
                idUE,
                questionnaireId,
                Mode.WEB,
                "{\"testdata\": \"test\"}"
        );

        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub()).isNotEmpty();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getCampaignId()).isNotNull().isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getInterrogationId()).isNotNull().isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getIdUE()).isNotNull().isEqualTo(idUE);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getQuestionnaireId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getMode()).isEqualTo(Mode.WEB);
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getData().get("testdata")).isNotNull();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getData().get("testdata")).isNotNull().isEqualTo("test");

        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(lunaticJsonPersistanceStub.getMongoStub().getFirst().getProcessDate()).isNull();

    }

}
