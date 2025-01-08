package fr.insee.genesis.domain.model.surveyunit;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class Variable {

	private String varId;
	private List<String> values;

}
