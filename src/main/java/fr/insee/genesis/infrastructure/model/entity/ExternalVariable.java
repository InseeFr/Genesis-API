package fr.insee.genesis.infrastructure.model.entity;

import lombok.Data;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
public class ExternalVariable implements Serializable {

	@Serial
	private static final long serialVersionUID = 6267528628435012000L;
	private String idVar;
	private List<String> values;
}
