package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Builder
@Getter
@Setter
public class VariableDto {

	private String idVar;
	private List<String> values;

}
