package fr.insee.genesis.domain.utils;

import fr.insee.genesis.Constants;
import fr.insee.bpm.metadata.model.Variable;
import fr.insee.bpm.metadata.model.VariablesMap;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class GroupUtils {

	private GroupUtils() {
		throw new IllegalStateException("Utility class");
	}

	public static String getGroupName(String variableName, VariablesMap variablesMap){
		Variable variable = variablesMap.getVariable(variableName);
		// If we don't find the variable, but it's A FILTER_RESULT or _MISSING variable
		// Then we look for the variable from which it derives
		if (variable == null) {
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
		Variable variable = variablesMap.getVariable(variableName);
		// If we don't find the variable, but it's A FILTER_RESULT or _MISSING variable
		// Then we look for the variable from which it derives
		if (variable == null) {
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
		Variable variable = variablesMap.getVariable(variableName);
		List<String> varsEno = Arrays.asList(Constants.getEnoVariables());
		if ( variable == null ) {
			// Variables added by Eno and identified in the constants list have ROOT_GROUP_NAME scope
			if(varsEno.contains(variableName))
			{
				return Constants.ROOT_GROUP_NAME;
			}
			if(variableName.startsWith(Constants.FILTER_RESULT_PREFIX)
					&& variablesMap.hasVariable(variableName.replace(Constants.FILTER_RESULT_PREFIX,""))
			){
				return variableName.replace(Constants.FILTER_RESULT_PREFIX,"");
			}
			if(variableName.endsWith(Constants.MISSING_SUFFIX)
					&& variablesMap.hasVariable(variableName.replace(Constants.MISSING_SUFFIX,""))
			){
				return variableName.replace(Constants.MISSING_SUFFIX,"");
			}
			return null;
		}
		if (variable.getGroup().isRoot()) {
			return null;
		}
		return variable.getGroup().getParentName();
	}


}
