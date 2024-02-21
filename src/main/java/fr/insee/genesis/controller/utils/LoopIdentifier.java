package fr.insee.genesis.controller.utils;

import fr.insee.genesis.Constants;
import fr.insee.genesis.controller.sources.ddi.Variable;
import fr.insee.genesis.controller.sources.ddi.VariablesMap;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class LoopIdentifier {

	private LoopIdentifier() {
		throw new IllegalStateException("Utility class");
	}

	public static String getLoopIdentifier(String variableName, VariablesMap variablesMap, int index) {
		Variable variable = variablesMap.getVariable(variableName);
		if (variable == null) {
			if(variableName.contains(Constants.VARIABLE_NAME_DELIMITER)
					&& variablesMap.getVariableNames().contains(variableName.split(Constants.VARIABLE_NAME_DELIMITER)[0])
			){
				Variable parentVariable = variablesMap.getVariable(variableName.split(Constants.VARIABLE_NAME_DELIMITER)[0]);
				return parentVariable.getGroupName();
			}
			log.debug("Variable {} not found in variablesMap and assigned in root group", variableName);
			return Constants.ROOT_GROUP_NAME;
		}
		if (variable.getGroup().isRoot()) {
			return variable.getGroup().getName();
		}
		return String.format("%s_%d", variable.getGroup().getName() ,index);
	}

	public static String getParentGroupName(String variableName, VariablesMap variablesMap) {
		Variable variable = variablesMap.getVariable(variableName);

		if ( variable == null ) {
			if(variableName.contains(Constants.VARIABLE_NAME_DELIMITER)
				&& variablesMap.getVariableNames().contains(variableName.split(Constants.VARIABLE_NAME_DELIMITER)[0])
			) {
				return variableName.split(Constants.VARIABLE_NAME_DELIMITER)[0];
			}
			return null;
		}
		if (variable.getGroup().isRoot()) {
			return null;
		}
		return variable.getGroup().getParentName();
	}
}
