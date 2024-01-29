package fr.insee.genesis.infrastructure.model;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

import lombok.Data;

@Data
public class VariableState implements Serializable {

	@Serial
	private static final long serialVersionUID = -1576556180669134053L;
	private String idVar;
	private String idLoop;
	private String idParent;
	private List<String> values;
}