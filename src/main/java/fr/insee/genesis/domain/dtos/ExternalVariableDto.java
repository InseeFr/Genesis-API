package fr.insee.genesis.domain.dtos;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Builder
@Data
public class ExternalVariableDto {

	private String idVar;
	private List<String> values;
}
