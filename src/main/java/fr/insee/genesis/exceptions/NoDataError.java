package fr.insee.genesis.exceptions;

import fr.insee.genesis.controller.model.Mode;
import lombok.Getter;

public class NoDataError extends GenesisError {

	@Getter
	private Mode mode;

	public NoDataError(String message, Mode mode) {
		super(message);
		this.mode = mode;
	}
}
