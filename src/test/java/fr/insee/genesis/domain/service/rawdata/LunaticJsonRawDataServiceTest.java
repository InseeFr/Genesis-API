package fr.insee.genesis.domain.service.rawdata;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.DataProcessingContextPersistancePortStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import fr.insee.genesis.stubs.SurveyUnitQualityToolPerretAdapterStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LunaticJsonRawDataServiceTest {
    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    FileUtils fileUtils = new FileUtils(new ConfigStub());
    ControllerUtils controllerUtils = new ControllerUtils(fileUtils);
    MetadataService metadataService = new MetadataService();

    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
    SurveyUnitService surveyUnitService = new SurveyUnitService(surveyUnitPersistencePortStub, metadataService, fileUtils);
    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();
    DataProcessingContextPersistancePortStub dataProcessingContextPersistancePortStub = new DataProcessingContextPersistancePortStub();
    SurveyUnitQualityToolPerretAdapterStub surveyUnitQualityToolPerretAdapterStub = new SurveyUnitQualityToolPerretAdapterStub();
    ConfigStub configStub = new ConfigStub();
    LunaticJsonRawDataService lunaticJsonRawDataService =
            new LunaticJsonRawDataService(
                    lunaticJsonRawDataPersistanceStub,
                    controllerUtils,
                    metadataService,
                    new SurveyUnitService(surveyUnitPersistencePortStub, metadataService, fileUtils),
                    new SurveyUnitQualityService(),
                    fileUtils,
                    new DataProcessingContextService(dataProcessingContextPersistancePortStub, surveyUnitPersistencePortStub),
                    surveyUnitQualityToolPerretAdapterStub,
                    configStub,
                    dataProcessingContextPersistancePortStub
            );

    @Test
    void saveDataTest_valid_only_collected_array() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        // We retrieve and cast the Map "COLLECTED"
        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR");

        // We retrieve and cast the Map "TESTVAR"
        Map<String, Object> testVarMap = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(testVarMap).isNotNull().hasSize(1).containsKey(DataState.COLLECTED.toString());

        // We retrieve and cast the List "COLLECTED"
        List<String> collectedList = JsonUtils.asStringList(testVarMap.get(DataState.COLLECTED.toString()));
        Assertions.assertThat(collectedList).isNotNull().isInstanceOf(List.class).containsExactly("test");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL")).isNull();
    }

    @Test
    void saveDataTest_valid_only_collected_value() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": \"test\"}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();
        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR");

        Map<String, Object> testVarMap = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(testVarMap).isNotNull().hasSize(1).containsKey(DataState.COLLECTED.toString());
        Assertions.assertThat(testVarMap.get(DataState.COLLECTED.toString())).isNotNull().isEqualTo("test");
        Assertions.assertThat(testVarMap.get(DataState.COLLECTED.toString())).isInstanceOf(String.class);

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL")).isNull();
    }

    @Test
    void saveDataTest_valid_only_external_array() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": [\"test\"]}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();
        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        Map<String, Object> externalData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL"));
        Assertions.assertThat(externalData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR_EXT");

        Object extVarValue = externalData.get("TESTVAR_EXT");
        Assertions.assertThat(extVarValue).isNotNull().isInstanceOf(List.class);
        List<String> extValCast = JsonUtils.asStringList(extVarValue);
        Assertions.assertThat(extValCast).containsExactly("test");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED")).isNull();
    }

    @Test
    void saveDataTest_valid_only_external_value() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test\"}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();
        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        Map<String, Object> externalData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL"));
        Assertions.assertThat(externalData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR_EXT");


        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isNotNull().isEqualTo("test");

        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isInstanceOf(String.class);

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED")).isNull();
    }

    @Test
    void saveDataTest_valid_both_only_collected_datastate() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test_ext\"}, " +
                "\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        Map<String, Object> externalData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL"));
        Assertions.assertThat(externalData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR_EXT");

        //External variable
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isNotNull().isEqualTo("test_ext");
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isInstanceOf(String.class);

        //Collected variable
        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR");
        Map<String,Object> colVarValue = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(colVarValue).containsOnlyKeys(DataState.COLLECTED.toString());
        Assertions.assertThat(colVarValue.get(DataState.COLLECTED.toString()))
                .isNotNull()
                .isInstanceOf(List.class);
        List<String> colVarCast = JsonUtils.asStringList(colVarValue.get(DataState.COLLECTED.toString()));
        Assertions.assertThat(colVarCast).containsExactly("test");
    }

    @Test
    void saveDataTest_valid_both_multiple_datastate() throws Exception {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test_ext\"}, " +
                "\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"], \"EDITED\": [\"test_ed\"]}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();

        Map<String, Object> externalData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("EXTERNAL"));
        Assertions.assertThat(externalData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR_EXT");

        //External variables
        Assertions.assertThat(externalData.get("TESTVAR_EXT"))
                .isNotNull()
                .isEqualTo("test_ext")
                .isInstanceOf(String.class);

        //Collected variables
        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData)
                .isNotNull()
                .hasSize(1)
                .containsOnlyKeys("TESTVAR");
        Map<String,Object> colVar = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(colVar).containsOnlyKeys(DataState.COLLECTED.toString(), DataState.EDITED.toString());
        Assertions.assertThat(colVar.get(DataState.COLLECTED.toString()))
                .isNotNull()
                .isInstanceOf(List.class);
        Assertions.assertThat(JsonUtils.asStringList(colVar.get(DataState.COLLECTED.toString()))).containsExactly("test");
        Assertions.assertThat(colVar.get(DataState.EDITED.toString()))
                .isNotNull()
                .isInstanceOf(List.class);
        Assertions.assertThat(JsonUtils.asStringList(colVar.get(DataState.EDITED.toString()))).containsExactly("test_ed");
    }

    @Test
    void convertRawData_should_not_throw_exception_if_external_not_present() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        assertDoesNotThrow(() -> lunaticJsonRawDataService.convertRawData(List.of(rawDataModel),new VariablesMap()));
    }

    @Test
    void convertRawData_if_external_not_present_test() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        List<SurveyUnitModel> suModels =  lunaticJsonRawDataService.convertRawData(List.of(rawDataModel),new VariablesMap());
        Assertions.assertThat(suModels).hasSize(1);
        Assertions.assertThat(suModels.getFirst().getCollectedVariables()).hasSize(1);
        Assertions.assertThat(suModels.getFirst().getExternalVariables()).isEmpty();
    }

    @Test
    void convertRawData_should_not_throw_exception_if_collected_not_present() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test\"}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        assertDoesNotThrow(() -> lunaticJsonRawDataService.convertRawData(List.of(rawDataModel),new VariablesMap()));
    }

    @Test
    void convertRawData_if_collected_not_present_test() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";
        String json = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test\"}}";

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        List<SurveyUnitModel> suModels =  lunaticJsonRawDataService.convertRawData(List.of(rawDataModel),new VariablesMap());
        Assertions.assertThat(suModels).hasSize(1);
        Assertions.assertThat(suModels.getFirst().getExternalVariables()).hasSize(1);
        Assertions.assertThat(suModels.getFirst().getCollectedVariables()).isEmpty();
    }


    @ParameterizedTest
    @ValueSource(ints = {5,500,5000,10000})
    void convertRawData_multipleBatchs(int rawDataSize) throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        List<String> interrogationIdList = prepareConvertTest(rawDataSize, campaignId, questionnaireId);
        //Activate review
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                        DataProcessingContextModel.builder()
                                .partitionId(campaignId)
                                .withReview(true)
                                .kraftwerkExecutionScheduleList(new ArrayList<>())
                                .build()
                )
        );

        //WHEN
        DataProcessResult dataProcessResult = lunaticJsonRawDataService.processRawData(campaignId, interrogationIdList,
                new ArrayList<>());

        //THEN
        Assertions.assertThat(dataProcessResult.dataCount()).isEqualTo(rawDataSize * 2/*EDITED*/);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(rawDataSize * 2);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps())
                .hasSize(Math.ceilDiv(rawDataSize, configStub.getRawDataProcessingBatchSize()));
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().getFirst()).containsKey(questionnaireId);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().getFirst().get(questionnaireId))
                .contains("TESTinterrogationId1");
    }

    @Test
    void convertRawData_review_desactivated() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        List<String> interrogationIdList = prepareConvertTest(1, campaignId, questionnaireId);

        //Desactivate review
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                        DataProcessingContextModel.builder()
                                .partitionId(campaignId)
                                .withReview(false)
                                .kraftwerkExecutionScheduleList(new ArrayList<>())
                                .build()
                )
        );

        //WHEN
        DataProcessResult dataProcessResult = lunaticJsonRawDataService.processRawData(campaignId, interrogationIdList,
                new ArrayList<>());

        //THEN
        Assertions.assertThat(dataProcessResult.dataCount()).isEqualTo(2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps()).isEmpty();
    }

    private List<String> prepareConvertTest(int rawDataSize, String campaignId, String questionnaireId) throws JsonProcessingException {
        //CLEAN
        surveyUnitPersistencePortStub.getMongoStub().clear();
        surveyUnitQualityToolPerretAdapterStub.getReceivedMaps().clear();
        dataProcessingContextPersistancePortStub.getMongoStub().clear();

        //GIVEN
        List<String> interrogationIdList = new ArrayList<>();
        for (int i = 0; i < rawDataSize; i++) {
            String interrogationId = "TESTinterrogationId" + (i + 1);
            String json = "{\"EXTERNAL\": {\"RPPRENOM\": \"TEST_EXT%d\"}, ".formatted(i) +
                    "\"COLLECTED\": {\"PRENOMREP\": {\"COLLECTED\": [\"test%d\"], \"EDITED\": [\"test_ed%d\"]}}}"
                            .formatted(i, i);

            LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                    .campaignId(campaignId)
                    .questionnaireId(questionnaireId)
                    .interrogationId(interrogationId)
                    .data(JsonUtils.jsonToMap(json))
                    .mode(Mode.WEB)
                    .build();

            interrogationIdList.add(interrogationId);
            lunaticJsonRawDataPersistanceStub.getMongoStub()
                    .add(LunaticJsonRawDataDocumentMapper.INSTANCE.modelToDocument(rawDataModel));
        }
        return interrogationIdList;
    }
}