package integration_tests;


import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.adapter.LunaticXmlAdapter;
import fr.insee.genesis.controller.sources.xml.*;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.model.surveyunit.SurveyUnitModel;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

class LunaticXmlAdapterTest {

    private static final String LOOP_NAME = "BOUCLE1";
    private static final String CAMPAIGN_ID = "CAMPAIGN_ID";
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit1 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit2 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit3 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit4 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit5 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit6 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit7 = new LunaticXmlSurveyUnit();
    MetadataModel metadataModel = new MetadataModel();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit8 = new LunaticXmlSurveyUnit();
    LunaticXmlSurveyUnit lunaticXmlSurveyUnit9 = new LunaticXmlSurveyUnit();

    @BeforeEach
    void setUp() {
        //Given
        //SurveyUnit 1 : Only collected data
        LunaticXmlData lunaticXmlData = new LunaticXmlData();
        LunaticXmlCollectedData lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        LunaticXmlCollectedData lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        List<LunaticXmlCollectedData> collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);
        List<LunaticXmlOtherData> external = List.of();
        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit1.setId("interrogationId1");
        lunaticXmlSurveyUnit1.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit1.setData(lunaticXmlData);

        //SurveyUnit 2 : COLLECTED + EDITED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit2.setId("interrogationId1");
        lunaticXmlSurveyUnit2.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit2.setData(lunaticXmlData);

        //SurveyUnit 3 : COLLECTED + EDITED + FORCED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setForced(List.of(new ValueType("3f", "string"), new ValueType("4f", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit3.setId("interrogationId1");
        lunaticXmlSurveyUnit3.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit3.setData(lunaticXmlData);

        //SurveyUnit 4 : COLLECTED + EDITED + PREVIOUS
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p", "string"), new ValueType("4p", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit4.setId("interrogationId1");
        lunaticXmlSurveyUnit4.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit4.setData(lunaticXmlData);

        //SurveyUnit 5 : COLLECTED + EDITED + PREVIOUS + INPUTED
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData.setEdited(List.of(new ValueType("1e", "string"), new ValueType("2e", "string")));
        lunaticXmlCollectedData.setInputed(List.of(new ValueType("1i", "string"), new ValueType("2i", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("3", "string"), new ValueType("4", "string")));
        lunaticXmlCollectedData2.setPrevious(List.of(new ValueType("3p", "string"), new ValueType("4p", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit5.setId("interrogationId1");
        lunaticXmlSurveyUnit5.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit5.setData(lunaticXmlData);

        //SurveyUnit 6 : COLLECTED only, has one unknown variable
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var3");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));


        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit6.setId("interrogationId1");
        lunaticXmlSurveyUnit6.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit6.setData(lunaticXmlData);

        //SurveyUnit 7 : COLLECTED only, has two unknown variables with known variable prefix or suffix
        lunaticXmlData = new LunaticXmlData();

        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var1_MISSING");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("1", "string"), new ValueType("2", "string")));

        LunaticXmlCollectedData lunaticXmlCollectedData3 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData3.setVariableName("FILTER_RESULT_var1");
        lunaticXmlCollectedData3.setCollected(List.of(new ValueType("1", "string"), new ValueType("1", "string")));

        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2, lunaticXmlCollectedData3);
        lunaticXmlData.setCollected(collected);

        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit7.setId("interrogationId1");
        lunaticXmlSurveyUnit7.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit7.setData(lunaticXmlData);

        //SurveyUnit 8 : Only collected data
        lunaticXmlData = new LunaticXmlData();
        lunaticXmlCollectedData = new LunaticXmlCollectedData();
        lunaticXmlCollectedData.setVariableName("var1");
        lunaticXmlCollectedData.setCollected(List.of(new ValueType(null,"null"),new ValueType("1", "string"), new ValueType("2", "string")));
        lunaticXmlCollectedData2 = new LunaticXmlCollectedData();
        lunaticXmlCollectedData2.setVariableName("var2");
        lunaticXmlCollectedData2.setCollected(List.of(new ValueType("4", "string")));
        collected = List.of(lunaticXmlCollectedData, lunaticXmlCollectedData2);
        lunaticXmlData.setCollected(collected);
        external = List.of();
        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit8.setId("interrogationId1");
        lunaticXmlSurveyUnit8.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit8.setData(lunaticXmlData);

        //SurveyUnit 9 : External data only
        lunaticXmlData = new LunaticXmlData();
        collected = List.of();
        lunaticXmlData.setCollected(collected);
        LunaticXmlOtherData lunaticXmlOtherData = new LunaticXmlOtherData();
        lunaticXmlOtherData.setVariableName("extvar1");
        lunaticXmlOtherData.setValues(List.of(new ValueType("ext", "string")));
        external = List.of(lunaticXmlOtherData);
        lunaticXmlData.setExternal(external);
        lunaticXmlSurveyUnit9.setId("interrogationId1");
        lunaticXmlSurveyUnit9.setQuestionnaireModelId("questionnaireId1");
        lunaticXmlSurveyUnit9.setData(lunaticXmlData);

        //VariablesMap
        Group group = new Group(LOOP_NAME, Constants.ROOT_GROUP_NAME);
        Variable var1 = new Variable("var1", group, VariableType.STRING, "1");
        Variable var2 = new Variable("var2", metadataModel.getRootGroup(), VariableType.STRING, "1");
        Variable extvar1 = new Variable("extvar1", group, VariableType.STRING, "1");
        metadataModel.getVariables().putVariable(var1);
        metadataModel.getVariables().putVariable(var2);
        metadataModel.getVariables().putVariable(extvar1);
    }

    @Test
    @DisplayName("SurveyUnitModel should not be null")
    void test01() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).isNotNull().isNotEmpty();
    }

