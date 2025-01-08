package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;

import java.util.List;

@Builder
public record VariableModel(
        String idVar,
        List<String> values,
        String idLoop,
        String idParent
) {}
