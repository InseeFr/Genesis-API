package fr.insee.genesis.controller.dto;

import java.util.List;

public record SaveContextualVariablesReportDto(
        String questionnaireId,
        int processedFiles,
        List<ContextualVariableFileReportDto> files
) {}
