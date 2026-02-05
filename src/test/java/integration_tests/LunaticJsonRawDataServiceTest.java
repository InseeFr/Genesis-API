package integration_tests;

import com.fasterxml.jackson.core.JsonProcessingException;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.context.DataProcessingContextModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.DataProcessResult;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.service.context.DataProcessingContextService;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.domain.service.rawdata.LunaticJsonRawDataService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.mappers.DataProcessingContextMapper;
import fr.insee.genesis.infrastructure.mappers.LunaticJsonRawDataDocumentMapper;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import integration_tests.stubs.ConfigStub;
import integration_tests.stubs.DataProcessingContextPersistancePortStub;
import integration_tests.stubs.LunaticJsonRawDataPersistanceStub;
import integration_tests.stubs.QuestionnaireMetadataPersistencePortStub;
import integration_tests.stubs.SurveyUnitPersistencePortStub;
import integration_tests.stubs.SurveyUnitQualityToolPerretAdapterStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LunaticJsonRawDataServiceTest {
    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    FileUtils fileUtils = new FileUtils(new ConfigStub());
    ControllerUtils controllerUtils = new ControllerUtils(fileUtils);

    static QuestionnaireMetadataService metadataService =
            new QuestionnaireMetadataService(new QuestionnaireMetadataPersistencePortStub());

    SurveyUnitPersistencePortStub surveyUnitPersistencePortStub = new SurveyUnitPersistencePortStub();
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
    void getRawDataByInterrogationId_shouldReturnOnlyMatchingData() {
        // GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();

        String InterrogationId = "INTERROGATION_1";
        String secondInterrogationId = "INTERROGATION_2";

        LunaticJsonRawDataModel rawData1 = LunaticJsonRawDataModel.builder()
                .campaignId("CAMPAIGN")
                .questionnaireId("QUESTIONNAIRE")
                .interrogationId(InterrogationId)
                .data(Map.of("key", "value1"))
                .mode(Mode.WEB)
                .build();

        LunaticJsonRawDataModel rawData2 = LunaticJsonRawDataModel.builder()
                .campaignId("CAMPAIGN")
                .questionnaireId("QUESTIONNAIRE")
                .interrogationId(secondInterrogationId)
                .data(Map.of("key", "value2"))
                .mode(Mode.WEB)
                .build();

        lunaticJsonRawDataService.save(rawData1);
        lunaticJsonRawDataService.save(rawData2);

        // WHEN
        List<LunaticJsonRawDataModel> result =
                lunaticJsonRawDataService.getRawDataByInterrogationId(InterrogationId);

        // THEN
        Assertions.assertThat(result)
                .hasSize(1)
                .allMatch(data -> InterrogationId.equals(data.interrogationId()));

        Assertions.assertThat(result.getFirst().data())
                .containsEntry("key", "value1");
    }

    @Test
    void convertRawData_should_not_throw_exception_if_collected_not_present() throws Exception {
        //GIVEN
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
        String campaignId = "SAMPLETEST-PARADATA-V1";
        String questionnaireId = "SAMPLETEST-PARADATA-V1";
        List<String> interrogationIdList = prepareConvertTest(rawDataSize, campaignId, questionnaireId);
        //Activate review
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                        DataProcessingContextModel.builder()
                                .collectionInstrumentId(questionnaireId)
                                .withReview(true)
                                .kraftwerkExecutionScheduleList(new ArrayList<>())
                                .build()
                )
        );

        //WHEN
        DataProcessResult dataProcessResult = lunaticJsonRawDataService.processRawData(questionnaireId);

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
        String campaignId = "SAMPLETEST-PARADATA-V1";
        String questionnaireId = "SAMPLETEST-PARADATA-V1";
        List<String> interrogationIdList = prepareConvertTest(1, campaignId, questionnaireId);

        //Desactivate review
        dataProcessingContextPersistancePortStub.getMongoStub().add(
                DataProcessingContextMapper.INSTANCE.modelToDocument(
                        DataProcessingContextModel.builder()
                                .collectionInstrumentId(questionnaireId)
                                .withReview(false)
                                .kraftwerkExecutionScheduleList(new ArrayList<>())
                                .build()
                )
        );

        //WHEN
        DataProcessResult dataProcessResult = lunaticJsonRawDataService.processRawData(questionnaireId);

        //THEN
        Assertions.assertThat(dataProcessResult.dataCount()).isEqualTo(2);
        Assertions.assertThat(surveyUnitPersistencePortStub.getMongoStub()).hasSize(2);
        Assertions.assertThat(surveyUnitQualityToolPerretAdapterStub.getReceivedMaps()).isEmpty();
    }

    @Test
    void getUnprocessedDataIdsTest_only_processed_data() throws JsonProcessingException {
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
                .processDate(LocalDateTime.now())
                .build();

        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);

        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataIds()).isEmpty();
    }
    @Test
    void getUnprocessedDataIdsTest_unprocessed_data() throws JsonProcessingException {
        surveyUnitPersistencePortStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-V1";
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
                .processDate(LocalDateTime.now())
                .build();

        String interrogationId2 = "TESTinterrogationId2";
        String json2 = "{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test_ext2\"}, " +
                "\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test2\"], \"EDITED\": [\"test_ed2\"]}}}";

        LunaticJsonRawDataModel rawDataModel2 = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId2)
                .data(JsonUtils.jsonToMap(json2))
                .mode(Mode.WEB)
                .build();
        //WHEN
        lunaticJsonRawDataService.save(rawDataModel);
        lunaticJsonRawDataService.save(rawDataModel2);

        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataIds()).isNotEmpty();
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataIds()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataService.getUnprocessedDataIds().getFirst().interrogationId()).isEqualTo("TESTinterrogationId2");
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

    @Test
    void convertRawData_collected_array_with_null_and_empty_values_should_ignore_them() throws Exception {
        // GIVEN
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        String json = """
        {
          "COLLECTED": {
            "TESTVAR": {
              "COLLECTED": [null, "", null, "12", ""]
            }
          }
        }
        """;

        LunaticJsonRawDataModel rawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId(campaignId)
                .questionnaireId(questionnaireId)
                .interrogationId(interrogationId)
                .data(JsonUtils.jsonToMap(json))
                .mode(Mode.WEB)
                .build();

        // WHEN
        List<SurveyUnitModel> suModels =
                lunaticJsonRawDataService.convertRawData(List.of(rawDataModel), new VariablesMap());

        // THEN
        Assertions.assertThat(suModels).hasSize(1);

        SurveyUnitModel suModel = suModels.getFirst();
        List<VariableModel> collectedVars = suModel.getCollectedVariables();

        // Only the non-empty values should be persisted
        Assertions.assertThat(collectedVars).hasSize(1);
        System.out.println(suModel);

        VariableModel variableModel = collectedVars.getFirst();
        Assertions.assertThat(variableModel.varId()).isEqualTo("TESTVAR");
        Assertions.assertThat(variableModel.value()).isEqualTo("12");

        Assertions.assertThat(variableModel.iteration()).isEqualTo(4);
    }
}