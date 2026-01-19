package fr.insee.genesis.controller.rest.responses;


import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.dto.rawdata.LunaticJsonRawDataUnprocessedDto;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.RawResponseModel;
import fr.insee.genesis.domain.ports.api.LunaticJsonRawDataApiPort;
import fr.insee.genesis.domain.ports.api.RawResponseApiPort;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.exceptions.GenesisError;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import fr.insee.genesis.infrastructure.document.rawdata.RawResponseDocument;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.repository.RawResponseInputRepository;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistencePortStub;
import fr.insee.genesis.stubs.RawResponseDataPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitQualityToolPerretAdapterStub;
import fr.insee.modelefiliere.RawResponseDto;
import lombok.extern.slf4j.Slf4j;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.ResponseEntity;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Slf4j
class RawResponseControllerTest {
    private final FileUtils fileUtils = new FileUtils(new ConfigStub());
    private final LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    private final RawResponseDataPersistanceStub rawResponseDataPersistanceStub = new RawResponseDataPersistanceStub();
    private final SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    private final SurveyUnitQualityToolPerretAdapterStub surveyUnitQualityToolPerretAdapterStub = new SurveyUnitQualityToolPerretAdapterStub();
    private final DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub =
            new DataProcessingContextPersistancePortStub();
    private static final QuestionnaireMetadataPersistencePortStub questionnaireMetadataPersistencePortStub =
            new QuestionnaireMetadataPersistencePortStub();


