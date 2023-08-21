package fr.insee.genesis.exceptions;

import lombok.Getter;

public class GenesisError {

	@Getter
	private String message;

	public GenesisError(String message) {
		this.message = message;
	}

}
