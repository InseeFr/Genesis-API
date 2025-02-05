package fr.insee.genesis.infrastructure.document.surveyunit;

import java.io.Serial;
import java.io.Serializable;

import lombok.Data;

@Data
public class VariableDocument implements Serializable {

	@Serial
	private static final long serialVersionUID = -1576556180669134053L;
	private String varId;
	private String loopId;
	private String parentId;
	private String value;
	private Integer iteration;
}