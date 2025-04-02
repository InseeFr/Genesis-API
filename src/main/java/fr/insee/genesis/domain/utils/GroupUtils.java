package fr.insee.genesis.domain.utils;

import fr.insee.genesis.Constants;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import lombok.experimental.UtilityClass;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
@UtilityClass
public class GroupUtils {

	public static String getGroupName(String variableName, VariablesMap variablesMap){
		List<String> varsEno = Arrays.asList(Constants.getEnoVariables());
		Variable variable = variablesMap.getVariable(variableName);
		// If we don't find the variable, but it's A FILTER_RESULT or _MISSING variable
		// Then we look for the variable from which it derives
		if (variable == null) {
			// Variables added by Eno and identified in the constants list have ROOT_GROUP_NAME scope
			if(varsEno.contains(variableName))
			{
				return Constants.ROOT_GROUP_NAME;
			}
			String relatedVariable = getRelatedVariableName(variableName,variablesMap);
			if (relatedVariable==null){
				// If we don't find a related variable, we assign variable to ROOT_GROUP_NAME
				log.debug("Variable {} not found in variablesMap and assigned in root group", variableName);
				return Constants.ROOT_GROUP_NAME;
			}
			return variablesMap.getVariable(relatedVariable).getGroupName();
		}
		// If the variable was in we return directly the group name
		return variable.getGroup().getName();
	}

	public static String getParentGroupName(String variableName, VariablesMap variablesMap){
		List<String> varsEno = Arrays.asList(Constants.getEnoVariables());
		Variable variable = variablesMap.getVariable(variableName);
		// If we don't find the variable, but it's A FILTER_RESULT or _MISSING variable
		// Then we look for the variable from which it derives
		if (variable == null) {
			// Variables added by Eno and identified in the constants list have ROOT_GROUP_NAME scope
			if(varsEno.contains(variableName))
			{
				return null;
			}
			String relatedVariableName = getRelatedVariableName(variableName,variablesMap);
			if (relatedVariableName==null){
				// If we don't find a related variable, we assign variable to ROOT_GROUP_NAME
				// so parent group is empty
				log.debug("Variable {} not found in variablesMap and assigned in root group, parent group name is empty", variableName);
				return null;
			}
			Variable relatedVariable =  variablesMap.getVariable(relatedVariableName);
			return relatedVariable.getGroup().isRoot() ? null : relatedVariable.getGroup().getParentName();
		}
		// If the variable was in metadata, we return directly the parent group name
		return variable.getGroup().isRoot() ? null : variable.getGroup().getParentName();
	}

	private static String getRelatedVariableName(String variableName, VariablesMap variablesMap) {
		if(variablesMap.hasVariable(removePrefixOrSuffix(variableName, Constants.FILTER_RESULT_PREFIX)))
		{
			return removePrefixOrSuffix(variableName, Constants.FILTER_RESULT_PREFIX);
		}
		if(variablesMap.hasVariable(removePrefixOrSuffix(variableName, Constants.MISSING_SUFFIX))
		){
			return removePrefixOrSuffix(variableName, Constants.MISSING_SUFFIX);
		}
		return null;
	}

	private static String removePrefixOrSuffix(String variableName, String pattern) {
		if (variableName.startsWith(pattern)){
			return variableName.replace(pattern, "");
		}
		if (variableName.endsWith(pattern)){
			return variableName.replace(pattern, "");
		}
		return variableName;
	}

}
