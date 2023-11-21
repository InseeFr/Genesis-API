package fr.insee.genesis.domain.dtos;

import lombok.*;

import java.util.List;

@Builder
@Getter
@Setter
public class VariableDto {

	private String idVar;
	private List<String> values;
}
