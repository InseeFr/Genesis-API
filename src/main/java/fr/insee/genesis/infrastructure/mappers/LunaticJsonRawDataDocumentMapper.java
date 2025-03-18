package fr.insee.genesis.infrastructure.mappers;

import fr.insee.genesis.domain.model.surveyunit.DataState;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawData;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataCollectedVariable;
import fr.insee.genesis.domain.model.surveyunit.rawdata.LunaticJsonRawDataVariable;
import fr.insee.genesis.infrastructure.document.rawdata.LunaticJsonRawDataDocument;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Mapper
public interface LunaticJsonRawDataDocumentMapper {

    @Mapping(target = "collectedVariables", source = "collectedVariables")
    @Mapping(target = "externalVariables", source = "externalVariables")
    LunaticJsonRawData documentToModel(LunaticJsonRawDataDocument document);

    LunaticJsonRawDataDocument modelToDocument(LunaticJsonRawData model);

    // Custom convert method for nested Map
    default Map<String, LunaticJsonRawDataCollectedVariable> mapCollectedVariables(
            Map<String, Map<DataState, Object>> collectedVariables) {

        if (collectedVariables == null) {
            return Map.of();
        }

        return collectedVariables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapToCollectedVariable(entry.getValue())
                ));
    }

    // Convert method from Map<DataState, Object> in LunaticJsonRawDataCollectedVariable
    default LunaticJsonRawDataCollectedVariable mapToCollectedVariable(
            Map<DataState, Object> stateObjectMap) {

        if (stateObjectMap == null) {
            return LunaticJsonRawDataCollectedVariable.builder()
                    .collectedVariableByStateMap(Map.of())
                    .build();
        }

        Map<DataState, LunaticJsonRawDataVariable> variableByStateMap = stateObjectMap.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapToVariable(entry.getValue())
                ));

        return LunaticJsonRawDataCollectedVariable.builder()
                .collectedVariableByStateMap(variableByStateMap)
                .build();
    }

    // Convert an object in LunaticJsonRawDataVariable
    default LunaticJsonRawDataVariable mapToVariable(Object value) {
        if (value instanceof List<?> list) {
            List<String> stringList = list.stream()
                    .map(Object::toString)
                    .collect(Collectors.toList());
            return LunaticJsonRawDataVariable.builder()
                    .valuesArray(stringList)
                    .value(null)
                    .build();
        } else if (value != null) {
            return LunaticJsonRawDataVariable.builder()
                    .valuesArray(null)
                    .value(value.toString())
                    .build();
        } else {
            return LunaticJsonRawDataVariable.builder()
                    .valuesArray(null)
                    .value(null)
                    .build();
        }
    }

    // ------- CUSTOM MAPPING METHODS (TO DOCUMENT) -------

    default Map<String, Map<DataState, Object>> mapCollectedVariablesToDocument(
            Map<String, LunaticJsonRawDataCollectedVariable> collectedVariables) {

        if (collectedVariables == null) {
            return Map.of();
        }

        return collectedVariables.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapCollectedVariableToDocument(entry.getValue())
                ));
    }

    default Map<DataState, Object> mapCollectedVariableToDocument(
            LunaticJsonRawDataCollectedVariable collectedVariable) {

        if (collectedVariable == null || collectedVariable.collectedVariableByStateMap() == null) {
            return Map.of();
        }

        return collectedVariable.collectedVariableByStateMap().entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> mapVariableToDocument(entry.getValue())
                ));
    }

    default Object mapVariableToDocument(LunaticJsonRawDataVariable variable) {
        if (variable == null) {
            return null;
        }

        if (variable.valuesArray() != null && !variable.valuesArray().isEmpty()) {
            return variable.valuesArray();
        } else {
            return variable.value();
        }
    }

    // ------- EXTERNAL VARIABLES -------

    default Map<String, Object> mapExternalVariablesToDocument(Map<String, Object> externalVariables) {
        return externalVariables != null ? externalVariables : Map.of();
    }


    // Direct mapping for external variables (if necessary to customize later)
    default Map<String, Object> mapExternalVariables(Map<String, Object> externalVariables) {
        return externalVariables != null ? externalVariables : Map.of();
    }
}
