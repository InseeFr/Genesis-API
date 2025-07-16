package fr.insee.genesis.controller.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record EditedResponseDto (
        String interrogationId,
        List<VariableQualityToolDto> editedPrevious,
        List<VariableQualityToolDto> editedExternal
){}
