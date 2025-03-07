package fr.insee.genesis.domain.service.rawdata;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawData;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataCollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataModel;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataVariable;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.stubs.LunaticJsonRawDataPersistanceStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

class LunaticJsonRawDataServiceTest {
    LunaticJsonRawDataPersistanceStub lunaticJsonRawDataPersistanceStub = new LunaticJsonRawDataPersistanceStub();
    LunaticJsonRawDataService lunaticJsonRawDataService = new LunaticJsonRawDataService(lunaticJsonRawDataPersistanceStub);

    @ParameterizedTest
    @ValueSource(strings = {"{\"testdata\": \"ERROR", //Invalid JSON syntax
            "{\"NOTCOLLECTED\": \"test\"}", //Invalid root structure
            "{\"COLLECTED\": {\"TESTVAR\": {\"NOTADATASTATE\": [\"test\"]}}}" //Invalid datastate in collected variable
    })
    void saveDataTest_Invalid_syntaxes(String dataJson){
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";

        //WHEN + THEN
        Assertions.assertThatThrownBy(() -> lunaticJsonRawDataService.saveData(
                campaignId
                ,"TESTIDQUEST"
                ,"TESTinterrogationId"
                ,null
                , dataJson
                , Mode.WEB
        )).isInstanceOf(GenesisException.class);
    }