    @Test
    @DisplayName("SurveyUnitModel should have the right questionnaireId")
    void test02() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectionInstrumentId()).isEqualTo("questionnaireId1".toUpperCase());
    }

    @Test
    @DisplayName("SurveyUnitModel should have the right id")
    void test03() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels.getFirst().getInterrogationId()).isEqualTo("interrogationId1");
    }

    @Test
    @DisplayName("SurveyUnitModel should contains 4 variable state updates")
    void test04() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit1, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).hasSize(4);
    }

    @Test
    @DisplayName("There should be a EDITED survey unit model with EDITED data")
    void test05() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit2, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(2);
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.EDITED)
        ).isNotEmpty();

        Optional<SurveyUnitModel> editedSurveyUnitModel = surveyUnitModels.stream().filter(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.EDITED)
        ).findFirst();
        Assertions.assertThat(editedSurveyUnitModel).isPresent();

        //Content check
        for (VariableModel collectedVariable : editedSurveyUnitModel.get().getCollectedVariables()) {
            Assertions.assertThat(collectedVariable.value()).containsAnyOf("1e","2e").isNotEqualTo("1").isNotEqualTo(
                    "2");
        }
    }

    @Test
    @DisplayName("There should be both EDITED survey unit model and FORCED survey unit model if there is EDITED and FORCED data")
    void test06() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit3, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(3);
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.FORCED)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("There should be a EDITED model and PREVIOUS model if there is EDITED and PREVIOUS data")
    void test07() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit4, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(3);
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.PREVIOUS)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("There should be multiple survey unit models if there is different data states (all 4)")
    void test08() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit5, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(4);
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.EDITED)
        ).isNotEmpty();
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.PREVIOUS)
        ).isNotEmpty();
        Assertions.assertThat(surveyUnitModels).filteredOn(surveyUnitModel ->
                surveyUnitModel.getState().equals(DataState.INPUTED)
        ).isNotEmpty();
    }

    @Test
    @DisplayName("If a variable not present in DDI then he is in the root group")
    void test09() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit6, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);

        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(1);

        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).filteredOn(collectedVariableModel ->
                collectedVariableModel.varId().equals("var3")).isNotEmpty();
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("var3")).toList().getFirst().parentId()).isNull();
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("var3")).toList().getFirst().scope()).isEqualTo(Constants.ROOT_GROUP_NAME);
    }

    @Test
    @DisplayName("If a variable A not present in DDI and is the extension of a known variable B, then the variable A is in the same group")
    void test10() {
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit7, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);

        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).filteredOn(collectedVariableModel ->
                collectedVariableModel.varId().equals("var1_MISSING")).hasSize(2);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("var1_MISSING")).toList().getFirst().parentId()).isNotNull().isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("var1_MISSING")).toList().getFirst().scope()).isNotEqualTo(Constants.ROOT_GROUP_NAME).isEqualTo(LOOP_NAME);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("FILTER_RESULT_var1")).toList().getFirst().parentId()).isNotNull().isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.varId().equals("FILTER_RESULT_var1")).toList().getFirst().scope()).isNotEqualTo(Constants.ROOT_GROUP_NAME).isEqualTo(LOOP_NAME);
    }

    @Test
    @DisplayName("Value should be affected in the good loop iteration")
    void test11(){
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit8, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).hasSize(3);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).filteredOn(collectedVariableModel ->
                collectedVariableModel.varId().equals("var1")).isNotEmpty();
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).filteredOn(collectedVariableModel ->
                collectedVariableModel.scope().equals("BOUCLE1")
                && collectedVariableModel.iteration().equals(1)
        ).isEmpty();
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.scope().equals("BOUCLE1")
                && collectedVariableModel.iteration().equals(2)
        ).toList().getFirst().value()).isEqualTo("1");
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables().stream().filter(collectedVariableModel ->
                collectedVariableModel.scope().equals("BOUCLE1")
                && collectedVariableModel.iteration().equals(3)
        ).toList().getFirst().value()).isEqualTo("2");
    }

    @Test
    @DisplayName("External value should be affected in the good loop iteration")
    void test12(){
        // When
        List<SurveyUnitModel> surveyUnitModels = LunaticXmlAdapter.convert(lunaticXmlSurveyUnit9, metadataModel.getVariables(), CAMPAIGN_ID, Mode.WEB);
        // Then
        Assertions.assertThat(surveyUnitModels).hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getCollectedVariables()).isEmpty();
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables()).hasSize(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().varId()).isEqualTo("extvar1");
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().value()).isEqualTo("ext");
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().scope()).isEqualTo(LOOP_NAME);
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().iteration()).isEqualTo(1);
        Assertions.assertThat(surveyUnitModels.getFirst().getExternalVariables().getFirst().parentId()).isEqualTo(Constants.ROOT_GROUP_NAME);
    }

}
