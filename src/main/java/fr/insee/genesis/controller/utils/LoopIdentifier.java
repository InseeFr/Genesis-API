package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.metadataparserlib.metadata.model.Variable;
import fr.insee.metadataparserlib.metadata.model.VariablesMap;
import lombok.extern.slf4j.Slf4j;

import java.util.Arrays;
import java.util.List;

@Slf4j
public class LoopIdentifier {

	private LoopIdentifier() {
		throw new IllegalStateException("Utility class");
	}

	public static String getLoopIdentifier(String variableName, VariablesMap variablesMap, int index) {
		Variable variable = variablesMap.getVariable(variableName);
		if (variable == null) {
			if(variableName.startsWith(Constants.FILTER_RESULT_PREFIX)
					&& variablesMap.hasVariable(variableName.replace(Constants.FILTER_RESULT_PREFIX,""))
			){
				return getRelatedVariableGroupName(variablesMap, variableName, Constants.FILTER_RESULT_PREFIX);
			}
			if(variableName.endsWith(Constants.MISSING_SUFFIX)
					&& variablesMap.hasVariable(variableName.replace(Constants.MISSING_SUFFIX,""))
			){
				return getRelatedVariableGroupName(variablesMap, variableName, Constants.MISSING_SUFFIX);
			}
			log.debug("Variable {} not found in variablesMap and assigned in root group", variableName);
			return Constants.ROOT_GROUP_NAME;
		}
		if (variable.getGroup().isRoot()) {
			return variable.getGroup().getName();
		}
		return String.format("%s_%d", variable.getGroup().getName() ,index);
	}

	private static String getRelatedVariableGroupName(VariablesMap variablesMap, String variableName, String constantToReplace) {
		Variable relatedVariable = variablesMap.getVariable(variableName.replace(constantToReplace, ""));
		return relatedVariable.getGroupName();
	}

	public static String getRelatedVariableName(String variableName, VariablesMap variablesMap) {
		Variable variable = variablesMap.getVariable(variableName);
		List<String> varsEno = Arrays.asList(Constants.getEnoVariables());
		if ( variable == null ) {
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
