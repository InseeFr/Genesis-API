package fr.insee.genesis.domain.service.contextualvariable;

import fr.insee.genesis.controller.dto.ContextualVariableFileReportDto;
import fr.insee.genesis.controller.dto.SaveContextualVariablesReportDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableStateDto;
import fr.insee.genesis.domain.model.contextualvariable.ContextualExternalVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualPreviousVariableModel;
import fr.insee.genesis.domain.model.contextualvariable.ContextualVariableModel;
import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.Mode;
import fr.insee.genesis.domain.ports.api.ContextualExternalVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualPreviousVariableApiPort;
import fr.insee.genesis.domain.ports.api.ContextualVariableApiPort;
import fr.insee.genesis.exceptions.GenesisException;
import fr.insee.genesis.infrastructure.utils.FileUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static fr.insee.genesis.Constants.TYPE_EXTERNAL;
import static fr.insee.genesis.Constants.TYPE_PREVIOUS;

@Service
@Slf4j
public class ContextualVariableJsonService implements ContextualVariableApiPort {
    private final ContextualPreviousVariableApiPort contextualPreviousVariableApiPort;
    private final ContextualExternalVariableApiPort contextualExternalVariableApiPort;

    @Autowired
    public ContextualVariableJsonService(ContextualPreviousVariableApiPort contextualPreviousVariableApiPort, ContextualExternalVariableApiPort contextualExternalVariableApiPort) {
        this.contextualPreviousVariableApiPort = contextualPreviousVariableApiPort;
        this.contextualExternalVariableApiPort = contextualExternalVariableApiPort;
    }

