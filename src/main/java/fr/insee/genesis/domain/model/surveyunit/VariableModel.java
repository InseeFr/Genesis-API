package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;

import java.util.List;

@Builder
public record VariableModel(
        String varId,
        List<String> values,
        String loopId,
        String parentId
) {}