    @Test
    void saveDataTest_valid_only_collected_array() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).containsOnlyKeys("TESTVAR");

        Map<DataState, LunaticJsonRawDataVariable> lunaticJsonRawDataVariableMap =
                lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR")
                        .collectedVariableByStateMap();

        Assertions.assertThat(lunaticJsonRawDataVariableMap).isNotNull().hasSize(1).containsKey(DataState.COLLECTED);
        Assertions.assertThat(lunaticJsonRawDataVariableMap.get(DataState.COLLECTED).valuesArray()).isNotNull().containsExactly(
                "test");

        Assertions.assertThat(lunaticJsonRawDataVariableMap.get(DataState.COLLECTED).value()).isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().isEmpty();
    }

    @Test
    void saveDataTest_valid_only_collected_value() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": \"test\"}}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).containsOnlyKeys("TESTVAR");

        Map<DataState, LunaticJsonRawDataVariable> lunaticJsonRawDataVariableMap =
                lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR")
                        .collectedVariableByStateMap();

        Assertions.assertThat(lunaticJsonRawDataVariableMap).isNotNull().hasSize(1).containsKey(DataState.COLLECTED);
        Assertions.assertThat(lunaticJsonRawDataVariableMap.get(DataState.COLLECTED).value()).isNotNull().isEqualTo("test");

        Assertions.assertThat(lunaticJsonRawDataVariableMap.get(DataState.COLLECTED).valuesArray()).isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().isEmpty();
    }

    @Test
    void saveDataTest_valid_only_external_array() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"EXTERNAL\": {\"TESTVAR_EXT\": [\"test\"]}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).containsOnlyKeys("TESTVAR_EXT");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").valuesArray())
                .isNotNull().containsExactly("test");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").value())
                .isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().isEmpty();
    }

    @Test
    void saveDataTest_valid_only_external_value() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test\"}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).containsOnlyKeys("TESTVAR_EXT");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").value())
                .isNotNull().isEqualTo("test");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").valuesArray())
                .isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().isEmpty();
    }

    @Test
    void saveDataTest_valid_both_only_collected_datastate() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test_ext\"}, " +
                        "\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"]}}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).containsOnlyKeys("TESTVAR_EXT");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").value())
                .isNotNull().isEqualTo("test_ext");
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").valuesArray())
                .isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).containsOnlyKeys("TESTVAR");
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap())
                .containsOnlyKeys(DataState.COLLECTED);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.COLLECTED).valuesArray())
                .isNotNull().containsExactly("test");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.COLLECTED).value())
                .isNull();
    }

    @Test
    void saveDataTest_valid_both_multiple_datastate() throws GenesisException {
        //GIVEN
        lunaticJsonRawDataPersistanceStub.getMongoStub().clear();
        String campaignId = "SAMPLETEST-PARADATA-v1";
        String questionnaireId = "TESTIDQUEST";
        String interrogationId = "TESTinterrogationId";

        //WHEN
        lunaticJsonRawDataService.saveData(
                campaignId
                , questionnaireId
                , interrogationId
                , null
                ,"{\"EXTERNAL\": {\"TESTVAR_EXT\": \"test_ext\"}, " +
                        "\"COLLECTED\": {\"TESTVAR\": {\"COLLECTED\": [\"test\"], \"EDITED\": [\"test_ed\"]}}}"
                , Mode.WEB
        );

        //THEN
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().campaignId()).isEqualTo(campaignId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().questionnaireId()).isEqualTo(questionnaireId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().interrogationId()).isEqualTo(interrogationId);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data()).isNotNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables()).containsOnlyKeys("TESTVAR_EXT");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").value())
                .isNotNull().isEqualTo("test_ext");
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().externalVariables().get("TESTVAR_EXT").valuesArray())
                .isNull();

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).isNotNull().hasSize(1);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables()).containsOnlyKeys("TESTVAR");
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap())
                .containsOnlyKeys(DataState.COLLECTED, DataState.EDITED);
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.COLLECTED).valuesArray())
                .isNotNull().containsExactly("test");
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.EDITED).valuesArray())
                .isNotNull().containsExactly("test_ed");

        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.COLLECTED).value())
                .isNull();
        Assertions.assertThat(lunaticJsonRawDataPersistanceStub.getMongoStub().getFirst().data().collectedVariables().get("TESTVAR").collectedVariableByStateMap()
                        .get(DataState.EDITED).value())
                .isNull();
    }

    @Test
    void getDataStates_test_collected_only() {
        //GIVEN
        List<LunaticJsonRawDataModel> rawDataList = new ArrayList<>();

        LunaticJsonRawData lunaticJsonRawData = LunaticJsonRawData.builder()
                .collectedVariables(new HashMap<>())
                .externalVariables(new HashMap<>())
                .build();

        LunaticJsonRawDataCollectedVariable lunaticJsonRawDataCollectedVariable = LunaticJsonRawDataCollectedVariable.builder()
                .collectedVariableByStateMap(new EnumMap<>(DataState.class))
                .build();

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .valuesArray(Collections.singletonList("test"))
                .build();

        lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().put(DataState.COLLECTED,
                lunaticJsonRawDataVariable);
        lunaticJsonRawData.collectedVariables().put("TESTVAR", lunaticJsonRawDataCollectedVariable);


        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .interrogationId("UE1")
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .data(lunaticJsonRawData)
                .build();
        rawDataList.add(lunaticJsonRawDataModel);

        //WHEN
        Set<DataState> dataStates = lunaticJsonRawDataService.getRawDataStates(rawDataList);
        //THEN
        Assertions.assertThat(dataStates).containsOnly(DataState.COLLECTED);
    }

    @Test
    void getDataStates_test_collected_2_states() {
        //GIVEN
        List<LunaticJsonRawDataModel> rawDataList = new ArrayList<>();

        LunaticJsonRawData lunaticJsonRawData = LunaticJsonRawData.builder()
                .collectedVariables(new HashMap<>())
                .externalVariables(new HashMap<>())
                .build();

        LunaticJsonRawDataCollectedVariable lunaticJsonRawDataCollectedVariable = LunaticJsonRawDataCollectedVariable.builder()
                .collectedVariableByStateMap(new EnumMap<>(DataState.class))
                .build();

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .valuesArray(Collections.singletonList("test"))
                .build();
        lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().put(DataState.COLLECTED,
                lunaticJsonRawDataVariable);

        lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .valuesArray(Collections.singletonList("test"))
                .build();
        lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().put(DataState.EDITED,
                lunaticJsonRawDataVariable);

        lunaticJsonRawData.collectedVariables().put("TESTVAR", lunaticJsonRawDataCollectedVariable);


        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .interrogationId("UE1")
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .data(lunaticJsonRawData)
                .build();
        rawDataList.add(lunaticJsonRawDataModel);

        //WHEN
        Set<DataState> dataStates = lunaticJsonRawDataService.getRawDataStates(rawDataList);
        //THEN
        Assertions.assertThat(dataStates).containsOnly(DataState.COLLECTED, DataState.EDITED);
    }


    @Test
    void getDataStates_test_external_only(){
        //GIVEN
        List<LunaticJsonRawDataModel> rawDataList = new ArrayList<>();

        LunaticJsonRawData lunaticJsonRawData = LunaticJsonRawData.builder()
                .collectedVariables(new HashMap<>())
                .externalVariables(new HashMap<>())
                .build();

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .valuesArray(Collections.singletonList("test"))
                .build();

        lunaticJsonRawData.externalVariables().put("TESTVAR", lunaticJsonRawDataVariable);

        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .interrogationId("UE1")
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .data(lunaticJsonRawData)
                .build();
        rawDataList.add(lunaticJsonRawDataModel);

        //WHEN
        Set<DataState> dataStates = lunaticJsonRawDataService.getRawDataStates(rawDataList);
        //THEN
        Assertions.assertThat(dataStates).containsOnly(DataState.COLLECTED);
    }

    @Test
    void getDataStates_test_both(){
        //GIVEN
        List<LunaticJsonRawDataModel> rawDataList = new ArrayList<>();

        LunaticJsonRawData lunaticJsonRawData = LunaticJsonRawData.builder()
                .collectedVariables(new HashMap<>())
                .externalVariables(new HashMap<>())
                .build();

        LunaticJsonRawDataCollectedVariable lunaticJsonRawDataCollectedVariable = LunaticJsonRawDataCollectedVariable.builder()
                .collectedVariableByStateMap(new EnumMap<>(DataState.class))
                .build();

        LunaticJsonRawDataVariable lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .value("test")
                .build();
        lunaticJsonRawDataCollectedVariable.collectedVariableByStateMap().put(DataState.EDITED,
                lunaticJsonRawDataVariable);

        lunaticJsonRawData.collectedVariables().put("TESTVAR", lunaticJsonRawDataCollectedVariable);

        lunaticJsonRawDataVariable = LunaticJsonRawDataVariable.builder()
                .value("test_ext")
                .build();
        lunaticJsonRawData.externalVariables().put("TESTVAR_EXT", lunaticJsonRawDataVariable);


        LunaticJsonRawDataModel lunaticJsonRawDataModel = LunaticJsonRawDataModel.builder()
                .campaignId("TESTCAMPAIGN")
                .questionnaireId("TESTQUEST")
                .interrogationId("UE1")
                .mode(Mode.WEB)
                .recordDate(LocalDateTime.now())
                .data(lunaticJsonRawData)
                .build();
        rawDataList.add(lunaticJsonRawDataModel);

        //WHEN
        Set<DataState> dataStates = lunaticJsonRawDataService.getRawDataStates(rawDataList);
        //THEN
        Assertions.assertThat(dataStates).containsOnly(DataState.COLLECTED, DataState.EDITED);
    }
}