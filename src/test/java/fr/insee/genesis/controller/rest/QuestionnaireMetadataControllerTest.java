package fr.insee.genesis.controller.rest;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariableType;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class QuestionnaireMetadataControllerTest {
    public static final String VARIABLE_NAME = "TESTVAR";
    static QuestionnaireMetadataPersistancePortStub questionnaireMetadataPersistancePortStub
            = new QuestionnaireMetadataPersistancePortStub();


    static QuestionnaireMetadataController questionnaireMetadataController = new QuestionnaireMetadataController(
            new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub)
    );

    @BeforeEach
    void clean(){
        questionnaireMetadataPersistancePortStub.getMongoStub().clear();
    }

    @Test
    void getMetadataTest(){
        //GIVEN
        String questionnaireId = "TESTQUEST";
        Mode mode = Mode.WEB;
        String variableName = VARIABLE_NAME;
        questionnaireMetadataPersistancePortStub.getMongoStub().add(
                new QuestionnaireMetadataDocument(
                        questionnaireId,
                        mode,
                        getMetadataModel(variableName)
                )
        );
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(1);

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.getMetadata(questionnaireId, mode);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat((MetadataModel) response.getBody()).isNotNull();
        Assertions.assertThat(((MetadataModel) response.getBody()).getVariables().hasVariable(variableName)
        ).isTrue();
        Assertions.assertThat(((MetadataModel) response.getBody()).getVariables().getVariable(variableName)
                .getGroup()).isEqualTo(((MetadataModel) response.getBody()).getRootGroup());
        Assertions.assertThat(((MetadataModel) response.getBody()).getVariables().getVariable(variableName)
                .getType()).isEqualTo(VariableType.STRING);
    }

    @Test
    void getMetadataTest_not_found(){
        //GIVEN
        String questionnaireId = "ERRORTESTQUEST";
        Mode mode = Mode.WEB;

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.getMetadata(questionnaireId, mode);

        //THEN
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void saveMetadataTest(){
        //GIVEN
        String questionnaireId = "TESTQUEST";
        Mode mode = Mode.WEB;
        String variableName = VARIABLE_NAME;
        MetadataModel metadataModel = getMetadataModel(variableName);

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.saveMetadata(questionnaireId, mode, metadataModel);


        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().questionnaireId())
                .isEqualTo(questionnaireId);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().mode())
                .isEqualTo(mode);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                        .hasVariable(variableName)).isTrue();
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                .getVariable(variableName).getGroup()).isEqualTo(metadataModel.getRootGroup());
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                .getVariable(variableName).getType()).isEqualTo(VariableType.STRING);
    }

    @Test
    void saveMetadataTest_overwrite(){
        //GIVEN
        String questionnaireId = "TESTQUEST";
        Mode mode = Mode.WEB;
        String variableName = VARIABLE_NAME;
        MetadataModel metadataModel = getMetadataModel(variableName);
        questionnaireMetadataPersistancePortStub.getMongoStub().add(
                new QuestionnaireMetadataDocument(
                        questionnaireId,
                        mode,
                        metadataModel
                )
        );
        metadataModel = new MetadataModel();
        variableName = "TESTVAR2";
        metadataModel.getVariables().putVariable(
                new Variable(
                        variableName,
                        metadataModel.getRootGroup(),
                        VariableType.INTEGER
                )
        );

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.saveMetadata(questionnaireId, mode, metadataModel);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(1);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().questionnaireId())
                .isEqualTo(questionnaireId);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().mode())
                .isEqualTo(mode);
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                .hasVariable(variableName)).isTrue();
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                .getVariable(variableName).getGroup()).isEqualTo(metadataModel.getRootGroup());
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub().getFirst().metadataModel().getVariables()
                .getVariable(variableName).getType()).isEqualTo(VariableType.INTEGER);
    }

    @Test
    void deleteMetadataTest(){
        //GIVEN
        String questionnaireId = "TESTQUEST";
        Mode mode = Mode.WEB;
        questionnaireMetadataPersistancePortStub.getMongoStub().add(
                new QuestionnaireMetadataDocument(
                        questionnaireId,
                        mode,
                        new MetadataModel()
                )
        );
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(1);

        //WHEN
        questionnaireMetadataController.deleteMetadata(questionnaireId, mode);

        //THEN
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).isEmpty();
    }

    private static MetadataModel getMetadataModel(String variableName) {
        MetadataModel metadataModel = new MetadataModel();
        metadataModel.getVariables().putVariable(
                new Variable(
                        variableName,
                        metadataModel.getRootGroup(),
                        VariableType.STRING
                )
        );
        return metadataModel;
    }

}