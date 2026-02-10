package fr.insee.genesis.domain.service.contextualvariable.previous;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.ports.spi.ContextualPreviousVariablePersistancePort;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class ContextualPreviousVariableJsonServiceTest {

    @Mock
    ContextualPreviousVariablePersistancePort contextualPreviousVariablePersistancePort;

    @InjectMocks
    ContextualPreviousVariableJsonService contextualPreviousVariableJsonService;

    @Test
    @SneakyThrows
    void readContextualPreviousFile_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_previous",
                "ok.json"
        ).toString();
        String sourceState = "test2";

        //WHEN
        boolean isOK = contextualPreviousVariableJsonService.readContextualPreviousFile(
                collectionInstrumentId,
                sourceState,
                filePath
        );

        //THEN
        Assertions.assertThat(isOK).isTrue();
        verify(contextualPreviousVariablePersistancePort, times(1)).backup(collectionInstrumentId);
        verify(contextualPreviousVariablePersistancePort, times(1)).saveAll(anyList());
        verify(contextualPreviousVariablePersistancePort, times(1)).deleteBackup(collectionInstrumentId);
        verify(contextualPreviousVariablePersistancePort, times(1)).delete(collectionInstrumentId);

    }

    @Test
    @SneakyThrows
    void readContextualPreviousFile_no_previous_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_external",
                "ok.json"
        ).toString();

        //WHEN
        boolean isOK = contextualPreviousVariableJsonService.readContextualPreviousFile(
                collectionInstrumentId,
                null,
                filePath
        );

        //THEN
        Assertions.assertThat(isOK).isFalse();
        verify(contextualPreviousVariablePersistancePort, times(1)).backup(collectionInstrumentId);
        verify(contextualPreviousVariablePersistancePort, times(1)).delete(collectionInstrumentId);
        verify(contextualPreviousVariablePersistancePort, never()).saveAll(anyList());
    }

    @Test
    @SneakyThrows
    void readContextualPreviousFile_sourcestate_too_long_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_external",
                "ok.json"
        ).toString();
        String sourceState = "AAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAA";

        //WHEN + THEN
        try{
            contextualPreviousVariableJsonService.readContextualPreviousFile(
                    collectionInstrumentId,
                    sourceState,
                    filePath
            );
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(400);
            verifyNoInteractions(contextualPreviousVariablePersistancePort);
        }
    }

    @Test
    @SneakyThrows
    void readContextualPreviousFile_jsonParseException_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_previous",
                "invalid_syntax.json"
        ).toString();

        //WHEN + THEN
        try{
            contextualPreviousVariableJsonService.readContextualPreviousFile(
                    collectionInstrumentId,
                    null,
                    filePath
            );
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(400);
            verify(contextualPreviousVariablePersistancePort, times(1)).backup(collectionInstrumentId);
            verify(contextualPreviousVariablePersistancePort, times(1)).delete(collectionInstrumentId);
            verify(contextualPreviousVariablePersistancePort, times(1)).restoreBackup(collectionInstrumentId);
            verify(contextualPreviousVariablePersistancePort, never()).saveAll(anyList());
        }
    }

    @Test
    @SneakyThrows
    void readContextualPreviousFile_IOException_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String filePath = Path.of(
                TestConstants.TEST_RESOURCES_DIRECTORY,
                "contextual_previous",
                "hgfhfghgfhfg"
        ).toString();

        //WHEN + THEN
        try{
            contextualPreviousVariableJsonService.readContextualPreviousFile(
                    collectionInstrumentId,
                    null,
                    filePath
            );
            Assertions.fail();
        }catch (GenesisException ge){
            Assertions.assertThat(ge.getStatus()).isEqualTo(500);
            verify(contextualPreviousVariablePersistancePort, never()).saveAll(anyList());
            verify(contextualPreviousVariablePersistancePort, times(1)).restoreBackup(collectionInstrumentId);
        }
    }

    @Test
    void findByCollectionInstrumentIdAndInterrogationId_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test2";
        ContextualPreviousVariableModel expected = ContextualPreviousVariableModel.builder().build();
        doReturn(expected).when(contextualPreviousVariablePersistancePort)
                .findByCollectionInstrumentIdAndInterrogationId(any(), any());

        //WHEN
        ContextualPreviousVariableModel actual = contextualPreviousVariableJsonService.findByCollectionInstrumentIdAndInterrogationId(
                collectionInstrumentId,
                interrogationId
        );

        //THEN
        verify(contextualPreviousVariablePersistancePort, times(1))
                .findByCollectionInstrumentIdAndInterrogationId(collectionInstrumentId, interrogationId);
        Assertions.assertThat(actual).isEqualTo(expected);
    }
}