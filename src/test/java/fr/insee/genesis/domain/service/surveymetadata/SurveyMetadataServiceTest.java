package fr.insee.genesis.domain.service.surveymetadata;

import fr.insee.bpm.metadata.model.Group;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.bpm.metadata.model.VariablesMap;
import fr.insee.genesis.Constants;
import fr.insee.genesis.domain.model.surveymetadata.SurveyMetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.infrastructure.document.surveymetadata.SurveyMetadataDocument;
import fr.insee.genesis.infrastructure.document.surveymetadata.VariableDocument;
import fr.insee.genesis.infrastructure.mappers.SurveyMetadataDocumentMapper;
import fr.insee.genesis.infrastructure.mappers.VariableDocumentMapper;
import fr.insee.genesis.stubs.SurveyMetadataPersistanceStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.Map;

class SurveyMetadataServiceTest {
    SurveyMetadataPersistanceStub surveyMetadataPersistanceStub = new SurveyMetadataPersistanceStub();
    SurveyMetadataService surveyMetadataService = new SurveyMetadataService(surveyMetadataPersistanceStub);

    private static final String CAMPAIGN_ID = "TESTCAMPAIGN";
    private static final String QUESTIONNAIRE_ID = "TESTCAMPAIGN_QUEST";
    private static final String VAR_NAME = "testvar";
    private static final String GROUP_NAME = "group1";
    private static final String QUESTION_NAME = "QUESTIONNAME";
    private static final Mode MODE = Mode.WEB;

    @Test
    void saveMetadatas() {
        surveyMetadataPersistanceStub.getMongoStub().clear();

        //When
        surveyMetadataService.saveMetadatas(
                CAMPAIGN_ID,
                QUESTIONNAIRE_ID,
                MODE,
                getTestVariablesMap()
        );

        //Then
        Assertions.assertThat(surveyMetadataPersistanceStub.getMongoStub()).isNotEmpty().hasSize(1);
        checkTestDocument(
                surveyMetadataPersistanceStub.getMongoStub().getFirst()
        );
    }

    @Test
    void getMetadatas() {
        //Given
        surveyMetadataPersistanceStub.getMongoStub().clear();
        surveyMetadataPersistanceStub.getMongoStub().add(
                getTestSurveyMetadataDocument()
        );
        
        //When
        SurveyMetadataModel surveyMetadataModel = surveyMetadataService.getMetadatas(CAMPAIGN_ID, QUESTIONNAIRE_ID, MODE);
        //Then
        checkTestModel(surveyMetadataModel);
    }

    private SurveyMetadataDocument getTestSurveyMetadataDocument() {
        return new SurveyMetadataDocument(
                CAMPAIGN_ID,
                QUESTIONNAIRE_ID,
                MODE,
                getTestVariablesDefinitions()
        );
    }

    @Test
    void getVariableMapFromMetadatas_test() {
        //Given
        surveyMetadataPersistanceStub.getMongoStub().clear();
        surveyMetadataPersistanceStub.getMongoStub().add(
                new SurveyMetadataDocument(
                        CAMPAIGN_ID,
                        QUESTIONNAIRE_ID,
                        MODE,
                        getTestVariablesDefinitions()
                )
        );



        //When
        VariablesMap variablesMap = surveyMetadataService.getVariableMapFromMetadatas(CAMPAIGN_ID, QUESTIONNAIRE_ID, MODE);

        //Then
        Assertions.assertThat(variablesMap).isNotNull();
        Assertions.assertThat(variablesMap.getVariables()).isNotNull().containsKey(VAR_NAME);
        checkTestVariable(variablesMap.getVariables().get(VAR_NAME));
    }

    //Utils
    private static Variable getTestVariable() {
        Variable variable = new Variable(
                SurveyMetadataServiceTest.VAR_NAME,
                new Group(SurveyMetadataServiceTest.GROUP_NAME, Constants.ROOT_GROUP_NAME),
                VariableType.STRING,
                "SASFORMAT"
        );
        variable.setMaxLengthData(500);
        variable.setQuestionName(QUESTION_NAME);
        variable.setInQuestionGrid(false);
        return variable;
    }

    private void checkTestVariable(Variable variable) {
        Assertions.assertThat(variable.getName()).isEqualTo(VAR_NAME);
        Assertions.assertThat(variable.getGroup().getName()).isEqualTo(GROUP_NAME);
        Assertions.assertThat(variable.getGroup().getParentName()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(variable.getType()).isEqualTo(VariableType.STRING);
        Assertions.assertThat(variable.getQuestionName()).isEqualTo(QUESTION_NAME);
        Assertions.assertThat(variable.getMaxLengthData()).isEqualTo(500);
    }


    private VariablesMap getTestVariablesMap() {
        VariablesMap variablesMap = new VariablesMap();
        variablesMap.putVariable(
                getTestVariable(
                )
        );
        return variablesMap;
    }

    private Map<String, VariableDocument> getTestVariablesDefinitions() {
        Map<String, VariableDocument> variableDefinitions = new LinkedHashMap<>();

        variableDefinitions.put(VAR_NAME, VariableDocumentMapper.INSTANCE.bpmToDocument(getTestVariable(
        )));

        return variableDefinitions;
    }

    private void checkTestDocument(SurveyMetadataDocument document) {
        Assertions.assertThat(document.campaignId()).isEqualTo(SurveyMetadataServiceTest.CAMPAIGN_ID);
        Assertions.assertThat(document.questionnaireId()).isEqualTo(SurveyMetadataServiceTest.QUESTIONNAIRE_ID);
        Assertions.assertThat(document.mode()).isEqualTo(MODE);
        Assertions.assertThat(document.variableDefinitions()).isNotNull().isNotEmpty().containsKey(SurveyMetadataServiceTest.VAR_NAME);
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).name()).isEqualTo(SurveyMetadataServiceTest.VAR_NAME);
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).group()).isNotNull();
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).group().getName()).isEqualTo(SurveyMetadataServiceTest.GROUP_NAME);
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).group().getParentName()).isEqualTo(Constants.ROOT_GROUP_NAME);
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).type()).isEqualTo(VariableType.STRING);
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).questionName()).isEqualTo("QUESTIONNAME");
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).sasFormat()).isEqualTo("SASFORMAT");
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).isInQuestionGrid()).isFalse();
        Assertions.assertThat(document.variableDefinitions().get(SurveyMetadataServiceTest.VAR_NAME).maxLengthData()).isEqualTo(500);
    }

    private void checkTestModel(SurveyMetadataModel model) {
        checkTestDocument(
                SurveyMetadataDocumentMapper.INSTANCE.modelToDocument(model)
        );
    }
}