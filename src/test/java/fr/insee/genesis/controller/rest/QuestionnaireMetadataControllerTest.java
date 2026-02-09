package fr.insee.genesis.controller.rest;

import fr.insee.bpm.metadata.model.MetadataModel;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.QuestionnaireMetadataApiPort;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class QuestionnaireMetadataControllerTest {
    QuestionnaireMetadataApiPort questionnaireMetadataApiPort;
    QuestionnaireMetadataController questionnaireMetadataController;

    @BeforeEach
    void setUp() {
        questionnaireMetadataApiPort = mock(QuestionnaireMetadataApiPort.class);
        questionnaireMetadataController = new QuestionnaireMetadataController(
                questionnaireMetadataApiPort
        );
    }

    @Test
    @SneakyThrows
    void getMetadata_test() {
        //GIVEN
        String questionnaireId = "questionnaireId";
        Mode mode = Mode.WEB;
        MetadataModel metadataModel = new MetadataModel();
        doReturn(metadataModel).when(questionnaireMetadataApiPort).find(any(), any());

        //WHEN
        ResponseEntity<Object> response = questionnaireMetadataController.getMetadata(questionnaireId, mode);

        //THEN
        verify(questionnaireMetadataApiPort, times(1)).find(
                questionnaireId,
                mode
        );
        Assertions.assertThat(response.getBody()).isEqualTo(metadataModel);
    }

    @Test
    void saveMetadata_test() {
        //GIVEN
        String questionnaireId = "questionnaireId";
        Mode mode = Mode.WEB;
        MetadataModel metadataModel = new MetadataModel();

        //WHEN
        questionnaireMetadataController.saveMetadata(questionnaireId, mode, metadataModel);

        //THEN
        verify(questionnaireMetadataApiPort, times(1)).save(
                questionnaireId, mode, metadataModel
        );
    }

    @Test
    void deleteMetadata_test() {
        //GIVEN
        String questionnaireId = "questionnaireId";
        Mode mode = Mode.WEB;

        //WHEN
        questionnaireMetadataController.deleteMetadata(questionnaireId, mode);

        //THEN
        verify(questionnaireMetadataApiPort, times(1)).remove(
                questionnaireId,
                mode
        );
    }
}