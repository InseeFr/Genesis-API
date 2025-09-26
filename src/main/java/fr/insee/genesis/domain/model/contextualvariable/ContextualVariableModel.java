package fr.insee.genesis.domain.model.contextualvariable;

import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import lombok.Builder;

import java.util.List;

@Builder
public record ContextualVariableModel(
        String interrogationId,
        List<VariableQualityToolDto> contextualPrevious,
        List<VariableQualityToolDto> contextualExternal
){}
