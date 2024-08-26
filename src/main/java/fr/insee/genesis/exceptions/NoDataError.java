package fr.insee.genesis.exceptions;

import fr.insee.genesis.domain.dtos.Mode;
import lombok.Getter;

@Getter
public class NoDataError extends GenesisError {

	private final Mode mode;

	public NoDataError(String message, Mode mode) {
		super(message);
		this.mode = mode;
	}
}
