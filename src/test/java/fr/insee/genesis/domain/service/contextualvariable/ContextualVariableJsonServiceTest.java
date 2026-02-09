package fr.insee.genesis.domain.service.contextualvariable;

import fr.insee.genesis.TestConstants;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.SneakyThrows;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.util.FileSystemUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class ContextualVariableJsonServiceTest {

    private static final String TEST_FOLDER = "testContextual";
    private static final Path TEST_FOLDER_PATH = Path.of(TestConstants.TEST_RESOURCES_DIRECTORY, TEST_FOLDER);

    private ContextualPreviousVariableApiPort contextualPreviousVariableApiPort;
    private ContextualExternalVariableApiPort contextualExternalVariableApiPort;

    ContextualVariableJsonService contextualVariableJsonService;

    @BeforeEach
    void setUp() throws IOException {
        contextualPreviousVariableApiPort = mock(ContextualPreviousVariableApiPort.class);
        contextualExternalVariableApiPort = mock(ContextualExternalVariableApiPort.class);
        contextualVariableJsonService = new ContextualVariableJsonService(
                contextualPreviousVariableApiPort,
                contextualExternalVariableApiPort
        );
        if (Files.exists(TEST_FOLDER_PATH)){
            FileSystemUtils.deleteRecursively(TEST_FOLDER_PATH);
        }
        Files.createDirectories(TEST_FOLDER_PATH);
        Files.createFile(TEST_FOLDER_PATH.resolve("ok.json"));
        Files.createFile(TEST_FOLDER_PATH.resolve("ok2.json"));
    }

    @Test
    void getContextualVariable_simple_variable_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test";
        String testVariableName = "testVariable";

        ContextualPreviousVariableModel contextualPreviousVariableModel = ContextualPreviousVariableModel
                .builder()
                .variables(new HashMap<>())
                .build();
        int value = 1;
        contextualPreviousVariableModel.getVariables().put(testVariableName, value);
        doReturn(contextualPreviousVariableModel).when(contextualPreviousVariableApiPort)
                .findByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                );

        ContextualExternalVariableModel contextualExternalVariableModel = ContextualExternalVariableModel
                .builder()
                .variables(new HashMap<>())
                .build();
        value = 2;
        contextualExternalVariableModel.getVariables().put(testVariableName, value);
        doReturn(contextualExternalVariableModel).when(contextualExternalVariableApiPort)
                .findByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                );

        //WHEN
        ContextualVariableModel contextualVariableModel =
                contextualVariableJsonService.getContextualVariable(collectionInstrumentId, interrogationId);

        //THEN
        Assertions.assertThat(contextualVariableModel).isNotNull();

        //Previous
        Assertions.assertThat(contextualVariableModel.contextualPrevious()).isNotNull().hasSize(1);
        List<VariableQualityToolDto> variableQualityToolDtosPrevious = contextualVariableModel.contextualPrevious();
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getVariableName())
                .isEqualTo(testVariableName);
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getIteration())
                .isEqualTo(1);
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        VariableStateDto variableStateDto = variableQualityToolDtosPrevious.getFirst().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(1);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();

        //External
        Assertions.assertThat(contextualVariableModel.contextualExternal()).isNotNull().hasSize(1);
        //Sort by iteration to avoid random order
        List<VariableQualityToolDto> variableQualityToolDtosExternal = contextualVariableModel.contextualExternal();
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getVariableName())
                .isEqualTo(testVariableName);
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getIteration())
                .isEqualTo(1);
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        variableStateDto = variableQualityToolDtosExternal.getFirst().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(2);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();
    }

    @Test
    void getContextualVariable_extract_lists_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test";
        String testListVarName = "testList";

        ContextualPreviousVariableModel contextualPreviousVariableModel = ContextualPreviousVariableModel
                .builder()
                .variables(new HashMap<>())
                .build();
        List<Integer> contextualPreviousList = List.of(1,2);
        contextualPreviousVariableModel.getVariables().put(testListVarName, contextualPreviousList);
        doReturn(contextualPreviousVariableModel).when(contextualPreviousVariableApiPort)
                .findByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                );


        ContextualExternalVariableModel contextualExternalVariableModel = ContextualExternalVariableModel
                .builder()
                .variables(new HashMap<>())
                .build();
        List<Integer> contextualExternalList = List.of(3,4);
        contextualExternalVariableModel.getVariables().put(testListVarName, contextualExternalList);
        doReturn(contextualExternalVariableModel).when(contextualExternalVariableApiPort)
                .findByCollectionInstrumentIdAndInterrogationId(
                        collectionInstrumentId,
                        interrogationId
                );

        //WHEN
        ContextualVariableModel contextualVariableModel =
                contextualVariableJsonService.getContextualVariable(collectionInstrumentId, interrogationId);

        //THEN
        Assertions.assertThat(contextualVariableModel).isNotNull();

        //Previous
        Assertions.assertThat(contextualVariableModel.contextualPrevious()).isNotNull().hasSize(2);
        //Sort by iteration to avoid random order
        List<VariableQualityToolDto> variableQualityToolDtosPrevious = contextualVariableModel.contextualPrevious()
                .stream().sorted(
                        Comparator.comparing(VariableQualityToolDto::getIteration)
                ).toList();
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getVariableName())
                .isEqualTo(testListVarName);
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getIteration())
                .isEqualTo(1);
        Assertions.assertThat(variableQualityToolDtosPrevious.getFirst().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        VariableStateDto variableStateDto = variableQualityToolDtosPrevious.getFirst().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(1);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();

        Assertions.assertThat(variableQualityToolDtosPrevious.getLast().getVariableName())
                .isEqualTo(testListVarName);
        Assertions.assertThat(variableQualityToolDtosPrevious.getLast().getIteration())
                .isEqualTo(2);
        Assertions.assertThat(variableQualityToolDtosPrevious.getLast().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        variableStateDto = variableQualityToolDtosPrevious.getLast().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(2);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();

        //External
        Assertions.assertThat(contextualVariableModel.contextualExternal()).isNotNull().hasSize(2);
        //Sort by iteration to avoid random order
        List<VariableQualityToolDto> variableQualityToolDtosExternal = contextualVariableModel.contextualExternal()
                .stream().sorted(
                        Comparator.comparing(VariableQualityToolDto::getIteration)
                ).toList();
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getVariableName())
                .isEqualTo(testListVarName);
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getIteration())
                .isEqualTo(1);
        Assertions.assertThat(variableQualityToolDtosExternal.getFirst().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        variableStateDto = variableQualityToolDtosExternal.getFirst().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(3);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();

        Assertions.assertThat(variableQualityToolDtosExternal.getLast().getVariableName())
                .isEqualTo(testListVarName);
        Assertions.assertThat(variableQualityToolDtosExternal.getLast().getIteration())
                .isEqualTo(2);
        Assertions.assertThat(variableQualityToolDtosExternal.getLast().getVariableStateDtoList())
                .isNotNull().hasSize(1);
        variableStateDto = variableQualityToolDtosExternal.getLast().getVariableStateDtoList().getFirst();
        Assertions.assertThat(variableStateDto.getState()).isEqualTo(DataState.COLLECTED);
        Assertions.assertThat(variableStateDto.getValue()).isEqualTo(4);
        Assertions.assertThat(variableStateDto.isActive()).isTrue();
        Assertions.assertThat(variableStateDto.getDate()).isNotNull();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true}) //false if only external, true if only previous
    void getContextualVariable_only_one_model_test(boolean onlyPrevious) {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test";
        String testVariableName = "testVariable";

        if(onlyPrevious){
            ContextualPreviousVariableModel contextualPreviousVariableModel = ContextualPreviousVariableModel
                    .builder()
                    .variables(new HashMap<>())
                    .build();
            int value = 1;
            contextualPreviousVariableModel.getVariables().put(testVariableName, value);
            doReturn(contextualPreviousVariableModel).when(contextualPreviousVariableApiPort)
                    .findByCollectionInstrumentIdAndInterrogationId(
                            collectionInstrumentId,
                            interrogationId
                    );
        }else{
            ContextualExternalVariableModel contextualExternalVariableModel = ContextualExternalVariableModel
                    .builder()
                    .variables(new HashMap<>())
                    .build();
            int value = 2;
            contextualExternalVariableModel.getVariables().put(testVariableName, value);
            doReturn(contextualExternalVariableModel).when(contextualExternalVariableApiPort)
                    .findByCollectionInstrumentIdAndInterrogationId(
                            collectionInstrumentId,
                            interrogationId
                    );
        }

        //WHEN
        ContextualVariableModel contextualVariableModel =
                contextualVariableJsonService.getContextualVariable(collectionInstrumentId, interrogationId);

        //THEN
        if(onlyPrevious){
            Assertions.assertThat(contextualVariableModel.contextualExternal()).isNotNull().isEmpty();
            Assertions.assertThat(contextualVariableModel.contextualPrevious()).isNotNull().hasSize(1);
        }else{
            Assertions.assertThat(contextualVariableModel.contextualPrevious()).isNotNull().isEmpty();
            Assertions.assertThat(contextualVariableModel.contextualExternal()).isNotNull().hasSize(1);
        }
    }

    @Test
    void getContextualVariable_null_model_test() {
        //GIVEN
        String collectionInstrumentId = "test";
        String interrogationId = "test";

        //WHEN
        ContextualVariableModel contextualVariableModel =
                contextualVariableJsonService.getContextualVariable(collectionInstrumentId, interrogationId);

        //THEN
            Assertions.assertThat(contextualVariableModel.contextualExternal()).isNotNull().isEmpty();
            Assertions.assertThat(contextualVariableModel.contextualPrevious()).isNotNull().isEmpty();
    }

    @Test
    @SneakyThrows
    void saveContextualVariableFiles() {
        //GIVEN
        String collectionInstrumentId = "test";
        FileUtils fileUtils = mock(FileUtils.class);
        List<Path> pathList = List.of(
                Path.of("ok.json"),
                Path.of("ok2.json")
        );
        String doneFolder = "testDone";
        doReturn(doneFolder).when(fileUtils).getDoneFolder(anyString(),anyString());
        doReturn(pathList).when(fileUtils).listFiles(anyString());
        doReturn(true).when(contextualPreviousVariableApiPort)
                .readContextualPreviousFile(any(), any(), any());
        doReturn(true).when(contextualExternalVariableApiPort)
                .readContextualExternalFile(any(), any());
        doAnswer(invocation ->
                Files.deleteIfExists(invocation.getArgument(0))
        ).when(fileUtils).moveFiles(any(Path.class), any());

        //WHEN
        int fileCount = contextualVariableJsonService.saveContextualVariableFiles(
                collectionInstrumentId,
                fileUtils,
                TEST_FOLDER_PATH.toString()
                );

        //THEN
        Assertions.assertThat(fileCount).isEqualTo(pathList.size());
        verify(fileUtils, times(pathList.size())).moveFiles(
                any(),
                eq(doneFolder)
        );
    }

    @AfterEach
    void clean() throws IOException {
        if (Files.exists(TEST_FOLDER_PATH)){
            FileSystemUtils.deleteRecursively(TEST_FOLDER_PATH);
        }
    }
}