package fr.insee.genesis.exceptions;

import lombok.Getter;

@Getter
public class GenesisError {

	private final String message;

	public GenesisError(String message) {
		this.message = message;
	}

}
