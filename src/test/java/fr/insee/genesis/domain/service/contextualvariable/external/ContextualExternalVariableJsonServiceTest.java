package fr.insee.genesis.domain.service.contextualvariable.external;

import com.fasterxml.jackson.core.JsonParseException;
import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualExternalVariablePersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class ContextualExternalVariableJsonServiceTest {
    ContextualExternalVariablePersistancePort contextualExternalVariablePersistancePort;
    ContextualExternalVariableJsonService contextualExternalVariableJsonService;

    @BeforeEach
    void setUp() {
        contextualExternalVariablePersistancePort = mock(ContextualExternalVariablePersistancePort.class);
        contextualExternalVariableJsonService = new ContextualExternalVariableJsonService(
            contextualExternalVariablePersistancePort
        );
    }

    @Test
    @SneakyThrows
    void readContextualExternalFile_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
            TestConstants.TEST_RESOURCES_DIRECTORY,
            "contextual_external",
            "ok.json"
        ).toString();

        //WHEN
        boolean isOK = contextualExternalVariableJsonService.readContextualExternalFile(
          collectionInstrumentId,
          filePath
        );

        //THEN
        Assertions.assertThat(isOK).isTrue();
        verify(contextualExternalVariablePersistancePort, times(1)).backup(collectionInstrumentId);
        verify(contextualExternalVariablePersistancePort, times(1)).saveAll(anyList());
        verify(contextualExternalVariablePersistancePort, times(1)).deleteBackup(collectionInstrumentId);
        verify(contextualExternalVariablePersistancePort, times(1)).delete(collectionInstrumentId);

    }

    @Test
    @SneakyThrows
    void readContextualExternalFile_no_external_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_previous",
                "ok.json"
        ).toString();

        //WHEN
        boolean isOK = contextualExternalVariableJsonService.readContextualExternalFile(
                collectionInstrumentId,
                filePath
        );

        //THEN
        Assertions.assertThat(isOK).isFalse();
        verify(contextualExternalVariablePersistancePort, times(1)).backup(collectionInstrumentId);
        verify(contextualExternalVariablePersistancePort, times(1)).delete(collectionInstrumentId);
        verify(contextualExternalVariablePersistancePort, never()).saveAll(anyList());
    }

    @Test
    @SneakyThrows
    void readContextualExternalFile_jsonParseException_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_external",
                "invalid_syntax.json"
        ).toString();

        //WHEN + THEN
        try{
            contextualExternalVariableJsonService.readContextualExternalFile(
                    collectionInstrumentId,
                    filePath
            );
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(400);
            verify(contextualExternalVariablePersistancePort, times(1)).backup(collectionInstrumentId);
            verify(contextualExternalVariablePersistancePort, times(1)).delete(collectionInstrumentId);
            verify(contextualExternalVariablePersistancePort, times(1)).restoreBackup(collectionInstrumentId);
            verify(contextualExternalVariablePersistancePort, never()).saveAll(anyList());
        }
    }

    @Test
    @SneakyThrows
    void readContextualExternalFile_IOException_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_external",
                "hgfhfghgfhfg"
        ).toString();

        //WHEN + THEN
        try{
            contextualExternalVariableJsonService.readContextualExternalFile(
                    collectionInstrumentId,
                    filePath
            );
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(500);
            verify(contextualExternalVariablePersistancePort, never()).saveAll(anyList());
            verify(contextualExternalVariablePersistancePort, times(1)).restoreBackup(collectionInstrumentId);
        }
    }

    @Test
    void findByCollectionInstrumentIdAndInterrogationId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test2";
        ContextualExternalVariableModel expected = ContextualExternalVariableModel.builder().build();
        doReturn(expected).when(contextualExternalVariablePersistancePort)
                .findByCollectionInstrumentIdAndInterrogationId(any(), any());

        //WHEN
        ContextualExternalVariableModel actual = contextualExternalVariableJsonService.findByCollectionInstrumentIdAndInterrogationId(
            collectionInstrumentId,
            interrogationId
        );

        //THEN
        verify(contextualExternalVariablePersistancePort, times(1))
                .findByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}