    @Override
    public ContextualVariableModel getContextualVariable(String collectionInstrumentId, String interrogationId) {
        ContextualVariableModel contextualVariableModel = ContextualVariableModel.builder()
                .interrogationId(interrogationId)
                .contextualPrevious(new ArrayList<>())
                .contextualExternal(new ArrayList<>())
                .build();

        ContextualPreviousVariableModel contextualPreviousVariableModel =
                        contextualPreviousVariableApiPort.findByCollectionInstrumentIdAndInterrogationId(
                                collectionInstrumentId,
                                interrogationId
                        );

        if(contextualPreviousVariableModel != null) {
            for (Map.Entry<String, Object> variable : contextualPreviousVariableModel.getVariables().entrySet()) {
                contextualVariableModel.contextualPrevious().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        ContextualExternalVariableModel contextualExternalVariableModel =
                        contextualExternalVariableApiPort.findByCollectionInstrumentIdAndInterrogationId(
                                collectionInstrumentId,
                                interrogationId
                        );

        if(contextualExternalVariableModel != null) {
            for (Map.Entry<String, Object> variable : contextualExternalVariableModel.getVariables().entrySet()) {
                contextualVariableModel.contextualExternal().addAll(extractVariables(variable.getValue(), variable.getKey()));
            }
        }

        return contextualVariableModel;
    }

    @Override
    public int saveContextualVariableFiles(
            String collectionInstrumentId,
            FileUtils fileUtils,
            String contextualFolderPath
    ) throws GenesisException {
        return saveContextualVariableFilesWithReport(
                collectionInstrumentId,
                fileUtils,
                contextualFolderPath
        ).processedFiles();
    }

    /**
     * Generates a detailed report of processed contextual variable files.
     * Added to preserve the existing behavior of {@link #saveContextualVariableFiles(String, FileUtils, String)},
     * which only returns the number of processed files.
     */
    @Override
    public SaveContextualVariablesReportDto saveContextualVariableFilesWithReport(
            String collectionInstrumentId,
            FileUtils fileUtils,
            String contextualFolderPath
    ) throws GenesisException {
        List<ContextualVariableFileReportDto> files = new ArrayList<>();

        for (Mode mode : Mode.values()) {
            try (Stream<Path> filePaths = Files.list(Path.of(contextualFolderPath))) {
                Iterator<Path> it = filePaths
                        .filter(path -> path.toString().endsWith(".json"))
                        .iterator();

                while (it.hasNext()) {
                    Path jsonFilePath = it.next();

                    Optional<ContextualVariableFileReportDto> report =
                            processContextualVariableFileForReport(collectionInstrumentId, jsonFilePath);

                    if (report.isPresent()) {
                        moveFile(collectionInstrumentId, mode, fileUtils, jsonFilePath.toString());
                        files.add(report.get());
                    }
                }
            } catch (NoSuchFileException nsfe) {
                log.debug(nsfe.toString());
            } catch (IOException ioe) {
                log.warn(ioe.toString());
            }
        }

        return new SaveContextualVariablesReportDto(
                collectionInstrumentId,
                files.size(),
                files
        );
    }

    private Optional<ContextualVariableFileReportDto> processContextualVariableFileForReport(
            String collectionInstrumentId,
            Path jsonFilePath
    ) throws GenesisException {
        try {
            Optional<String> type = processContextualVariableFileAndGetType(
                    collectionInstrumentId,
                    jsonFilePath
            );

            return type.map(value -> new ContextualVariableFileReportDto(
                    jsonFilePath.getFileName().toString(),
                    value
            ));
        } catch (GenesisException e) {
            throw new GenesisException(
                    e.getStatus(),
                    "Error while processing file '%s' : %s"
                            .formatted(jsonFilePath.getFileName().toString(), e.getMessage())
            );
        }
    }

    private Optional<String> processContextualVariableFileAndGetType(
            String collectionInstrumentId,
            Path jsonFilePath
    ) throws GenesisException {
        boolean isPrevious = contextualPreviousVariableApiPort.readContextualPreviousFile(
                collectionInstrumentId.toUpperCase(),
                null,
                jsonFilePath.toString()
        );

        if (isPrevious) {
            return Optional.of(TYPE_PREVIOUS);
        }

        boolean isExternal = contextualExternalVariableApiPort.readContextualExternalFile(
                collectionInstrumentId.toUpperCase(),
                jsonFilePath.toString()
        );

        if (isExternal) {
            return Optional.of(TYPE_EXTERNAL);
        }

        return Optional.empty();
    }


    private static void moveFile(String collectionInstrumentId, Mode mode, FileUtils fileUtils, String filePath) throws GenesisException {
        try {
            fileUtils.moveFiles(Path.of(filePath), fileUtils.getDoneFolder(collectionInstrumentId, mode.getFolder()));
        } catch (IOException e) {
            throw new GenesisException(HttpStatus.INTERNAL_SERVER_ERROR, "Error while moving file to done : %s".formatted(e.toString()));
        }
    }

    @SuppressWarnings("unchecked")
    private List<VariableQualityToolDto> extractVariables(Object variable, String variableName) {
        List<VariableQualityToolDto> variableQualityToolDtos = new ArrayList<>();

        if(!(variable instanceof List<?>)){
            variableQualityToolDtos.add(extractValue(variable, variableName, 1));
            return variableQualityToolDtos;
        }

        int i = 1;
        for(Object element : (List<Object>)variable){
            variableQualityToolDtos.add(extractValue(element, variableName, i));
            i++;
        }
        return variableQualityToolDtos;
    }

    private VariableQualityToolDto extractValue(Object variable, String variableName, int iteration) {
        VariableQualityToolDto variableQualityToolDto = VariableQualityToolDto.builder()
                .variableName(variableName)
                .iteration(iteration)
                .variableStateDtoList(new ArrayList<>())
                .build();
        variableQualityToolDto.getVariableStateDtoList().add(
                VariableStateDto.builder()
                        .state(DataState.COLLECTED)
                        .active(true)
                        .value(variable)
                        .date(Instant.now())
                        .build()
        );
        return variableQualityToolDto;
    }
}
