package fr.insee.genesis.domain.service.rawdata;

import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.controller.services.MetadataService;
import fr.insee.genesis.controller.utils.ControllerUtils;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitQualityService;
import fr.insee.genesis.domain.service.surveyunit.SurveyUnitService;
import fr.insee.genesis.domain.utils.JsonUtils;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import fr.insee.genesis.stubs.ConfigStub;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import fr.insee.genesis.stubs.SurveyUnitPersistencePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class LunaticJsonRawDataServiceTest {
    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    FileUtils fileUtils = new FileUtils(new ConfigStub());
    ControllerUtils controllerUtils = new ControllerUtils(fileUtils);
    MetadataService metadataService = new MetadataService();
    SurveyUnitService surveyUnitService = new SurveyUnitService(new SurveyUnitPersistencePortStub());
    SurveyUnitQualityService surveyUnitQualityService = new SurveyUnitQualityService();

    LunaticJsonRawDataService lunaticJsonRawDataService = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub,controllerUtils,metadataService,surveyUnitService,surveyUnitQualityService,fileUtils);

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
        Assertions.assertThat(collectedData).isNotNull().hasSize(1);
        Assertions.assertThat(collectedData).containsOnlyKeys("TESTVAR");

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
        Assertions.assertThat(collectedData).isNotNull().hasSize(1);
        Assertions.assertThat(collectedData).containsOnlyKeys("TESTVAR");

        Map<String, Object> testVarMap = JsonUtils.asMap(collectedData.get("TESTVAR"));;
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
        Assertions.assertThat(externalData).isNotNull().hasSize(1);
        Assertions.assertThat(externalData).containsOnlyKeys("TESTVAR_EXT");

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
        Assertions.assertThat(externalData).isNotNull().hasSize(1);
        Assertions.assertThat(externalData).containsOnlyKeys("TESTVAR_EXT");


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
        Assertions.assertThat(externalData).isNotNull().hasSize(1);
        Assertions.assertThat(externalData).containsOnlyKeys("TESTVAR_EXT");

        //External variable
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isNotNull().isEqualTo("test_ext");
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isInstanceOf(String.class);

        //Collected variable
        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData).isNotNull().hasSize(1);
        Assertions.assertThat(collectedData).containsOnlyKeys("TESTVAR");
        Map<String,Object> colVarValue = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(colVarValue).containsOnlyKeys(DataState.COLLECTED.toString());
        Assertions.assertThat(colVarValue.get(DataState.COLLECTED.toString())).isNotNull();
        Assertions.assertThat(colVarValue.get(DataState.COLLECTED.toString())).isInstanceOf(List.class);
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
        Assertions.assertThat(externalData).isNotNull().hasSize(1);
        Assertions.assertThat(externalData).containsOnlyKeys("TESTVAR_EXT");

        //External variables
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isNotNull().isEqualTo("test_ext");
        Assertions.assertThat(externalData.get("TESTVAR_EXT")).isInstanceOf(String.class);

        //Collected variables
        Map<String, Object> collectedData = JsonUtils.asMap(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().get("COLLECTED"));
        Assertions.assertThat(collectedData).isNotNull().hasSize(1);
        Assertions.assertThat(collectedData).containsOnlyKeys("TESTVAR");
        Map<String,Object> colVar = JsonUtils.asMap(collectedData.get("TESTVAR"));
        Assertions.assertThat(colVar).containsOnlyKeys(DataState.COLLECTED.toString(), DataState.EDITED.toString());
        Assertions.assertThat(colVar.get(DataState.COLLECTED.toString())).isNotNull();
        Assertions.assertThat(JsonUtils.asStringList(colVar.get(DataState.COLLECTED.toString()))).containsExactly("test");
        Assertions.assertThat(colVar.get(DataState.EDITED.toString())).isNotNull();
        Assertions.assertThat(JsonUtils.asStringList(colVar.get(DataState.EDITED.toString()))).containsExactly("test_ed");
        Assertions.assertThat(colVar.get(DataState.COLLECTED.toString())).isInstanceOf(List.class);
        Assertions.assertThat(colVar.get(DataState.EDITED.toString())).isInstanceOf(List.class);
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



}