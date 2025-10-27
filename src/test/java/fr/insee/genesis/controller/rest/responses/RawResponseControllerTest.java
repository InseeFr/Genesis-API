package fr.insee.genesis.controller.rest.responses;


import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitQualityToolPerretAdapterStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class RawResponseControllerTest {
    private final FileUtils fileUtils = new FileUtils(new ConfigStub());
    private final LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    private final SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    private final SurveyUnitQualityToolPerretAdapterStub surveyUnitQualityToolPerretAdapterStub = new SurveyUnitQualityToolPerretAdapterStub();
    private final DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub =
            new DataProcessingContextPersistancePortStub();
    private static final QuestionnaireMetadataPersistancePortStub questionnaireMetadataPersistancePortStub =
            new QuestionnaireMetadataPersistancePortStub();


    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub,
            new ControllerUtils(fileUtils),
            new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub),
            new SurveyUnitService(surveyUnitPersistencePortStub, new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub), fileUtils),
            new SurveyUnitQualityService(),
            fileUtils,
            new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub),
            surveyUnitQualityToolPerretAdapterStub,
            new ConfigStub(),
            new DataProcessingContextPersistancePortStub()
    );

    private final RawResponseController rawResponseController = new RawResponseController(lunaticJsonRawDataApiPort);


    @Test
    void saveJsonRawDataFromStringTest() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "testIdQuest".toUpperCase();
        String interrogationId = "testinterrogationId";
        String idUE = "testIdUE";
        Map<String,Object> json = JsonUtils.jsonToMap("{\"COLLECTED\": {\"testdata\": {\"COLLECTED\": [\"test\"]}}}");

        //WHEN
        ResponseEntity<String> response = rawResponseController.saveRawResponsesFromJsonBody(
                campaignId
                , questionnaireId
                , interrogationId
                , idUE
                , Mode.WEB
                , json
        );

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).isNotEmpty().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isNotNull().isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().idUE()).isNotNull().isEqualTo(idUE);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isNotNull().isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().mode()).isEqualTo(Mode.WEB);

        Map<String, Object> collectedVar = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedVar.get("testdata")).isNotNull();

        Object value = JsonUtils.asMap(collectedVar.get("testdata")).get(DataState.COLLECTED.toString());
        Assertions.assertThat(value).isNotNull().isInstanceOf(List.class);
        List<?> list = (List<?>) value;
        Assertions.assertThat(list).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().recordDate()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().processDate()).isNull();
    }

    @Test
    void getUnprocessedDataTest(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST1";
        String questionnaireId = "QUEST1";
        String interrogationId = "testinterrogationId1";
        addJsonRawDataDocumentToStub(campaignId, questionnaireId, interrogationId, null);

        //WHEN
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnproccessedJsonRawData().getBody();

        //THEN
        Assertions.assertThat(dtos).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(dtos.getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(dtos.getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(dtos.getFirst().interrogationId()).isEqualTo(interrogationId);
    }

    @Test
    void getUnprocessedDataTest_processDate_present(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST2";
        String questionnaireId = "QUEST2";
        String interrogationId = "testinterrogationId2";
        addJsonRawDataDocumentToStub(campaignId, questionnaireId, interrogationId, LocalDateTime.now());

        //WHEN
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnproccessedJsonRawData().getBody();

        //THEN
        Assertions.assertThat(dtos).isNotNull().isEmpty();
    }

    //raw data process
    //json
    @Test
    void processJsonRawDataTest(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        surveyUnitPersistencePortStub.getMongoStub().clear();
        surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().clear();
        String campaignId = "SAMPLETEST-PARADATA-v2";
        String questionnaireId = campaignId + "_quest";
        String interrogationId = "testinterrogationId1";
        String idUE = "testIdUE1";
        String varName = "AVIS_MAIL";
        String varValue = "TEST";
        addJsonRawDataDocumentToStub(campaignId, questionnaireId, interrogationId, idUE, null, LocalDateTime.now(),varName
                , varValue);

        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                  DataProcessingContextModel.builder()
                          .partitionId(campaignId)
                          .kraftwerkExecutionScheduleList(new ArrayList<>())
                          .withReview(true)
                          .build()
                )
        );


        List<String> interrogationIdList = new ArrayList<>();
        interrogationIdList.add(interrogationId);

        //WHEN
        rawResponseController.processJsonRawData(campaignId, questionnaireId, interrogationIdList);


        //THEN
        //Genesis model survey unit created successfully
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCampaignId()).isEqualTo(campaignId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getQuestionnaireId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isNotNull().isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getIdUE()).isEqualTo(idUE);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getFileDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getRecordDate()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().varId()).isNotNull().isEqualTo(varName);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectedVariables().getFirst().value()).isNotNull().isEqualTo(varValue);

        //Process date check
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().processDate()).isNotNull();

        //Perret call check
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps())
                .hasSize(1);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().getFirst()).containsKey(questionnaireId);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().getFirst().get(questionnaireId))
                .contains(interrogationId);
    }


    @Test
    void getRawResponsesFromJsonBody() {
        //GIVEN
        String campaignId = "getRawResponsesFromJsonBody";
        String questionnaireId = campaignId + "_quest";
        String interrogationId = "getRawResponsesFromJsonBody_id1";
        String varName = "VARName1";
        String varValue = "TEST";
        Instant recordDate = Instant.parse("2025-01-01T01:00:00.000Z");
        Instant processDate = Instant.parse("2025-01-02T01:00:00.000Z");

        addJsonRawDataDocumentToStub(campaignId, questionnaireId, interrogationId, null,
                LocalDateTime.ofInstant(processDate, ZoneOffset.UTC),
                LocalDateTime.ofInstant(recordDate, ZoneOffset.UTC),
                varName, varValue);

        Instant starDate= recordDate.minusSeconds(86400),endDate = recordDate.plusSeconds(86400);
        int page=0, size= 10;

        //WHEN
        ResponseEntity<PagedModel<LunaticJsonRawDataModel>> response = rawResponseController.getRawResponsesFromJsonBody(campaignId, starDate, endDate, page, size);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        org.junit.jupiter.api.Assertions.assertNotNull(response.getBody());
        Assertions.assertThat(response.getBody().getContent()).hasSize(1);
    }

    //Utils
    private void addJsonRawDataDocumentToStub(String campaignId, String questionnaireId, String interrogationId,
                                                     LocalDateTime processDate) {
        LunaticJsonRawDataDocument lunaticJsonDataDocument = LunaticJsonRawDataDocument.builder()
                .campaignId(campaignId)
                .mode(Mode.WEB)
                .interrogationId(interrogationId)
                .questionnaireId(questionnaireId)
                .recordDate(LocalDateTime.now())
                .processDate(processDate)
                .build();

        lunaticJsonRawDataPersistanceStub.getMongoStub().add(lunaticJsonDataDocument);
    }

    private void addJsonRawDataDocumentToStub(String campaignId,
                                              String questionnaireId,
                                              String interrogationId,
                                              String idUE,
                                              LocalDateTime processDate,
                                              LocalDateTime recordDate,
                                              String variableName,
                                              String variableValue)
    {
        Map<String, Object> jsonMap = Map.of(
                "COLLECTED", Map.of(variableName, Map.of("COLLECTED", variableValue)),
                "EXTERNAL", Map.of(variableName + "_EXTERNAL", variableValue + "_EXTERNAL")
        );

        LunaticJsonRawDataDocument lunaticJsonDataDocument = LunaticJsonRawDataDocument.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .mode(Mode.WEB)
                .interrogationId(interrogationId)
                .idUE(idUE)
                .recordDate(recordDate)
                .processDate(processDate)
                .data(jsonMap)
                .build();

        lunaticJsonRawDataPersistanceStub.getMongoStub().add(lunaticJsonDataDocument);
    }
}
