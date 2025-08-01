package fr.insee.genesis.controller.rest;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.service.metadata.QuestionnaireMetadataService;
import fr.insee.genesis.infrastructure.document.metadata.QuestionnaireMetadataDocument;
import fr.insee.genesis.stubs.QuestionnaireMetadataPersistancePortStub;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

class QuestionnaireMetadataControllerTest {
    static QuestionnaireMetadataPersistancePortStub questionnaireMetadataPersistancePortStub
            = new QuestionnaireMetadataPersistancePortStub();


    static QuestionnaireMetadataController questionnaireMetadataController = new QuestionnaireMetadataController(
            new QuestionnaireMetadataService(questionnaireMetadataPersistancePortStub)
    );

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

        //Then
        Assertions.assertThat(questionnaireMetadataPersistancePortStub.getMongoStub()).hasSize(0);
    }


}