    private final LunaticJsonRawDataApiPort lunaticJsonRawDataApiPort = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub,
            new ControllerUtils(fileUtils),
            new QuestionnaireMetadataService(questionnaireMetadataPersistencePortStub),
            new SurveyUnitService(surveyUnitPersistencePortStub, new QuestionnaireMetadataService(questionnaireMetadataPersistencePortStub), fileUtils),
            new SurveyUnitQualityService(),
            fileUtils,
            new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub),
            surveyUnitQualityToolPerretAdapterStub,
            new ConfigStub(),
            new DataProcessingContextPersistancePortStub()
    );

    // TODO: change this
    RawResponseInputRepository rawResponseInputRepositoryStub = new RawResponseInputRepository(null, null) {
        @Override
        public void saveAsRawJson(RawResponseDto dto) {
            // Ne rien faire — stub pour les tests
        }
    };

    RawResponseApiPort rawResponseApiPortStub = new RawResponseApiPort() {
        @Override
        public List<RawResponseModel> getRawResponses(String questionnaireModelId, Mode mode, List<String> interrogationIdList) {
            return List.of();
        }

        @Override
        public List<RawResponseModel> getRawResponsesByInterrogationID(String interrogationId) {
            return List.of();
        }

        @Override
        public DataProcessResult processRawResponses(String questionnaireId, List<String> interrogationIdList, List<GenesisError> errors) throws GenesisException {
            return null;
        }

        @Override
        public DataProcessResult processRawResponses(String collectionInstrumentId) throws GenesisException {
            return null;
        }

        @Override
        public List<SurveyUnitModel> convertRawResponse(List<RawResponseModel> rawResponsModels, VariablesMap variablesMap) {
            return List.of();
        }

        @Override
        public List<String> getUnprocessedCollectionInstrumentIds() {
            return List.of();
        }

        @Override
        public void updateProcessDates(List<SurveyUnitModel> surveyUnitModels) {

        }
        @Override
        public Page<RawResponseModel> findRawResponseDataByCampaignIdAndDate(String campaignId, Instant startDate, Instant endDate, Pageable pageable) {
            return rawResponseDataPersistanceStub.findByCampaignIdAndDate(campaignId, startDate, endDate, pageable);
        }

        @Override
        public long countByCollectionInstrumentId(String collectionInstrumentId) {
            return 0;
        }

        @Override
        public Set<String> getDistinctCollectionInstrumentIds() {
            return Set.of();
        }
        
        @Override
        public Page<RawResponseModel> findRawResponseDataByCollectionInstrumentId(String collectionInstrumentId, Pageable pageable) {
            return null;
        }
    };

    private final RawResponseController rawResponseController = new RawResponseController(lunaticJsonRawDataApiPort,  rawResponseApiPortStub, rawResponseInputRepositoryStub);


    @Test
    void saveJsonRawDataFromStringTest() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnprocessedJsonRawData().getBody();

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
        List<LunaticJsonRawDataUnprocessedDto> dtos = rawResponseController.getUnprocessedJsonRawData().getBody();

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
        String campaignId = "SAMPLETEST-PARADATA-V2";
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
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectionInstrumentId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isNotNull().isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getUsualSurveyUnitId()).isEqualTo(idUE);
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
    void processJsonRawDataV2Test(){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        surveyUnitPersistencePortStub.getMongoStub().clear();
        surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().clear();
        String questionnaireId = "SAMPLETEST-PARADATA-V2";
        String interrogationId = "testinterrogationId1";
        String idUE = "testIdUE1";
        String varName = "AVIS_MAIL";
        String varValue = "TEST";
        addJsonRawDataDocumentToStub(questionnaireId, questionnaireId, interrogationId, idUE, null, LocalDateTime.now(),varName
                , varValue);

        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                        DataProcessingContextModel.builder()
                                .partitionId(questionnaireId)
                                .kraftwerkExecutionScheduleList(new ArrayList<>())
                                .withReview(true)
                                .build()
                )
        );

        //WHEN
        ResponseEntity response = rawResponseController.processJsonRawData(questionnaireId);


        //THEN
        if(!response.getStatusCode().is2xxSuccessful()){
            log.error(response.getBody().toString());
        }
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();

        //Genesis model survey unit created successfully
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).isNotNull().isNotEmpty().hasSize(1);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst()).isNotNull();
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCampaignId()).isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getCollectionInstrumentId()).isNotNull().isEqualTo(questionnaireId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getMode()).isNotNull().isEqualTo(Mode.WEB);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getInterrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub().getFirst().getUsualSurveyUnitId()).isEqualTo(idUE);
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
    void getLunaticJsonRawDataModelFromJsonBody() {
        //GIVEN
        String campaignId = "getLunaticJsonRawDataModelFromJsonBody";
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
        ResponseEntity<PagedModel<LunaticJsonRawDataModel>> response = rawResponseController.getLunaticJsonRawDataModelFromJsonBody(campaignId, starDate, endDate, page, size);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getContent()).hasSize(1);
    }

    @Test
    void getRawResponsesFromJsonBody() {
        //GIVEN
        String campaignId = "VPPI2024M05";
        String questionnaireId = campaignId + "_quest";
        String interrogationId = "getRawResponsesFromJsonBody_id1";
        Instant recordDate = Instant.parse("2025-01-01T01:00:00.000Z");

        addJsonRawResponseDataDocumentToStub(campaignId, questionnaireId, interrogationId);

        Instant starDate= recordDate.minusSeconds(86400),endDate = recordDate.plusSeconds(86400);
        int page=0, size= 10;

        //WHEN
        ResponseEntity<PagedModel<RawResponseModel>> response = rawResponseController.getRawResponsesFromJsonBody(campaignId, starDate, endDate, page, size);

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

    private void addJsonRawResponseDataDocumentToStub(String campaignId, String questionnaireId, String interrogationId) {
        RawResponseDocument rawResponseDocument = RawResponseDocument.builder()
                .collectionInstrumentId(questionnaireId)
                .interrogationId(interrogationId)
                .recordDate(LocalDateTime.now())
                .payload(Map.of("campaignId", campaignId))
                .build();

        rawResponseDataPersistanceStub.getMongoStub().add(rawResponseDocument);
    }

    @Test
    void getRawResponsesFromJsonBody_filterByCampaignId() {
        // GIVEN
        rawResponseDataPersistanceStub.getMongoStub().clear();

        String campaignId = "CAMPAIGN_OK";
        String questionnaireId = campaignId + "_QUEST";
        String interrogationId1 = "INT_1";
        String interrogationId2 = "INT_2";

        // document with the wanted campaignId
        RawResponseDocument rawResponseDocument = RawResponseDocument.builder()
                .collectionInstrumentId(questionnaireId)
                .interrogationId(interrogationId1)
                .recordDate(LocalDateTime.now())
                .payload(Map.of("campaignId", campaignId))
                .build();

        // document with another campaignId
        RawResponseDocument rawResponseDocument1 = RawResponseDocument.builder()
                .collectionInstrumentId("OTHER_QUEST")
                .interrogationId(interrogationId2)
                .recordDate(LocalDateTime.now())
                .payload(Map.of("campaignId", "OTHER_CAMPAIGN"))
                .build();

        rawResponseDataPersistanceStub.getMongoStub().addAll(List.of(rawResponseDocument, rawResponseDocument1));

        Instant startDate = Instant.now().minusSeconds(3600);
        Instant endDate = Instant.now().plusSeconds(3600);

        // WHEN
        ResponseEntity<PagedModel<RawResponseModel>> response =
                rawResponseController.getRawResponsesFromJsonBody(
                        campaignId,
                        startDate,
                        endDate,
                        0,
                        10
                );

        // THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(response.getBody()).isNotNull();
        Assertions.assertThat(response.getBody().getContent()).hasSize(1);

        RawResponseModel result = response.getBody().getContent().getFirst();
        Assertions.assertThat(result.interrogationId()).isEqualTo(interrogationId1);
        Assertions.assertThat(result.payload().get("campaignId")).isEqualTo(campaignId);
    }

}
