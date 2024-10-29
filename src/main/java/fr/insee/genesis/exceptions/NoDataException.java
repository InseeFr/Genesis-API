package fr.insee.genesis.exceptions;

import lombok.Getter;

@Getter
public class NoDataException extends Exception {
	public NoDataException(String message) {
		super(message);
	}
}
