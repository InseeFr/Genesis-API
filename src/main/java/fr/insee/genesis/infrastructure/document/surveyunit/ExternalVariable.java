package fr.insee.genesis.infrastructure.document.surveyunit;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExternalVariable implements Serializable {

	@Serial
	private static final long serialVersionUID = 6267528628435012000L;
	private String varId;
	private List<String> values;
}