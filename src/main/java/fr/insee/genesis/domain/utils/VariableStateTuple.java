package fr.insee.genesis.domain.utils;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.VariableModel;

public record VariableStateTuple(
        VariableModel variableModel,
        DataState dataState
) {
}
