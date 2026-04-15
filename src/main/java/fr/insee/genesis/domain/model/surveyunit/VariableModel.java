package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;

@Builder
public record VariableModel(
        String varId,
        String value,
        DataState state,
        String scope,
        Integer iteration,
        String parentId
) {}
