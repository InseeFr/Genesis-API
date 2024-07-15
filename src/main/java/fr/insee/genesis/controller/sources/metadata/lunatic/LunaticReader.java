package fr.insee.genesis.controller.sources.metadata.lunatic;


import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.metadata.Group;
import fr.insee.genesis.controller.sources.metadata.McqVariable;
import fr.insee.genesis.controller.sources.metadata.UcqVariable;
import fr.insee.genesis.controller.sources.metadata.Variable;
import fr.insee.genesis.controller.sources.metadata.VariableType;
import fr.insee.genesis.controller.sources.metadata.VariablesMap;
import fr.insee.genesis.exceptions.GenesisException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public class LunaticReader {
    private static final String VARIABLES = "variables";
    private static final String EXCEPTION_MESSAGE = "Unable to read Lunatic questionnaire file: ";
    private static final String RESPONSE = "response";
    private static final String COMPONENTS = "components";
    private static final String COMPONENT_TYPE = "componentType";
    private static final String VALUE = "value";
    private static final String LABEL = "label";
    private static final String MISSING_RESPONSE = "missingResponse";
    private static final String LUNATIC_MODEL_VERSION = "lunaticModelVersion";

    private LunaticReader() {
        throw new IllegalStateException("Utility class");
    }

    public static VariablesMap getVariablesFromLunaticJson(Path lunaticJsonPath) throws GenesisException {
        try {
            JsonNode rootNode = readJson(lunaticJsonPath);
            String lunaticModelVersion = rootNode.get(LUNATIC_MODEL_VERSION).asText();
            boolean isLunaticV2 = compareVersions(lunaticModelVersion, "2.3.0") > 0;
            List<String> variables = new ArrayList<>();
            JsonNode variablesNode = rootNode.get(VARIABLES);
            variablesNode.forEach(newVar -> variables.add(newVar.get("name").asText()));
            VariablesMap variablesMap = new VariablesMap();
            // Root group is created in VariablesMap constructor
            Group rootGroup = variablesMap.getRootGroup();
            iterateOnComponents(rootNode, variables, variablesMap, rootGroup, isLunaticV2);

            // We iterate on root components to identify variables belonging to root group
            JsonNode rootComponents = rootNode.get(COMPONENTS);
            for (JsonNode comp : rootComponents) {
                addResponsesAndMissing(comp, rootGroup, variables, variablesMap, isLunaticV2);
            }

            // We add the remaining (not identified in any loops nor root) variables to the root group
            variables.forEach(
                    varName -> variablesMap.putVariable(new Variable(varName, rootGroup, VariableType.STRING)));
            return variablesMap;
        } catch (Exception e) {
            throw new GenesisException(500, EXCEPTION_MESSAGE);
        }
    }

    /**
     * Compare two versions of the form x.y.z
     *
     * @param version1 : version of the form x.y.z
     * @param version2 : version of the form x.y.z
     * @return 1 if version1 is greater, 0 if they are equal, -1 if version2 is greater.
     */
    public static int compareVersions(String version1, String version2) {
        int comparisonResult = 0;

        String[] version1Splits = version1.split("\\.");
        String[] version2Splits = version2.split("\\.");
        int maxLengthOfVersionSplits = Math.max(version1Splits.length, version2Splits.length);

        for (int i = 0; i < maxLengthOfVersionSplits; i++) {
            Integer v1 = i < version1Splits.length ? Integer.parseInt(version1Splits[i]) : 0;
            Integer v2 = i < version2Splits.length ? Integer.parseInt(version2Splits[i]) : 0;
            int compare = v1.compareTo(v2);
            if (compare != 0) {
                comparisonResult = compare;
                break;
            }
        }
        return comparisonResult;
    }

    /**
     * Recursive function to get groups from lunatic json and puts it in the variablesMap
     *
     * @param rootNode     root node of lunatic JSON
     * @param variablesMap variablesMap to fill
     */
    private static void iterateOnComponents(JsonNode rootNode, List<String> variables, VariablesMap variablesMap,
                                            Group parentGroup, boolean isLunaticV2) {
        JsonNode componentsNode = rootNode.get(COMPONENTS);
        if (componentsNode.isArray()) {
            for (JsonNode component : componentsNode) {
                if (component.get(COMPONENT_TYPE).asText().equals("Loop")) {
                    if (component.has("lines")) {
                        processPrimaryLoop(variables, variablesMap, parentGroup, component, isLunaticV2);
                    }
                    if (component.has("iterations")) {
                        Group group = processDependingLoop(variables, variablesMap, parentGroup,
                                component, isLunaticV2);
                        iterateOnComponents(component, variables, variablesMap, group, isLunaticV2);
                    }
                }
            }
        }
    }

    private static void processPrimaryLoop(List<String> variables, VariablesMap variablesMap, Group parentGroup,
                                           JsonNode component, boolean isLunaticV2) {
        JsonNode primaryComponents = component.get(COMPONENTS);
        //We create a group only with the name of the first response
        //Then we add all the variables found in response to the newly created group
        String groupName = getFirstResponseName(primaryComponents);
        log.info("Creation of group :" + groupName);
        Group group = putNewGroup(variablesMap, groupName, parentGroup);
        for (JsonNode primaryComponent : primaryComponents) {
            addResponsesAndMissing(primaryComponent, group, variables, variablesMap, isLunaticV2);
        }
    }

    private static String getFirstResponseName(JsonNode components) {
        for (JsonNode component : components) {
            if (component.has(RESPONSE)) {
                return component.get(RESPONSE).get("name").asText();
            }
        }
        return null;
    }

    private static Group putNewGroup(VariablesMap variablesMap, String newName, Group parentGroup) {
        Group group = new Group(String.format("%s_%s", Constants.LOOP_NAME_PREFIX, newName), parentGroup.getName());
        variablesMap.putGroup(group);
        return group;
    }

    /**
     * Adds variables to the metadata model (it infers type of variables from the component type)
     * Checks Lunatic version to adapt to the different ways of writing the JSON
     *
     * @param primaryComponent : a component of the questionnaire
     * @param group            : the group to which the variables belong
     * @param variablesMap     : metadata model of the questionnaire to be completed
     */
    private static void addResponsesAndMissing(JsonNode primaryComponent, Group group, List<String> variables,
                                               VariablesMap variablesMap, boolean isLunaticV2) {
        //We read the name of the collected variables in response(s)
        //And we deduce the variable type by looking at the component that encapsulate the variable
        ComponentLunatic componentType = ComponentLunatic.fromJsonName(primaryComponent.get(COMPONENT_TYPE).asText());
        String variableName;
        switch (componentType) {
            case ComponentLunatic.DATE_PICKER, ComponentLunatic.CHECKBOX_BOOLEAN, ComponentLunatic.INPUT,
                    ComponentLunatic.TEXT_AREA, ComponentLunatic.SUGGESTER:
                variableName = getVariableName(primaryComponent);
                variablesMap.putVariable(new Variable(variableName, group, componentType.getType()));
                variables.remove(variableName);
                break;
            case ComponentLunatic.INPUT_NUMBER:
                variableName = getVariableName(primaryComponent);
                if (primaryComponent.get("decimals").asInt() == 0) {
                    variablesMap.putVariable(new Variable(variableName, group, VariableType.INTEGER));
                    break;
                }
                variablesMap.putVariable(new Variable(variableName, group, VariableType.NUMBER));
                variables.remove(variableName);
                break;
            case ComponentLunatic.DROPDOWN:
                variableName = getVariableName(primaryComponent);
                UcqVariable ucqVar = new UcqVariable(variableName, group, VariableType.STRING);
                JsonNode modalities = primaryComponent.get("options");
                for (JsonNode modality : modalities) {
                    ucqVar.addModality(modality.get(VALUE).asText(), modality.get(LABEL).asText());
                }
                variablesMap.putVariable(ucqVar);
                variables.remove(variableName);
                break;
            case ComponentLunatic.RADIO, ComponentLunatic.CHECKBOX_ONE:
                variableName = getVariableName(primaryComponent);
                UcqVariable ucqVarOne = new UcqVariable(variableName, group, VariableType.STRING);
                JsonNode modalitiesOne = primaryComponent.get("options");
                for (JsonNode modality : modalitiesOne) {
                    if (isLunaticV2) {
                        ucqVarOne.addModality(modality.get(VALUE).asText(), modality.get(LABEL).get(VALUE).asText());
                        continue;
                    }
                    ucqVarOne.addModality(modality.get(VALUE).asText(), modality.get(LABEL).asText());
                }
                variablesMap.putVariable(ucqVarOne);
                variables.remove(variableName);
                break;
            case ComponentLunatic.CHECKBOX_GROUP:
                processCheckboxGroup(primaryComponent, group, variables, variablesMap, isLunaticV2);
                break;
            case ComponentLunatic.PAIRWISE_LINKS:
                // In we case of a pairwiseLinks component we have to iterate on the components to find the responses
                // It is a nested component, but we treat it differently than the loops because it does not create a
                // new level of information
                iterateOnComponentsToFindResponses(primaryComponent, variables, variablesMap, group, isLunaticV2);
                break;
            case ComponentLunatic.TABLE:
                iterateOnTableBody(primaryComponent, group, variables, variablesMap, isLunaticV2);
                break;
            case null:
                log.warn(String.format("%s component type not recognized",
                        primaryComponent.get(COMPONENT_TYPE).asText()));
                break;
        }
        //We also had the missing variable if it exists (only one missing variable even if multiple responses)
        addMissingVariable(primaryComponent, group, variables, variablesMap);
    }

    private static String getVariableName(JsonNode component) {
        return component.get(RESPONSE).get("name").asText();
    }

    /**
     * Process a checkbox group to create a boolean variable for each response
     *
     * @param checkboxComponent : component representing a checkbox group
     * @param group             : group to which the variables belong
     * @param variablesMap      : metadata model of the questionnaire to be completed
     * @param isLunaticV2       : true if the Lunatic version is 2.3 or higher
     */
    private static void processCheckboxGroup(JsonNode checkboxComponent, Group group, List<String> variables,
                                             VariablesMap variablesMap, boolean isLunaticV2) {
        String variableName;
        JsonNode responses = checkboxComponent.get("responses");
        List<String> responsesName = new ArrayList<>();
        for (JsonNode response : responses) {
            responsesName.add(getVariableName(response));
        }
        String questionName = findLongestCommonPrefix(responsesName);
        for (JsonNode response : responses) {
            variableName = getVariableName(response);
            McqVariable mcqVariable = new McqVariable(variableName, group, VariableType.BOOLEAN);
            if (isLunaticV2) mcqVariable.setText(response.get(LABEL).get(VALUE).asText());
            if (!isLunaticV2) mcqVariable.setText(response.get(LABEL).asText());
            mcqVariable.setInQuestionGrid(true);
            mcqVariable.setQuestionItemName(questionName);
            variablesMap.putVariable(mcqVariable);
            variables.remove(variableName);
        }
    }

    private static void iterateOnComponentsToFindResponses(JsonNode node, List<String> variables,
                                                           VariablesMap variablesMap, Group group,
                                                           boolean isLunaticV2) {
        JsonNode components = node.get(COMPONENTS);
        if (components.isArray()) {
            for (JsonNode component : components) {
                addResponsesAndMissing(component, group, variables, variablesMap, isLunaticV2);
            }
        }
    }

    /**
     * Iterate on the components in the body of a table to find the responses
     *
     * @param tableComponent : component representing a table
     * @param group          : group to which the variables belong
     * @param variablesMap   : metadata model of the questionnaire to be completed
     * @param isLunaticV2    : true if the Lunatic version is 2.3 or higher
     */
    private static void iterateOnTableBody(JsonNode tableComponent, Group group, List<String> variables,
                                           VariablesMap variablesMap, boolean isLunaticV2) {
        // In we case of a table component we have to iterate on the body components to find the responses
        // The body is a nested array of arrays
        // In Lunatic 2.2 and lower the body is called cells
        JsonNode body = isLunaticV2 ? tableComponent.get("body") : tableComponent.get("cells");
        for (JsonNode arr : body) {
            if (arr.isArray()) {
                for (JsonNode cell : arr) {
                    if (cell.has(COMPONENT_TYPE)) {
                        addResponsesAndMissing(cell, group, variables, variablesMap, isLunaticV2);
                    }
                }
            }
        }
    }

    private static Group processDependingLoop(List<String> variables, VariablesMap variablesMap, Group parentGroup,
                                              JsonNode component, boolean isLunaticV2) {
        JsonNode loopDependencies = component.get("loopDependencies");
        String groupName;
        if (!loopDependencies.isEmpty()) {
            StringBuilder groupNameBuilder = new StringBuilder(loopDependencies.get(0).asText());
            for (int i = 1; i < loopDependencies.size(); i++) {
                groupNameBuilder.append("_").append(loopDependencies.get(i).asText());
            }
            groupName = groupNameBuilder.toString();
        } else {
            int i = 1;
            groupName = "UNNAMED_" + i;
            List<String> groups = variablesMap.getGroupNames();
            while (groups.contains(groupName)) {
                i++;
                groupName = "UNNAMED_" + i;
            }
        }
        log.info("Creation of group :" + groupName);
        Group group = putNewGroup(variablesMap, groupName, parentGroup);
        iterateOnComponentsToFindResponses(component, variables, variablesMap, group, isLunaticV2);
        return group;
    }

    /**
     * Find the common part of a list of strings that differs only at the end
     *
     * @param similarStrings : list of strings
     * @return the common prefix
     */
    public static String findLongestCommonPrefix(List<String> similarStrings) {
        int minLength = similarStrings.getFirst().length();
        for (String str : similarStrings) {
            if (str.length() < minLength) {
                minLength = str.length();
            }
        }
        String commonPrefix = "";
        for (int i = 1; i < minLength; i++) {
            boolean isCommon = true;
            String stringToTest = similarStrings.getFirst().substring(0, i);
            for (String str : similarStrings) {
                if (!str.startsWith(stringToTest)) {
                    isCommon = false;
                    break;
                }
            }
            if (isCommon) {
                commonPrefix = stringToTest;
            } else {
                break;
            }
        }

        return commonPrefix;
    }

    private static JsonNode readJson(Path filePath) throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        return mapper.readTree(filePath.toFile());
    }

    /**
     * Add the missing variable defined in the component if present
     *
     * @param component     : a questionnaire component
     * @param group         : group to which the variables belong
     * @param variables     : list of variables to be completed
     * @param variablesMap : metadata model of the questionnaire to be completed
     */
    private static void addMissingVariable(JsonNode component, Group group, List<String> variables,
                                           VariablesMap variablesMap) {
        if (component.has(MISSING_RESPONSE)) {
            String missingVariable = component.get(MISSING_RESPONSE).get("name").asText();
            variablesMap.putVariable(new Variable(missingVariable, group, VariableType.STRING));
            variables.remove(missingVariable);
        }
    }
}