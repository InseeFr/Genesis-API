package fr.insee.genesis.controller.utils;

import fr.insee.genesis.controller.dto.SurveyUnitDto;
import fr.insee.genesis.controller.dto.SurveyUnitQualityToolDto;
import fr.insee.genesis.controller.dto.VariableQualityToolDto;
import fr.insee.genesis.controller.dto.VariableDto;
import lombok.experimental.UtilityClass;

import java.util.ArrayList;
import java.util.List;

@UtilityClass
public class DataTransformer {

    public static SurveyUnitQualityToolDto transformSurveyUnitDto(SurveyUnitDto dto){

        List<VariableQualityToolDto> transformedCollectedVariables = new ArrayList<>();
        if (dto.getCollectedVariables() != null) {
            transformedCollectedVariables = dto.getCollectedVariables()
                    .stream()
                    .map(DataTransformer::transformVariable)
                    .toList();
        }

        List<VariableQualityToolDto> transformedExternalVariables = new ArrayList<>();
        if (dto.getExternalVariables() != null) {
            transformedExternalVariables = dto.getExternalVariables()
                    .stream()
                    .map(DataTransformer::transformVariable)
                    .toList();
        }

        return SurveyUnitQualityToolDto.builder()
                .surveyUnitId(dto.getSurveyUnitId())
                .collectedVariables(transformedCollectedVariables)
                .externalVariables(transformedExternalVariables)
                .build();
    }

    private static VariableQualityToolDto transformVariable(VariableDto variable) {
        Integer iteration = null;

        if (variable.getScope() != null) {
            iteration = LoopIdParser.extractIndex(variable.getScope());
        }

        return VariableQualityToolDto.builder()
            .variableName(variable.getVariableName())
            .iteration(iteration)
            .variableStateDtoList(variable.getVariableStateDtoList())
            .build();
    }

}
