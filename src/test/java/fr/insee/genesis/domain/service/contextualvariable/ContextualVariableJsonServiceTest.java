package fr.insee.genesis.domain.service.contextualvariable;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ContextualVariableJsonServiceTest {

    private static final String TEST_FOLDER = "testContextual";
    private static final Path TEST_FOLDER_PATH = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, TEST_FOLDER);

    @Mock
    private ContextualPreviousVariableApiPort contextualPreviousVariableApiPort;

    @Mock
    private ContextualExternalVariableApiPort contextualExternalVariableApiPort;

    @InjectMocks
    ContextualVariableJsonService contextualVariableJsonService;

    @BeforeEach
    void setUp() throws IOException {
        if (Files.exists(TEST_FOLDER_PATH)){
            FileSystemUtils.deleteRecursively(TEST_FOLDER_PATH);
        }
        Files.createDirectories(TEST_FOLDER_PATH);
        Files.createFile(TEST_FOLDER_PATH.resolve("ok.json"));
        Files.createFile(TEST_FOLDER_PATH.resolve("ok2.json"));
    }

    @Test
    void getContextualVariableByList_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationPrefix = "interro";
        List<String> interrogationIds = new ArrayList<>();
        for (int i = 1; i <= 3; i++) {
            interrogationIds.add(interrogationPrefix + i);
        }
        //Previous
        Map<String, ContextualPreviousVariableModel> contextualPreviousVariableModelMap = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            contextualPreviousVariableModelMap.put(
                    interrogationPrefix + i,
                    ContextualPreviousVariableModel.builder().build()
            );
        }
        doReturn(contextualPreviousVariableModelMap)
                .when(contextualPreviousVariableApiPort).findByCollectionInstrumentIdAndInterrogationIdList(
                        anyString(), anyList()
                );
        //External
        Map<String, ContextualExternalVariableModel> contextualExternalVariableModelMap = new HashMap<>();
        for (int i = 1; i <= 3; i++) {
            contextualExternalVariableModelMap.put(
                    interrogationPrefix + i,
                    ContextualExternalVariableModel.builder().build()
            );
        }
        doReturn(contextualExternalVariableModelMap)
                .when(contextualExternalVariableApiPort).findByCollectionInstrumentIdAndInterrogationIdList(
                        anyString(), anyList()
                );

        //WHEN
        Map<String, ContextualVariableModel> result = contextualVariableJsonService.getContextualVariablesByList(
                collectionInstrumentId, interrogationIds
        );

        //THEN
        verify(contextualPreviousVariableApiPort, times(1))
                .findByCollectionInstrumentIdAndInterrogationIdList(
                        collectionInstrumentId, interrogationIds
                );
        verify(contextualExternalVariableApiPort, times(1))
                .findByCollectionInstrumentIdAndInterrogationIdList(
                        collectionInstrumentId, interrogationIds
                );
        Assertions.assertThat(result).isNotNull().containsOnlyKeys(interrogationIds);
        for(Map.Entry<String, ContextualVariableModel> entry : result.entrySet()){
            Assertions.assertThat(entry.getValue()).isNotNull();
        }

    }



    @AfterEach
    void clean() throws IOException {
        if (Files.exists(TEST_FOLDER_PATH)){
            FileSystemUtils.deleteRecursively(TEST_FOLDER_PATH);
        }
    }
}