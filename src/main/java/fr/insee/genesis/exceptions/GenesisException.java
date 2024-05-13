package fr.insee.genesis.exceptions;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.io.Serial;

@Getter
@AllArgsConstructor
public class GenesisException extends Exception{

    /**
	 * 
	 */
	@Serial
	private static final long serialVersionUID = 3356078796351491095L;

	private final int status;

    private final String message;

}
