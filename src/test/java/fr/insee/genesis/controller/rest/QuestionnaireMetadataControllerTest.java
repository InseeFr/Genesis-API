package fr.insee.genesis.controller.rest;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

class QuestionnaireMetadataControllerTest {
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
        questionnaireMetadataPersistancePortStub.getMongoStub().add(
                new QuestionnaireMetadataDocument(
                        questionnaireId,
                        mode,
                        new MetadataModel()
                )
        );
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(1);

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.getMetadata(questionnaireId, mode);

        //THEN
        Assertions.assertThat(response.getStatusCode().is2xxSuccessful()).isTrue();
        Assertions.assertThat((MetadataModel) response.getBody()).isNotNull();
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


}