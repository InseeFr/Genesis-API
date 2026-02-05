package fr.insee.genesis.controller.rest.responses;

import fr.insee.genesis.Constants;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualVariableApiPort;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ContextualVariableControllerUnitTest {

    private ContextualVariableController contextualVariableController;
    private ContextualPreviousVariableApiPort contextualPreviousVariableApiPort;
    private ContextualExternalVariableApiPort contextualExternalVariableApiPort;
    private ContextualVariableApiPort contextualVariableApiPort;

    @BeforeEach
    void setUp() {
        contextualPreviousVariableApiPort = mock(ContextualPreviousVariableApiPort.class);
        contextualExternalVariableApiPort = mock(ContextualExternalVariableApiPort.class);
        contextualVariableApiPort = mock(ContextualVariableApiPort.class);


        contextualVariableController = new ContextualVariableController(
            contextualPreviousVariableApiPort,
            contextualExternalVariableApiPort,
            contextualVariableApiPort,
            TestConstants.getConfigStub()
        );
    }

    @Test
    void getContextualVariables() {
        //WHEN
        contextualVariableController.getContextualVariables(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                TestConstants.DEFAULT_INTERROGATION_ID
        );

        //THEN
        verify(contextualVariableApiPort, times(1)).getContextualVariable(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                TestConstants.DEFAULT_INTERROGATION_ID
        );
    }

    @Test
    @SneakyThrows
    void saveContextualVariables() {
        //WHEN
        contextualVariableController.saveContextualVariables(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID
        );

        //THEN
        verify(contextualVariableApiPort, times(1)).saveContextualVariableFiles(
                eq(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID),
                any(),
                any()
        );
    }

    @Test
    @SneakyThrows
    void readContextualPreviousJson() {
        //GIVEN
        FileUtils fileUtils = new FileUtils(TestConstants.getConfigStub());
        String expectedFilePath = "%s%s/%s".formatted(
                fileUtils.getDataFolder(
                        TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                        Mode.WEB.getFolder(),
                        null
                ),
                Constants.CONTEXTUAL_FOLDER,
                "ok.json"
        );

        //WHEN
        contextualVariableController.readContextualPreviousJson(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Mode.WEB,
                null,
                "ok.json"
        );

        //THEN
        verify(contextualPreviousVariableApiPort, times(1)).readContextualPreviousFile(
                eq(TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID),
                any(),
                eq(expectedFilePath)
        );
    }

    @Test
    void readContextualPreviousJson_notJson() {
        //WHEN
        ResponseEntity<Object> response = contextualVariableController.readContextualPreviousJson(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Mode.WEB,
                null,
                "ok.xml"
        );

        //THEN
        verifyNoInteractions(contextualPreviousVariableApiPort);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }

    @Test
    @SneakyThrows
    void readContextualExternalJson() {
        //GIVEN
        FileUtils fileUtils = new FileUtils(TestConstants.getConfigStub());
        String expectedFilePath = "%s%s/%s".formatted(
                fileUtils.getDataFolder(
                        TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                        Mode.WEB.getFolder(),
                        null
                ),
                Constants.CONTEXTUAL_FOLDER,
                "ok.json"
        );

        //WHEN
        contextualVariableController.readContextualExternalJson(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Mode.WEB,
                "ok.json"
        );

        //THEN
        verify(contextualExternalVariableApiPort, times(1)).readContextualExternalFile(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                expectedFilePath
        );
    }

    @Test
    void readContextualExternalJson_notJson() {
        //WHEN
        ResponseEntity<Object> response = contextualVariableController.readContextualExternalJson(
                TestConstants.DEFAULT_COLLECTION_INSTRUMENT_ID,
                Mode.WEB,
                "ok.xml"
        );

        //THEN
        verifyNoInteractions(contextualExternalVariableApiPort);
        Assertions.assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}